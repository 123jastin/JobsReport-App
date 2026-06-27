package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.network.RemoteReportDetail
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    slug: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToJobDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val reportDetail by viewModel.liveReportDetail.collectAsState()
    val isLoading by viewModel.isReportDetailLoading.collectAsState()

    // Fetch report on entry
    LaunchedEffect(slug) {
        viewModel.fetchLiveReportDetail(slug)
    }

    // Clean report detail state on exit
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearLiveReportDetail()
        }
    }

    // Beautiful slate theme background colors
    val bgDark = Color(0xFF0F172A)
    val cardBg = Color(0xFF1E293B)
    val accentBlue = Color(0xFF3B82F6)
    val textMuted = Color(0xFF94A3B8)
    val borderCol = Color(0xFF334155).copy(alpha = 0.5f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = bgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "INTELLIGENCE BRIEFING",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("report_detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, reportDetail?.title ?: "JobsReport Market Report")
                                putExtra(Intent.EXTRA_TEXT, "Read this awesome job market intelligence briefing: https://jobsreport.online/report/$slug")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Intelligence Briefing"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgDark,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgDark)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = accentBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SYNCING MARKET INTELLIGENCE DATA...",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = textMuted
                    )
                }
            } else if (reportDetail == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "No data",
                        tint = textMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Briefing Not Found",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The requested market intelligence report could not be found or has been archived.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Back to Intel Feed", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val report = reportDetail!!
                val stats = report.stats
                val chartData = report.chartData ?: emptyList()
                val distribution = report.distribution ?: emptyList()
                val companies = report.companies ?: emptyList()
                val jobs = report.jobs ?: emptyList()

                val todayStr = remember {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                }
                val sortedJobs = remember(jobs, todayStr) {
                    jobs.sortedWith { a, b ->
                        val aExpired = (a.active == false) || (!a.expiresAt.isNullOrEmpty() && a.expiresAt!! < todayStr)
                        val bExpired = (b.active == false) || (!b.expiresAt.isNullOrEmpty() && b.expiresAt!! < todayStr)
                        when {
                            aExpired && !bExpired -> 1
                            !aExpired && bExpired -> -1
                            else -> {
                                val aDate = a.postedAt ?: a.datePosted ?: ""
                                val bDate = b.postedAt ?: b.datePosted ?: ""
                                bDate.compareTo(aDate)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Breadcrumb navigation & Source tag
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Home",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted,
                                modifier = Modifier.clickable { onBackClick() }
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = textMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Reports",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted,
                                modifier = Modifier.clickable { onBackClick() }
                            )
                            if (!report.country.isNullOrEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = report.country!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted
                                )
                            }
                        }
                    }

                    // Title Header Block
                    item {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Text(
                                    text = "MARKET ANALYSIS / INTELLIGENCE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF10B981)
                                )
                            }

                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 38.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Metadata details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = textMuted, modifier = Modifier.size(12.dp))
                                    Text(text = report.monthYear ?: "Recently", style = MaterialTheme.typography.bodySmall, color = textMuted)
                                }
                                if (!report.role.isNullOrEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = textMuted, modifier = Modifier.size(12.dp))
                                        Text(text = report.role!!.uppercase(), style = MaterialTheme.typography.bodySmall, color = textMuted)
                                    }
                                }
                                if (!report.country.isNullOrEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = textMuted, modifier = Modifier.size(12.dp))
                                        Text(text = report.country!!, style = MaterialTheme.typography.bodySmall, color = textMuted)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = borderCol)
                        }
                    }

                    // Key metrics cards Row (Companies, Growth, Placements)
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Hiring Companies Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "COMPANIES HIRING",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${stats?.companies ?: companies.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Active Employers",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Growth Trend Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "GROWTH TREND",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${stats?.growth ?: 14}%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Above avg velocity",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Active Placements Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "PLACEMENTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${jobs.size}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Active Positions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF3B82F6),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Visual Chart Data section / Location Distribution section
                    val hasChartData = chartData.isNotEmpty() && chartData.any { it.demand > 0f }
                    if (hasChartData) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderCol, RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = accentBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Job Demand Velocity",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Dynamic Bar Chart with Custom Canvas Drawing
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        chartData.forEach { data ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = data.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = textMuted,
                                                    modifier = Modifier.width(100.dp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Normalized value container
                                                val demandValue = data.demand
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(14.dp)
                                                        .background(Color(0xFF0F172A), RoundedCornerShape(100.dp))
                                                ) {
                                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                                        val percentage = (demandValue / 100f).coerceIn(0f, 1f)
                                                        drawRoundRect(
                                                            color = Color(0xFF8B5CF6),
                                                            size = Size(size.width * percentage, size.height),
                                                            cornerRadius = CornerRadius(100f, 100f)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Text(
                                                    text = "${demandValue.toInt()}%",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF8B5CF6),
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (distribution.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, borderCol, RoundedCornerShape(20.dp)),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Text(
                                            text = "Location Distribution",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        val colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B))

                                        // Render visual representation ring chart
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Left: Donut Chart Canvas
                                            Box(
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                    val total = distribution.sumOf { it.value.toDouble() }.toFloat()
                                                    var startAngle = -90f
                                                    distribution.forEachIndexed { index, item ->
                                                        val sweepAngle = (item.value / total) * 360f
                                                        drawArc(
                                                            color = colors[index % colors.size],
                                                            startAngle = startAngle,
                                                            sweepAngle = sweepAngle,
                                                            useCenter = false,
                                                            style = Stroke(width = 24f)
                                                        )
                                                        startAngle += sweepAngle
                                                    }
                                                }
                                                Text(
                                                    text = "DIST",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            // Right: Distribution Legends list
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                distribution.forEachIndexed { index, item ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(CircleShape)
                                                                    .background(colors[index % colors.size])
                                                            )
                                                            Text(
                                                                text = item.name,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = textMuted,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        Text(
                                                            text = "${item.value.toInt()}",
                                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderCol, RoundedCornerShape(20.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = textMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "CHART DATA WILL POPULATE AS JOBS ARE ADDED FOR THIS ROLE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = textMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Key Insights & Briefing Content block
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, borderCol, RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "Key Insights & Market Analysis",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                val rawContent = report.content ?: report.excerpt ?: report.summary ?: "No details available."
                                // Clean up basic HTML tags if any (from server rich editor)
                                val cleanContent = remember(rawContent) {
                                    rawContent.replace(Regex("<[^>]*>"), "")
                                        .replace("&nbsp;", " ")
                                        .replace("&amp;", "&")
                                }

                                Text(
                                    text = cleanContent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }

                    // Active Placements List Section Header
                    item {
                        Text(
                            text = "ACTIVE PLACEMENTS FOR THIS BRIEFING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = textMuted,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    // Empty State or List of Live Jobs
                    if (jobs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "NO ACTIVE PLACEMENTS TRACKED IN THIS QUARTER",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = textMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(sortedJobs) { rj ->
                            val expired = (rj.active == false) || (!rj.expiresAt.isNullOrEmpty() && rj.expiresAt!! < todayStr)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.ensureRemoteJobSaved(rj) { localJobId ->
                                            onNavigateToJobDetail(localJobId)
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .animateContentSize()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rj.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (expired) Color.White.copy(alpha = 0.6f) else Color.White,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Surface(
                                            color = if (expired) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (expired) "EXPIRED" else "ACTIVE",
                                                color = if (expired) Color(0xFFF87171) else Color(0xFF34D399),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = textMuted, modifier = Modifier.size(13.dp))
                                        Text(text = rj.company, style = MaterialTheme.typography.bodySmall, color = if (expired) Color.White.copy(alpha = 0.6f) else Color.White)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = textMuted, modifier = Modifier.size(13.dp))
                                            Text(text = rj.location, style = MaterialTheme.typography.bodySmall, color = textMuted)
                                        }

                                        if (!rj.salary.isNullOrEmpty()) {
                                            Text(
                                                text = rj.salary!!,
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                fontWeight = FontWeight.Bold,
                                                color = if (expired) Color(0xFF34D399).copy(alpha = 0.6f) else Color(0xFF34D399)
                                            )
                                        }
                                    }

                                    // Display posted & expires dates if present
                                    val postedStr = rj.postedAt ?: rj.datePosted
                                    if (!postedStr.isNullOrEmpty() || !rj.expiresAt.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!postedStr.isNullOrEmpty()) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = textMuted, modifier = Modifier.size(12.dp))
                                                    Text(text = "Posted: $postedStr", style = MaterialTheme.typography.labelSmall, color = textMuted)
                                                }
                                            }
                                            if (!rj.expiresAt.isNullOrEmpty()) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = if (expired) Color(0xFFF87171) else Color(0xFF8B5CF6), modifier = Modifier.size(12.dp))
                                                    Text(
                                                        text = "${if (expired) "Expired" else "Expires"}: ${rj.expiresAt}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (expired) Color(0xFFF87171) else Color(0xFF8B5CF6),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            viewModel.ensureRemoteJobSaved(rj) { localJobId ->
                                                onNavigateToJobDetail(localJobId)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (expired) Color.White.copy(alpha = 0.1f) else accentBlue,
                                            contentColor = if (expired) textMuted else Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (expired) "Archived" else "Careers Portal", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Hiring Companies List block
                    if (companies.isNotEmpty()) {
                        item {
                            Text(
                                text = "HIRING COMPANIES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = textMuted,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }

                        items(companies) { company ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (company.name ?: "?").take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = company.name ?: "Unknown Company",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    if (!company.url.isNullOrEmpty()) {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    uriHandler.openUri(company.url!!)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Could not open career portal link", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Launch,
                                                contentDescription = "Open Website",
                                                tint = accentBlue
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Report Details Sidebar Info block
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, borderCol, RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Report Details",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                val details = listOf(
                                    "Role" to (report.role ?: "N/A"),
                                    "Period" to (report.monthYear ?: "N/A"),
                                    "Country" to (report.country ?: "Tanzania"),
                                    "Published" to (report.updatedAt?.take(10) ?: "N/A"),
                                    "Report ID" to (report.id?.take(8) ?: "JR-UNK")
                                )

                                details.forEach { (label, value) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = label, style = MaterialTheme.typography.bodySmall, color = textMuted)
                                        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
