package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobEntity
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: MainViewModel,
    categorySlug: String,
    countrySlug: String?,
    onBackClick: () -> Unit,
    onJobClick: (Int) -> Unit,
    onNavigateToAiAdvisor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allJobs by viewModel.allJobs.collectAsState()
    val selectedCountryState by viewModel.selectedCountry.collectAsState()
    val isCategoryJobsLoading by viewModel.isCategoryJobsLoading.collectAsState()

    // 1. Resolve Category Name
    val categoryName = remember(categorySlug) {
        when (categorySlug.lowercase()) {
            "engineering" -> "Engineering"
            "design" -> "Design"
            "marketing" -> "Marketing"
            "product-management" -> "Product Management"
            "writing" -> "Writing"
            "customer-support" -> "Customer Support"
            "finance" -> "Finance"
            "hr", "human-resources" -> "Human Resources"
            else -> categorySlug.replace("-", " ").split(" ").joinToString(" ") {
                it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
            }
        }
    }

    LaunchedEffect(categoryName) {
        viewModel.fetchLiveCategoryJobs(categoryName)
    }

    // 2. Resolve Country Name (URL parameter takes priority, otherwise state)
    val countryName = remember(countrySlug, selectedCountryState) {
        if (!countrySlug.isNullOrEmpty()) {
            when (countrySlug.lowercase()) {
                "tanzania" -> "Tanzania"
                "united-states" -> "United States"
                "united-kingdom" -> "United Kingdom"
                "germany" -> "Germany"
                "kenya" -> "Kenya"
                "south-africa" -> "South Africa"
                else -> countrySlug.replace("-", " ").split(" ").joinToString(" ") {
                    it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
                }
            }
        } else if (selectedCountryState != "Worldwide") {
            selectedCountryState
        } else {
            ""
        }
    }

    val isWorldwide = countryName.isEmpty()

    // 3. Filter jobs by Category and Country
    val filteredJobs = remember(allJobs, categoryName, countryName, isWorldwide) {
        allJobs.filter { job ->
            val matchesCategory = job.category.equals(categoryName, ignoreCase = true)
            val matchesCountry = isWorldwide || job.location.contains(countryName, ignoreCase = true)
            matchesCategory && matchesCountry && job.active
        }
    }

    // 4. Pagination
    val jobsPerPage = 10
    var currentPage by remember { mutableStateOf(1) }
    // Reset page if filters change
    LaunchedEffect(categorySlug, countrySlug, selectedCountryState) {
        currentPage = 1
    }

    val totalPages = remember(filteredJobs) {
        val count = filteredJobs.size
        if (count == 0) 1 else kotlin.math.ceil(count.toDouble() / jobsPerPage).toInt()
    }

    val paginatedJobs = remember(filteredJobs, currentPage) {
        filteredJobs.drop((currentPage - 1) * jobsPerPage).take(jobsPerPage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isWorldwide) "$categoryName Jobs" else "$categoryName Jobs in $countryName",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("category_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0F172A),
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("category_screen_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Breadcrumbs
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HOME",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { onBackClick() }
                    )
                    Text(
                        text = "/",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "CATEGORY",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "/",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = categoryName.uppercase(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            // 2. Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isWorldwide) "${categoryName} Jobs" else "${categoryName} Jobs in ${countryName}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 34.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (isCategoryJobsLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = Color(0xFF3B82F6),
                                    strokeWidth = 1.5.dp
                                )
                                Text(
                                    text = "SYNCING...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF3B82F6),
                                    fontSize = 10.sp
                                )
                            }
                        } else {
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
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isWorldwide) {
                            "Browse ${filteredJobs.size} ${categoryName.lowercase()} jobs worldwide"
                        } else {
                            "Browse ${filteredJobs.size} ${categoryName.lowercase()} jobs in ${countryName}"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // 3. Ad Banner Component (Stylized high-quality visual placeholder)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .clickable { onNavigateToAiAdvisor() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2563EB).copy(alpha = 0.08f),
                                        Color(0xFF1D4ED8).copy(alpha = 0.02f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Advisor",
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "SPONSORED BY AI ADVISOR",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Ace your next $categoryName interview! Generate custom mock interview questions tailored for this category in seconds.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "Try AI Advisor Now →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Jobs List / Empty State
            if (filteredJobs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "No jobs found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isWorldwide) {
                                "No ${categoryName.lowercase()} jobs available right now."
                            } else {
                                "No ${categoryName.lowercase()} jobs available in ${countryName} right now."
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(paginatedJobs) { index, job ->
                    JobItemRow(
                        job = job,
                        onClick = { onJobClick(job.id) },
                        modifier = Modifier.testTag("category_job_item_${job.id}")
                    )
                }
            }

            // 5. Pagination controls
            if (totalPages > 1) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF3B82F6),
                                disabledContentColor = Color(0xFF475569)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "PREV",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Page $currentPage of $totalPages",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        TextButton(
                            onClick = { if (currentPage < totalPages) currentPage++ },
                            enabled = currentPage < totalPages,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF3B82F6),
                                disabledContentColor = Color(0xFF475569)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "NEXT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobItemRow(
    job: JobEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo placeholder matching other lists
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = job.company.firstOrNull()?.uppercase()?.toString() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Role Tag (e.g., Full-Time, Remote)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF3B82F6).copy(alpha = 0.12f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = (if (job.type.isNotEmpty()) job.type else "General").uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                        }

                        if (job.workplace.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFF10B981).copy(alpha = 0.12f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = job.workplace.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }

                    Text(
                        text = job.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Divider / Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Company",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = job.company,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (job.location.isNotEmpty()) job.location else "Remote",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (job.salary.isNotEmpty()) {
                Text(
                    text = job.salary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
