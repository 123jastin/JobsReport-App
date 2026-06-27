package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobEntity
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToJobs: () -> Unit,
    onJobClick: (Int) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToRegions: () -> Unit,
    onNavigateToCompanies: () -> Unit,
    onNavigateToAboutUs: () -> Unit = {},
    onNavigateToContactUs: () -> Unit = {},
    onNavigateToDisclaimer: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allJobs by viewModel.allJobs.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val filteredJobs by viewModel.filteredJobs.collectAsState()

    val liveCategories by viewModel.liveCategories.collectAsState()
    val isCategoriesLoading by viewModel.isCategoriesLoading.collectAsState()
    val isHomeDataLoading by viewModel.isHomeDataLoading.collectAsState()
    val liveHomeReports by viewModel.liveHomeReports.collectAsState()
    val liveHomeSpotlight by viewModel.liveHomeSpotlight.collectAsState()

    var showAllCategories by remember { mutableStateOf(false) }

    // Dynamic country-specific metadata matching React logic
    val currentFlag = when (selectedCountry) {
        "Tanzania" -> "🇹🇿"
        "United States" -> "🇺🇸"
        "United Kingdom" -> "🇬🇧"
        "Germany" -> "🇩🇪"
        "Kenya" -> "🇰🇪"
        "South Africa" -> "🇿🇦"
        else -> "🌍"
    }

    // Categories details mapping with icons
    val categoryMetadata = listOf(
        CategoryMeta("Engineering", "engineering", Icons.Default.Code, Color(0xFF3B82F6)),
        CategoryMeta("Design", "design", Icons.Default.Palette, Color(0xFFEC4899)),
        CategoryMeta("Marketing", "marketing", Icons.Default.TrendingUp, Color(0xFF10B981)),
        CategoryMeta("Product Management", "product-management", Icons.Default.Assessment, Color(0xFF8B5CF6)),
        CategoryMeta("Writing", "writing", Icons.Default.Edit, Color(0xFFF59E0B)),
        CategoryMeta("Customer Support", "customer-support", Icons.Default.Headset, Color(0xFF06B6D4)),
        CategoryMeta("Finance", "finance", Icons.Default.AccountBalance, Color(0xFFE11D48)),
        CategoryMeta("Human Resources", "hr", Icons.Default.People, Color(0xFF14B8A6))
    )

    // Calculate dynamic job counts per category from Database combined with Live Categories
    val categoriesWithCounts = remember(allJobs, liveCategories) {
        if (liveCategories.isNotEmpty()) {
            liveCategories.map { lc ->
                val matchingMeta = categoryMetadata.find { it.name.equals(lc.name, ignoreCase = true) || it.slug.equals(lc.slug, ignoreCase = true) }
                CategoryMeta(
                    name = lc.name,
                    slug = lc.slug ?: lc.name.lowercase().replace(" ", "-"),
                    icon = matchingMeta?.icon ?: Icons.Default.Work,
                    tint = matchingMeta?.tint ?: Color(0xFF3B82F6),
                    jobCount = lc.jobCount ?: lc.jobsCount ?: lc.count ?: allJobs.count { it.category.equals(lc.name, ignoreCase = true) }
                )
            }
        } else {
            categoryMetadata.map { meta ->
                val count = allJobs.count { it.category.equals(meta.name, ignoreCase = true) }
                meta.copy(jobCount = count)
            }
        }
    }

    val visibleCategories = if (showAllCategories) {
        categoriesWithCounts
    } else {
        categoriesWithCounts.take(6)
    }

    // Latest filtered jobs (take first 5)
    val displayJobs = remember(filteredJobs) {
        filteredJobs.take(5)
    }

    // Dynamic counts
    val categoriesCount = categoriesWithCounts.size
    val activeReportsCount = remember(liveHomeReports) {
        liveHomeReports.size
    }
    val activeJobsCount = filteredJobs.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep Slate Dark
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. HERO HEADER SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Pulsing Talent Intelligence Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REAL-TIME TALENT INTELLIGENCE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    if (isHomeDataLoading || isCategoriesLoading) {
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
                                text = "SYNCING...",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF94A3B8),
                                fontSize = 9.sp
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
                                text = "LIVE SYNC",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF10B981),
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Dynamic Header Title
                Text(
                    text = if (selectedCountry == "Worldwide") {
                        "Find Your Next\nCareer Opportunity."
                    } else {
                        "Jobs in $selectedCountry\n$currentFlag Latest Vacancies."
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 38.sp,
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hero Description
                Text(
                    text = if (selectedCountry == "Worldwide") {
                        "Insight-first job discovery. We aggregate real-time market data to show you where the demand is actually shifting."
                    } else {
                        "Find the latest jobs and career opportunities in $selectedCountry. Browse verified vacancies from top employers hiring in $selectedCountry."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF94A3B8),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Grid Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        icon = Icons.Default.FlashOn,
                        count = "$categoriesCount",
                        label = "Categories",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        icon = Icons.Default.BarChart,
                        count = "$activeReportsCount",
                        label = "Reports",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        icon = Icons.Default.Business,
                        count = "$activeJobsCount",
                        label = "Active Jobs",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. JOB CATEGORIES SECTION
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(16.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Job Categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (showAllCategories) "Showing all $categoriesCount categories" else "Top categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (categoriesCount > 6) {
                        TextButton(
                            onClick = { showAllCategories = !showAllCategories },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B82F6))
                        ) {
                            Text(
                                text = if (showAllCategories) "SHOW LESS" else "SEE MORE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (showAllCategories) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Grid using FlowRow (or Column + Rows)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chunks = visibleCategories.chunked(2)
                    chunks.forEach { rowMetaList ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMetaList.forEach { meta ->
                                Card(
                                    onClick = {
                                        onCategoryClick(meta.slug)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            Color(0xFF334155).copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .testTag("home_cat_${meta.slug}"),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(meta.tint.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = meta.icon,
                                                contentDescription = meta.name,
                                                tint = meta.tint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = meta.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${meta.jobCount} active roles",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            // Filler if single item in row
                            if (rowMetaList.size == 1) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 3. LATEST OPPORTUNITIES SECTION
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(16.dp)
                                    .background(Color(0xFF3B82F6))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Latest Opportunities",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (selectedCountry == "Worldwide") {
                                "Top active job listings globally"
                            } else {
                                "Latest vacancies in $selectedCountry"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    TextButton(
                        onClick = onNavigateToJobs,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF3B82F6))
                    ) {
                        Text("VIEW ALL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (displayJobs.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No opportunities found", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Try switching your location or refining your query.", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayJobs.forEach { job ->
                            Card(
                                onClick = { onJobClick(job.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .testTag("home_job_${job.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circular Logo Placeholder
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF334155)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = job.company.take(2).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = Color(0xFF3B82F6).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = job.category.uppercase(),
                                                color = Color(0xFF60A5FA),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 8.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = job.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = job.company,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Text(
                                                text = "•",
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = job.location,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "View details",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. LATEST REPORTS SECTION LINK
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { onNavigateToReports() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ANALYTICAL REPORTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA78BFA),
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Market Intelligence Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Read detailed analyses and review live skill metrics in East Africa and globally.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "View Reports",
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 5. EXPLORE JOBS BY COUNTRY SECTION
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(16.dp)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Explore Jobs by Country",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val countries = listOf(
                    CountryInfo("Worldwide", "🌍", Color(0xFF3B82F6)),
                    CountryInfo("Tanzania", "🇹🇿", Color(0xFF10B981)),
                    CountryInfo("United States", "🇺🇸", Color(0xFF3B82F6)),
                    CountryInfo("United Kingdom", "🇬🇧", Color(0xFFEF4444)),
                    CountryInfo("Germany", "🇩🇪", Color(0xFFF59E0B)),
                    CountryInfo("Kenya", "🇰🇪", Color(0xFF10B981)),
                    CountryInfo("South Africa", "🇿🇦", Color(0xFFEF4444))
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(countries) { country ->
                        val isSelected = selectedCountry == country.name
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isSelected) {
                                country.tint.copy(alpha = 0.2f)
                            } else {
                                Color(0xFF1E293B)
                            },
                            border = borderStroke(
                                width = 1.dp,
                                color = if (isSelected) country.tint.copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clickable { viewModel.setSelectedCountry(country.name) }
                                .testTag("home_country_${country.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = country.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = country.name,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .clickable { onNavigateToRegions() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Browse Jobs by City & Region",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 6. WEEKLY SPOTLIGHT SECTION
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weekly Spotlight",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = "LIVE MARKET DATA",
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Top companies actively shifting their hiring strategy based on market telemetry.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Spotlight tag cloud clickable to filter companies
                    val spotlightCompanies = remember(liveHomeSpotlight) {
                        if (liveHomeSpotlight.isNotEmpty()) {
                            liveHomeSpotlight
                        } else {
                            listOf(
                                "SwahiliTech Solutions",
                                "NMB Bank Tanzania",
                                "NovaTech Solutions",
                                "Aether Creative Agency"
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(spotlightCompanies) { company ->
                            Text(
                                text = company.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setSearchQuery(company)
                                        onNavigateToJobs()
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 7. FOOTER SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Divider(color = Color(0xFF334155).copy(alpha = 0.5f))

                // Footer Links Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "QUICK LINKS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        FooterLink("About Us", onClick = onNavigateToAboutUs)
                        FooterLink("Contact Us", onClick = onNavigateToContactUs)
                        FooterLink("Companies", onClick = onNavigateToCompanies)
                        FooterLink("Job Regions", onClick = onNavigateToRegions)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LEGAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        FooterLink("Privacy Policy", onClick = onNavigateToPrivacyPolicy)
                        FooterLink("Terms of Service", onClick = onNavigateToTermsOfService)
                        FooterLink("Disclaimer", onClick = onNavigateToDisclaimer)
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CONNECT WITH US",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialChip("WhatsApp", Icons.Default.Message, Color(0xFF10B981), onClick = {})
                        SocialChip("Facebook", Icons.Default.Flag, Color(0xFF3B82F6), onClick = {})
                        SocialChip("Email Us", Icons.Default.Email, Color(0xFF8B5CF6), onClick = {})
                    }
                }

                // Brand text
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(
                        text = "JobsReport.online",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Real-time job market intelligence platform. Helping job seekers discover employment opportunities across Tanzania and beyond.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "© 2026 JobsReport",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF475569),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    icon: ImageVector,
    count: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FooterLink(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = Color(0xFF64748B),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    )
}

@Composable
fun SocialChip(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier
            .clickable { onClick() }
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

data class CategoryMeta(
    val name: String,
    val slug: String,
    val icon: ImageVector,
    val tint: Color,
    val jobCount: Int = 0
)

data class CountryInfo(
    val name: String,
    val flag: String,
    val tint: Color
)
