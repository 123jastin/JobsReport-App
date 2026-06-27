package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobEntity
import com.example.viewmodel.MainViewModel

private data class LocationInfo(
    val name: String,
    val region: String,
    val postcode: String,
    val country: String
)

private val mockLocations = listOf(
    LocationInfo("Tanzania", "Dar es Salaam", "11101", "Tanzania"),
    LocationInfo("United States", "California", "94025", "United States"),
    LocationInfo("United Kingdom", "Greater London", "EC1A 1BB", "United Kingdom"),
    LocationInfo("Germany", "Berlin State", "10115", "Germany"),
    LocationInfo("Kenya", "Nairobi County", "00100", "Kenya"),
    LocationInfo("South Africa", "Western Cape", "8001", "South Africa")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionDetailScreen(
    viewModel: MainViewModel,
    regionName: String,
    onBackClick: () -> Unit,
    onJobClick: (Int) -> Unit,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allJobs by viewModel.allJobs.collectAsState()
    val liveLocations by viewModel.liveLocations.collectAsState()
    val isLocationsLoading by viewModel.isLocationsLoading.collectAsState()
    val isMarketLoading by viewModel.isMarketLoading.collectAsState()

    // Find matched location info
    val matchedLocation = remember(regionName, liveLocations) {
        val regionNameLower = regionName.lowercase().trim()
        val regionSlugLower = regionNameLower.replace(Regex("\\s+"), "-")
        liveLocations.find { loc ->
            val locNameLower = loc.name.lowercase().trim()
            locNameLower == regionNameLower || 
            locNameLower.replace(Regex("\\s+"), "-") == regionSlugLower
        } ?: com.example.network.RemoteLocation(
            name = regionName,
            country = when (regionNameLower) {
                "tanzania", "dar es salaam" -> "Tanzania"
                "united states", "california" -> "United States"
                "united kingdom", "greater london", "london" -> "United Kingdom"
                "germany", "berlin", "berlin state" -> "Germany"
                "kenya", "nairobi", "nairobi county" -> "Kenya"
                "south africa", "cape town", "western cape" -> "South Africa"
                else -> "Worldwide"
            },
            region = when (regionNameLower) {
                "tanzania", "dar es salaam" -> "Dar es Salaam"
                "united states", "california" -> "California"
                "united kingdom", "greater london", "london" -> "Greater London"
                "germany", "berlin", "berlin state" -> "Berlin State"
                "kenya", "nairobi", "nairobi county" -> "Nairobi County"
                "south africa", "cape town", "western cape" -> "Western Cape"
                else -> null
            },
            postcode = when (regionNameLower) {
                "tanzania", "dar es salaam" -> "11101"
                "united states", "california" -> "94025"
                "united kingdom", "greater london", "london" -> "EC1A 1BB"
                "germany", "berlin", "berlin state" -> "10115"
                "kenya", "nairobi", "nairobi county" -> "00100"
                "south africa", "cape town", "western cape" -> "8001"
                else -> null
            }
        )
    }

    // Filter jobs for this region
    val filteredJobs = remember(allJobs, regionName, matchedLocation) {
        val regionNameLower = regionName.lowercase().trim()
        val regionSlugLower = regionNameLower.replace(Regex("\\s+"), "-")
        val matchedRegionLower = matchedLocation?.region?.lowercase()?.trim()

        val matchedJobs = allJobs.filter { job ->
            val loc = job.location.lowercase().trim()
            loc.contains(regionNameLower) || 
                (matchedRegionLower != null && loc.contains(matchedRegionLower)) ||
                loc.replace(Regex("\\s+"), "-").contains(regionSlugLower) ||
                (regionNameLower == "united states" && (
                    loc.contains("ca") || loc.contains("ny") || loc.contains("tx") || 
                    loc.contains("wa") || loc.contains("il") || loc.contains("chicago") || 
                    loc.contains("san francisco") || loc.contains("austin") || loc.contains("seattle")
                )) ||
                (regionNameLower == "united kingdom" && (
                    loc.contains("london") || loc.contains("manchester") || loc.contains("uk")
                )) ||
                (regionNameLower == "germany" && (
                    loc.contains("berlin") || loc.contains("munich") || loc.contains("de")
                ))
        }

        // Sort by active jobs first (Active before Expired)
        matchedJobs.sortedWith(compareByDescending<JobEntity> { it.active })
    }

    val activeJobs = filteredJobs.filter { it.active }
    val expiredJobs = filteredJobs.filter { !it.active }
    val companiesCount = filteredJobs.map { it.company }.distinct().size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = regionName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(innerPadding)
                .testTag("region_detail_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Breadcrumbs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "HOME",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { onNavigateToDashboard() }
                    )
                    Text(text = "/", fontSize = 10.sp, color = Color(0xFF475569))
                    Text(
                        text = "REGIONS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B),
                        modifier = Modifier.clickable { onBackClick() }
                    )
                    Text(text = "/", fontSize = 10.sp, color = Color(0xFF475569))
                    Text(
                        text = regionName.uppercase(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Header Title Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "REGIONAL JOB MARKET",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B),
                                letterSpacing = 1.sp
                            )
                        }

                        if (isLocationsLoading || isMarketLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    color = Color(0xFFF59E0B),
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

                    Text(
                        text = "Jobs in $regionName",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )

                    val postcodeText = if (matchedLocation != null && !matchedLocation.postcode.isNullOrBlank()) " • Postcode: ${matchedLocation.postcode}" else ""
                    Text(
                        text = "Browse ${activeJobs.size} active job opportunities in $regionName$postcodeText.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // 3. Stats Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Active Jobs Stat
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(text = "${activeJobs.size}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text(text = "Active Jobs", color = Color(0xFF64748B), fontSize = 10.sp)
                            }
                        }
                    }

                    // Companies Stat
                    Card(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(text = "$companiesCount", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text(text = "Companies", color = Color(0xFF64748B), fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }

                    // Expired Stat (only if > 0)
                    if (expiredJobs.isNotEmpty()) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(text = "${expiredJobs.size}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    Text(text = "Expired", color = Color(0xFF64748B), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Top Display Ad Slot
            item {
                RegionAdBannerCard(
                    slotId = "4550717155",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 5. Jobs List or Empty State
            if (filteredJobs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "No Jobs in $regionName",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "No job listings are currently available for this region.",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                            ) {
                                Text("← Browse Regions", color = Color.White)
                            }
                            Button(
                                onClick = onNavigateToDashboard,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Text("View All Jobs →", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Active Jobs section
                if (activeJobs.isNotEmpty()) {
                    item {
                        Text(
                            text = "ACTIVE JOBS IN $regionName (${activeJobs.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFBBF24),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Render active jobs with custom in-feed ads after every 3 jobs
                    itemsIndexed(activeJobs) { index, job ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            JobRowItem(
                                job = job,
                                onClick = { onJobClick(job.id) }
                            )

                            // Show Alternating In-Feed Ads every 3 jobs
                            if ((index + 1) % 3 == 0 && index < activeJobs.size - 1) {
                                val adSlotId = when (((index + 1) / 3) % 3) {
                                    1 -> "1805968460"
                                    2 -> "9872160747"
                                    else -> "5598749525"
                                }
                                InFeedAdCard(slotId = adSlotId)
                            }
                        }
                    }
                }

                // Expired Jobs section
                if (expiredJobs.isNotEmpty()) {
                    item {
                        Text(
                            text = "EXPIRED LISTINGS (${expiredJobs.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    itemsIndexed(expiredJobs) { _, job ->
                        JobRowItem(
                            job = job,
                            onClick = { 
                                Toast.makeText(context, "This listing has expired.", Toast.LENGTH_SHORT).show()
                            },
                            isExpired = true
                        )
                    }
                }
            }

            // 6. Footer Display Ad Slot
            item {
                RegionAdBannerCard(
                    slotId = "5466053430",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun JobRowItem(
    job: JobEntity,
    onClick: () -> Unit,
    isExpired: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isExpired) Color(0xFF334155).copy(alpha = 0.2f) else Color(0xFF334155).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .alpha(if (isExpired) 0.5f else 1.0f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom adaptive logo container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = job.company.firstOrNull()?.toString()?.uppercase() ?: "?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Job metadata
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isExpired) {
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "EXPIRED",
                                color = Color(0xFFEF4444),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = job.type.uppercase(),
                                color = Color(0xFF60A5FA),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = job.workplace,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired) Color(0xFF64748B) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = job.company,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF475569),
                        fontSize = 12.sp
                    )
                    Text(
                        text = job.location,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (job.salary.isNotEmpty()) {
                    Text(
                        text = job.salary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RegionAdBannerCard(
    slotId: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.01f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "JOBSREPORT VERIFIED ADVERTISEMENT",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                letterSpacing = 1.sp
            )
            Text(
                text = "Secure Bridge Slot $slotId Active",
                fontSize = 8.sp,
                color = Color(0xFF334155),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun InFeedAdCard(
    slotId: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TELEMETRY IN-FEED SPONSOR",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6),
                    letterSpacing = 1.sp
                )
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "SAFE BRIDGE",
                        color = Color(0xFF10B981),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Verified Partner Ad",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Ads delivered via JobsReport Telemetry slot $slotId are end-to-end sandbox verified and scanned for secure redirection routing.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
