package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.JobEntity
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import android.text.Html
import android.text.method.LinkMovementMethod

fun stripHtml(html: String): String {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html).toString()
        }
    } catch (e: Exception) {
        html
    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                setTextColor(android.graphics.Color.parseColor("#CBD5E1"))
                textSize = 14f
                setLineSpacing(4f, 1.1f)
                movementMethod = LinkMovementMethod.getInstance()
                setLinkTextColor(android.graphics.Color.parseColor("#3B82F6"))
            }
        },
        update = { textView ->
            // 🔥 Add basic CSS to style lists
            val styledHtml = """
                <style>
                    body { color: #CBD5E1; font-size: 14px; line-height: 1.6; }
                    ul, ol { padding-left: 20px; }
                    li { margin-bottom: 4px; }
                    b, strong { color: #F1F5F9; }
                </style>
                $html
            """.trimIndent()
            
            val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(styledHtml, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(styledHtml)
            }
            textView.text = spanned
        }
    )
}

data class MockAttachment(
    val name: String,
    val type: String, // "pdf" or "image"
    val description: String,
    val url: String? = null,
    val thumbnail: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: Int,
    viewModel: MainViewModel,
    onNavigateToAi: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    
    val allJobs by viewModel.allJobs.collectAsState()
    val rawJob = allJobs.find { it.id == jobId }

    val liveJobDetail by viewModel.liveJobDetail.collectAsState()
    val isJobDetailLoading by viewModel.isJobDetailLoading.collectAsState()
    val liveCompanies by viewModel.liveCompanies.collectAsState()

    LaunchedEffect(rawJob?.remoteId) {
        val remoteId = rawJob?.remoteId
        if (!remoteId.isNullOrEmpty()) {
            viewModel.fetchLiveJobDetail(remoteId)
        }
    }

    LaunchedEffect(Unit) {
        if (liveCompanies == null) {
            viewModel.fetchLiveCompanies()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLiveJobDetail()
        }
    }

    val job = remember(rawJob, liveJobDetail) {
        val ld = liveJobDetail
        if (ld != null) {
            // 🔥 Build from live data entirely
            JobEntity(
                id = rawJob?.id ?: -1,
                title = ld.title,
                company = ld.company,
                logoResName = ld.logoUrl ?: ld.logoResName ?: "",
                location = ld.location,
                salary = ld.salary ?: "Tshs / Neg",
                type = ld.type ?: "Full-time",
                workplace = ld.workplace ?: "Remote",
                datePosted = ld.postedAt ?: ld.datePosted ?: "Recent",
                description = ld.description ?: "",
                requirements = ld.requirements ?: "",
                benefits = ld.benefits ?: "",
                category = ld.category ?: "General",
                companyWebsite = ld.companyWebsite ?: "",
                remoteId = ld.id ?: rawJob?.remoteId ?: ""
            )
        } else {
            rawJob
        }
    }

    if (job == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF3B82F6))
        }
        return
    }

    val companyDescription = remember(job, liveCompanies) {
        val matched = liveCompanies?.find { 
            it.name.equals(job.company, ignoreCase = true) || 
            (!it.website.isNullOrEmpty() && job.companyWebsite.isNotEmpty() && it.website.contains(job.companyWebsite, ignoreCase = true))
        }
        matched?.description
    }

    // Navigation and interactive states
    var showApplyDialog by remember { mutableStateOf(false) }
    var showRedirectDialog by remember { mutableStateOf(false) }
    var applicationNotes by remember { mutableStateOf("") }
    
    var timeLeft by remember { mutableStateOf(10) }
    var animatedProgress by remember { mutableStateOf(0f) }

    val todayStr = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    val isExpired = remember(job, liveJobDetail, todayStr) {
        val active = liveJobDetail?.active ?: job.active
        val expiresAt = liveJobDetail?.expiresAt
        (!active) || (!expiresAt.isNullOrEmpty() && expiresAt < todayStr)
    }

    val whatsappNumber = liveJobDetail?.whatsapp_number
    val applicationInstructions = liveJobDetail?.application_instructions
    val jobUrl = remember(job, liveJobDetail) {
        liveJobDetail?.url ?: job.companyWebsite
    }
    val isEmailLink = remember(jobUrl) {
        !jobUrl.isNullOrEmpty() && jobUrl.startsWith("mailto:")
    }
    val hasWhatsApp = remember(whatsappNumber) {
        !whatsappNumber.isNullOrEmpty() && whatsappNumber.trim().length > 6
    }
    val hasInstructions = remember(applicationInstructions) {
        !applicationInstructions.isNullOrEmpty() && applicationInstructions.trim().isNotEmpty()
    }

    LaunchedEffect(showRedirectDialog) {
        val currentJob = job
        if (showRedirectDialog) {
            timeLeft = 10
            animatedProgress = 0f
            val durationMs = 10000L
            val startTime = System.currentTimeMillis()
            while (showRedirectDialog) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= durationMs) {
                    animatedProgress = 1f
                    timeLeft = 0
                    viewModel.applyToJob(currentJob, "Auto-redirected via Telemetry Bridge")
                    try {
                        uriHandler.openUri(jobUrl)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                    }
                    showRedirectDialog = false
                    break
                }
                animatedProgress = elapsed.toFloat() / durationMs
                timeLeft = 10 - (elapsed / 1000).toInt()
                delay(30)
            }
        }
    }
    
    // Viewer states
    var viewerOpen by remember { mutableStateOf(false) }
    var viewerIndex by remember { mutableStateOf(0) }

    // Related Jobs list (exclude self, up to 3)
    val relatedJobs = remember(allJobs, job, liveJobDetail) {
        val ld = liveJobDetail
        if (ld != null && !ld.relatedJobs.isNullOrEmpty()) {
            ld.relatedJobs.map { rj ->
                JobEntity(
                    id = -1, // temporary mock id
                    title = rj.title,
                    company = rj.company,
                    logoResName = rj.logoResName ?: "",
                    location = rj.location,
                    salary = rj.salary ?: "Tshs / Neg",
                    type = rj.type ?: "Full-time",
                    workplace = rj.workplace ?: "Remote",
                    datePosted = rj.datePosted ?: "Just now",
                    category = rj.category ?: job.category,
                    companyWebsite = rj.companyWebsite ?: "https://jobsreport.online",
                    description = rj.description ?: "",
                    requirements = rj.requirements ?: "",
                    benefits = rj.benefits ?: "",
                    remoteId = rj.id ?: ""
                )
            }
        } else {
            allJobs.filter { it.category == job.category && it.id != job.id }.take(3)
        }
    }

    // Attachments Section — only show if real attachments exist
    val attachments = remember(job, liveJobDetail) {
        val realImages = liveJobDetail?.images
        if (!realImages.isNullOrEmpty()) {
            realImages.map { img ->
                val type = if (img.type == "pdf" || img.name?.lowercase()?.endsWith(".pdf") == true) "pdf" else "image"
                MockAttachment(
                    name = img.name ?: "Attachment",
                    type = type,
                    description = img.caption ?: img.seoDescription ?: "",
                    url = img.url,
                    thumbnail = img.thumbnail ?: img.url
                )
            }
        } else {
            emptyList() // 🔥 No mock data
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("detail_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Market Vacancy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )

                            if (isJobDetailLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        color = Color(0xFF3B82F6),
                                        strokeWidth = 1.2.dp
                                    )
                                    Text(
                                        text = "Syncing...",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            } else if (liveJobDetail != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Text(
                                        text = "Live Sync",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, job.title)
                                    putExtra(Intent.EXTRA_TEXT, "Check out this job opportunity: ${job.title} at ${job.company} on JobsReport.online")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Job via"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Job",
                                tint = Color(0xFF94A3B8)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleBookmark(job) },
                            modifier = Modifier.testTag("detail_bookmark_btn")
                        ) {
                            Icon(
                                imageVector = if (job.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (job.isBookmarked) Color(0xFF3B82F6) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasInstructions && !isExpired) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.05f))
                                .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "📋 How to Apply: $applicationInstructions",
                                fontSize = 10.sp,
                                color = Color(0xFF93C5FD),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    if (!jobUrl.isNullOrEmpty() && !isExpired && !hasWhatsApp) {
                        val displayUrl = remember(jobUrl) {
                            if (isEmailLink) {
                                jobUrl.replace("mailto:", "")
                            } else {
                                try {
                                    val uri = java.net.URI(jobUrl)
                                    val host = uri.host?.replace("www.", "") ?: jobUrl
                                    host + (uri.path ?: "")
                                } catch (e: Exception) {
                                    jobUrl
                                }
                            }
                        }
                        Text(
                            text = if (isEmailLink) "📧 $displayUrl" else "🔗 $displayUrl",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (hasWhatsApp && !isExpired) {
                        Button(
                            onClick = {
                                val cleanNum = whatsappNumber?.filter { it.isDigit() } ?: ""
                                val text = "Hello, I am interested in the ${job.title} position at ${job.company}. Please share more details."
                                uriHandler.openUri("https://wa.me/$cleanNum?text=${android.net.Uri.encode(text)}")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("💬 Apply via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else if (isExpired) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFFEF4444).copy(alpha = 0.1f),
                                disabledContentColor = Color(0xFFF87171)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🚫 Application Closed", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else if (!jobUrl.isNullOrEmpty()) {
                        Button(
                            onClick = { showRedirectDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isEmailLink) "✉️ Send Application" else "Apply Now",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                                disabledContentColor = Color(0xFF475569)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("No application link available", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val logoUrl = liveJobDetail?.logoUrl ?: job.logoResName
                    val isUrl = logoUrl.startsWith("http://") || logoUrl.startsWith("https://")
                    val context = LocalContext.current
                    val imageResId = remember(logoUrl) {
                        if (!isUrl && logoUrl.isNotEmpty()) {
                            context.resources.getIdentifier(logoUrl, "drawable", context.packageName)
                        } else 0
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUrl) {
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = job.company,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = job.company,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = job.company.take(2).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = job.company,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold
                    )

                    if (isExpired) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "EXPIRED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFF334155).copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Meta grid rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetaItemCompact(icon = Icons.Outlined.LocationOn, label = "LOCATION", value = job.location)
                        MetaItemCompact(icon = Icons.Outlined.MonetizationOn, label = "SALARY", value = job.salary)
                        MetaItemCompact(icon = Icons.Outlined.WorkOutline, label = "ROLE TYPE", value = "${job.workplace} / ${job.type}")
                    }
                }
            }

            // Safety check / Report Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.04f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = "Shield warning",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "STAY SAFE ONLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF87171),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Never pay money for application requests or interviews.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            val mailText = "Please review this listing: ${job.title} at ${job.company}"
                            uriHandler.openUri("mailto:jjovinatha@gmail.com?subject=Report%20Job&body=${android.net.Uri.encode(mailText)}")
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF87171))
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = "Report", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REPORT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Company Info Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ABOUT ${job.company.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(job.company, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                    if (!job.companyWebsite.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = job.companyWebsite.replace("https://", "").replace("www.", ""),
                                color = Color(0xFF3B82F6),
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { uriHandler.openUri(job.companyWebsite) }
                            )
                        }
                    }
                    if (!companyDescription.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = companyDescription ?: "",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Attachments Section — only show if real attachments exist
            if (attachments.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ATTACHMENTS (${attachments.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            attachments.forEachIndexed { index, attachment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                                        .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewerIndex = index
                                            viewerOpen = true
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (attachment.type == "pdf") Color(0xFFEF4444).copy(alpha = 0.12f)
                                                    else Color(0xFF3B82F6).copy(alpha = 0.12f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (attachment.type == "image" && !attachment.thumbnail.isNullOrEmpty()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(attachment.thumbnail)
                                                        .crossfade(true)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                        .build(),
                                                    contentDescription = attachment.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (attachment.type == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                                    contentDescription = attachment.type,
                                                    tint = if (attachment.type == "pdf") Color(0xFFF87171) else Color(0xFF60A5FA),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = attachment.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = attachment.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                Toast.makeText(context, "Initiated download of ${attachment.name}...", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Download",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "View",
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Job Description
            DetailSectionCompact(title = "Job Description") {
                HtmlText(
                    html = job.description,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Requirements
            DetailSectionCompact(title = "Requirements") {
                HtmlText(
                    html = job.requirements,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Benefits
            DetailSectionCompact(title = "Benefits & Perks") {
                HtmlText(
                    html = job.benefits,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mock AD Banner represented nicely
            MockAdBanner()

            // Similar Active Jobs List
            if (relatedJobs.isNotEmpty()) {
                Column {
                    Text(
                        text = "SIMILAR ACTIVE JOBS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        relatedJobs.forEach { rJob ->
                            Card(
                                onClick = {
                                    // Direct replacement inside the same composable without stacking too many states
                                    onBackClick()
                                    viewModel.allJobs.value.find { it.id == rJob.id }?.let {
                                        // Navigate to same detail but clean
                                        onBackClick() // double pop to prevent nesting if needed or just navigates
                                    }
                                    // Robust route navigation
                                    uriHandler.openUri("https://jobsreport.online/market/job-${rJob.id}") // Mock fallback or opens
                                    Toast.makeText(context, "Opening ${rJob.title}...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rJob.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowOutward,
                                            contentDescription = "Details",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rJob.company,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "•",
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = rJob.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal apply dialogue
    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = {
                Text(
                    text = "Submit Application",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Would you like to write a custom cover note or paste your online portfolio URL before dispatching to ${job.company}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                    OutlinedTextField(
                        value = applicationNotes,
                        onValueChange = { applicationNotes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("apply_note_input"),
                        label = { Text("Portfolio Link / Remarks", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. My portfolio: https://github.com/myprofile", color = Color(0xFF475569)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applyToJob(job, applicationNotes)
                        showApplyDialog = false
                        Toast.makeText(context, "Application sent successfully!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.testTag("dialog_submit_btn")
                ) {
                    Text("Send Application", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showApplyDialog = false }
                ) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B),
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }

    // Career Redirect Dialog
    if (showRedirectDialog) {
        val isEmail = jobUrl.startsWith("mailto:")
        val domain = remember(jobUrl) {
            try {
                val uri = java.net.URI(jobUrl)
                uri.host ?: jobUrl
            } catch (e: Exception) {
                jobUrl
            }
        }
        Dialog(
            onDismissRequest = { showRedirectDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { showRedirectDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) {}
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "TELEMETRY ROUTE VERIFIED",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA),
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            IconButton(
                                onClick = { showRedirectDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Company Info
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (isEmail) "OPENING EMAIL APPLICATION" else "OPENING EXTERNAL CAREER PAGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = job.company,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SubdirectoryArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA).copy(alpha = 0.9f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = job.title,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF60A5FA).copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Countdown Box
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Redirect Bridge Active",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 0.5.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Redirecting in:",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "${timeLeft}s",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF60A5FA)
                                        )
                                    }
                                }

                                // Custom Animated Progress Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1C1917))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(animatedProgress)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6))
                                                )
                                            )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Safe",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Safe: $domain",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Informational warning text
                        Text(
                            text = "You are leaving the JobsReport telemetry interface. External career pages are hosted directly by hiring corporate entities.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )

                        // AD section mock
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.01f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "JOBSREPORT VERIFIED ADVERTISEMENT",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Premium Ads Safeguard Active",
                                    fontSize = 8.sp,
                                    color = Color(0xFF334155)
                                )
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.applyToJob(job, "Applied manually via Telemetry Bridge")
                                    try {
                                        uriHandler.openUri(jobUrl)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                                    }
                                    showRedirectDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isEmail) "📧 Open Email App" else "Open Now",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Go",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { showRedirectDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-screen Viewer dialog for mock attachments
    if (viewerOpen) {
        val activeAttachment = attachments[viewerIndex]
        Dialog(
            onDismissRequest = { viewerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Title Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.8f))
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewerOpen = false }) {
                                Icon(Icons.Default.Close, "Close viewer", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeAttachment.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Preview Mode // Secure Stream",
                                    color = Color(0xFF10B981),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Secure Download completed for ${activeAttachment.name}", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Download, "Download", tint = Color.White)
                        }
                    }

                    // Content Viewer
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeAttachment.type == "pdf") {
                            // Beautiful rendered Mock Document
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.72f)
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "OFFICIAL VACANCY STATEMENT",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF60A5FA)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "SECURE PDF",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFF87171)
                                                )
                                            }
                                        }

                                        Divider(color = Color(0xFF334155))

                                        Text(
                                            text = "JOB TITLE: ${job.title}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )

                                        Text(
                                            text = "ORGANIZATION: ${job.company}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF94A3B8)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "CIRCULAR DIGEST DETAILS:\n${stripHtml(job.description).take(200)}...",
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            color = Color(0xFFCBD5E1)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "QUALIFICATION OUTLINES:\n${stripHtml(job.requirements).take(150)}...",
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "JOBSREPORT VERIFIED SEAL",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "PAGE 1 OF 1",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        } else {
                            if (!activeAttachment.url.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(activeAttachment.url)
                                            .crossfade(true)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = activeAttachment.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                }
                            } else {
                                // Beautiful rendered Mock Image poster
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                                                )
                                            )
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Business,
                                                    contentDescription = null,
                                                    tint = Color(0xFF60A5FA),
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text(
                                                text = "WE ARE HIRING!",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                letterSpacing = 2.sp
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = job.title.uppercase(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF472B6),
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Text(
                                                text = "Join us at ${job.company}.\nApply securely on JobsReport.online.",
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8),
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Navigation footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(vertical = 20.dp, horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewerIndex = 0 },
                            enabled = viewerIndex > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, "Previous")
                            Text("Prev")
                        }

                        Text(
                            text = "${viewerIndex + 1} / ${attachments.size}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { viewerIndex = 1 },
                            enabled = viewerIndex < attachments.size - 1,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Next")
                            Icon(Icons.Default.KeyboardArrowRight, "Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetaItemCompact(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DetailSectionCompact(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Divider(color = Color(0xFF334155).copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
fun MockAdBanner() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { /* No-op, just aesthetic */ }
    ) {
        val stroke = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        drawRoundRect(
            color = Color(0xFF334155),
            style = stroke
        )
    }
    // overlay label inside Box centered
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SPONSORED INTEGRITY LINK",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "JobsReport.online Premium Ads Safeguard",
                fontSize = 9.sp,
                color = Color(0xFF334155)
            )
        }
    }
}
