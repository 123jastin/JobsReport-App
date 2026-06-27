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
import com.example.viewmodel.MainViewModel

data class RegionItem(
    val name: String,
    val flag: String,
    val activeJobsCount: Int,
    val totalJobsCount: Int,
    val description: String,
    val primaryDomain: String,
    val status: String,
    val country: String,
    val countrySlug: String,
    val slug: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionsScreen(
    viewModel: MainViewModel,
    onRegionClick: (String) -> Unit,
    onCountryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allJobs by viewModel.allJobs.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    var searchTerm by remember { mutableStateOf("") }

    val liveLocations by viewModel.liveLocations.collectAsState()
    val isLocationsLoading by viewModel.isLocationsLoading.collectAsState()

    // Dynamically calculate counts based on the current database state & live locations from endpoint
    val regions = remember(allJobs, liveLocations) {
        if (liveLocations.isNotEmpty()) {
            val uniqueRegions = mutableMapOf<String, com.example.network.RemoteLocation>()
            liveLocations.forEach { loc ->
                val key = loc.name.lowercase().trim()
                if (!uniqueRegions.containsKey(key)) {
                    uniqueRegions[key] = loc
                }
            }
            
            uniqueRegions.values.map { loc ->
                val regionName = loc.name.lowercase().trim()
                val matchingJobs = allJobs.filter { 
                    val jobLoc = it.location.lowercase()
                    jobLoc.contains(regionName)
                }
                val activeJobsCount = matchingJobs.count { it.active }
                val totalJobsCount = matchingJobs.size
                
                val flag = when (loc.country.lowercase().trim()) {
                    "tanzania" -> "🇹🇿"
                    "kenya" -> "🇰🇪"
                    "united states", "usa", "us" -> "🇺🇸"
                    "united kingdom", "uk" -> "🇬🇧"
                    "germany" -> "🇩🇪"
                    "south africa" -> "🇿🇦"
                    else -> "🌍"
                }
                
                val cleanCountry = loc.country.trim()
                val cleanRegionName = loc.name.trim()
                val countrySlug = cleanCountry.lowercase().replace(Regex("\\s+"), "-")
                val slug = cleanRegionName.lowercase().replace(Regex("\\s+"), "-")
                val primaryDomain = "jobsreport.online/${cleanCountry.lowercase().replace(Regex("\\s+"), "")}"
                
                val description = when (cleanCountry.lowercase()) {
                    "tanzania" -> "East Africa Digital Hub & fintech innovation surge center."
                    "united states" -> "High-growth Silicon Valley, Austin, & Seattle hubs."
                    "united kingdom" -> "London tech corridor & remote contract clusters."
                    "germany" -> "Berlin fintech ecosystems and industrial software."
                    "kenya" -> "Nairobi tech ecosystem & mobile banking surge."
                    "south africa" -> "Cape Town creative clusters & digital telemetry."
                    else -> "International tech talent & regional employment hubs in $cleanCountry."
                }
                
                RegionItem(
                    name = cleanRegionName,
                    flag = flag,
                    activeJobsCount = activeJobsCount,
                    totalJobsCount = totalJobsCount,
                    description = description,
                    primaryDomain = primaryDomain,
                    status = if (activeJobsCount > 0) "ACTIVE SYNC" else "STANDBY",
                    country = cleanCountry,
                    countrySlug = countrySlug,
                    slug = slug
                )
            }
        } else {
            val tzCount = allJobs.count { it.active && (it.location.contains("Tanzania", ignoreCase = true) || it.location.contains("Dar es Salaam", ignoreCase = true)) }
            val usCount = allJobs.count { it.active && (it.location.contains("United States", ignoreCase = true) || it.location.contains("CA") || it.location.contains("NY") || it.location.contains("TX") || it.location.contains("WA") || it.location.contains("IL") || it.location.contains("chicago") || it.location.contains("san francisco") || it.location.contains("austin") || it.location.contains("seattle")) }
            val ukCount = allJobs.count { it.active && (it.location.contains("United Kingdom", ignoreCase = true) || it.location.contains("London", ignoreCase = true) || it.location.contains("Manchester", ignoreCase = true) || it.location.contains("uk", ignoreCase = true)) }
            val deCount = allJobs.count { it.active && (it.location.contains("Germany", ignoreCase = true) || it.location.contains("Berlin", ignoreCase = true) || it.location.contains("Munich", ignoreCase = true) || it.location.contains("de", ignoreCase = true)) }
            val keCount = allJobs.count { it.active && (it.location.contains("Kenya", ignoreCase = true) || it.location.contains("Nairobi", ignoreCase = true)) }
            val zaCount = allJobs.count { it.active && (it.location.contains("South Africa", ignoreCase = true) || it.location.contains("Cape Town", ignoreCase = true)) }

            val tzTotal = allJobs.count { it.location.contains("Tanzania", ignoreCase = true) || it.location.contains("Dar es Salaam", ignoreCase = true) }
            val usTotal = allJobs.count { it.location.contains("United States", ignoreCase = true) || it.location.contains("CA") || it.location.contains("NY") || it.location.contains("TX") || it.location.contains("WA") || it.location.contains("IL") || it.location.contains("chicago") || it.location.contains("san francisco") || it.location.contains("austin") || it.location.contains("seattle") }
            val ukTotal = allJobs.count { it.location.contains("United Kingdom", ignoreCase = true) || it.location.contains("London", ignoreCase = true) || it.location.contains("Manchester", ignoreCase = true) || it.location.contains("uk", ignoreCase = true) }
            val deTotal = allJobs.count { it.location.contains("Germany", ignoreCase = true) || it.location.contains("Berlin", ignoreCase = true) || it.location.contains("Munich", ignoreCase = true) || it.location.contains("de", ignoreCase = true) }
            val keTotal = allJobs.count { it.location.contains("Kenya", ignoreCase = true) || it.location.contains("Nairobi", ignoreCase = true) }
            val zaTotal = allJobs.count { it.location.contains("South Africa", ignoreCase = true) || it.location.contains("Cape Town", ignoreCase = true) }

            listOf(
                RegionItem("Tanzania", "🇹🇿", tzCount, tzTotal, "East Africa Digital Hub & fintech innovation surge center.", "jobsreport.online/tz", if (tzCount > 0) "ACTIVE SYNC" else "STANDBY", "Tanzania", "tanzania", "tanzania"),
                RegionItem("United States", "🇺🇸", usCount, usTotal, "High-growth Silicon Valley, Austin, & Seattle hubs.", "jobsreport.online/us", if (usCount > 0) "ACTIVE SYNC" else "STANDBY", "United States", "united-states", "united-states"),
                RegionItem("United Kingdom", "🇬🇧", ukCount, ukTotal, "London tech corridor & remote contract clusters.", "jobsreport.online/uk", if (ukCount > 0) "ACTIVE SYNC" else "STANDBY", "United Kingdom", "united-kingdom", "united-kingdom"),
                RegionItem("Germany", "🇩🇪", deCount, deTotal, "Berlin fintech ecosystems and industrial software.", "jobsreport.online/de", if (deCount > 0) "ACTIVE SYNC" else "STANDBY", "Germany", "germany", "germany"),
                RegionItem("Kenya", "🇰🇪", keCount, keTotal, "Nairobi tech ecosystem & mobile banking surge.", "jobsreport.online/ke", if (keCount > 0) "ACTIVE SYNC" else "STANDBY", "Kenya", "kenya", "kenya"),
                RegionItem("South Africa", "🇿🇦", zaCount, zaTotal, "Cape Town creative clusters & digital telemetry.", "jobsreport.online/za", if (zaCount > 0) "ACTIVE SYNC" else "STANDBY", "South Africa", "south-africa", "south-africa")
            )
        }
    }

    // Filter locations by selectedCountry if it is not "Worldwide"
    val countryFilteredRegions = remember(regions, selectedCountry) {
        if (selectedCountry == "Worldwide") {
            regions
        } else {
            regions.filter { it.country.equals(selectedCountry, ignoreCase = true) }
        }
    }

    // Filter regions by search term
    val filteredRegions = remember(countryFilteredRegions, searchTerm) {
        if (searchTerm.isEmpty()) {
            countryFilteredRegions
        } else {
            countryFilteredRegions.filter {
                it.name.contains(searchTerm, ignoreCase = true) ||
                it.country.contains(searchTerm, ignoreCase = true)
            }
        }
    }

    // Separate regions with jobs vs without jobs
    val regionsWithJobs = remember(filteredRegions) {
        filteredRegions.filter { it.totalJobsCount > 0 }
    }
    val regionsWithoutJobs = remember(filteredRegions) {
        filteredRegions.filter { it.totalJobsCount == 0 }
    }

    // Compute Metrics
    val totalActiveJobs = remember(regionsWithJobs) {
        regionsWithJobs.sumOf { it.activeJobsCount }
    }
    val locationsWithJobs = remember(regionsWithJobs) {
        regionsWithJobs.size
    }
    val totalLocations = remember(filteredRegions) {
        filteredRegions.size
    }

    val groupedByCountry = remember(regionsWithJobs) {
        regionsWithJobs.groupBy { it.country }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("regions_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title section
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
                            text = "REGIONAL JOB EXPLORER",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            letterSpacing = 1.sp
                        )
                    }

                    if (isLocationsLoading) {
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
                    text = if (selectedCountry == "Worldwide") "Jobs by City & Region" else "Jobs by Region in $selectedCountry",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = if (selectedCountry == "Worldwide") {
                        "Browse job opportunities across $locationsWithJobs cities and regions worldwide. $totalActiveJobs active jobs available."
                    } else {
                        "Browse job opportunities across $locationsWithJobs regions in $selectedCountry. $totalActiveJobs active jobs available."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // 2. Active filter banner if not Worldwide
        if (selectedCountry != "Worldwide") {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setSelectedCountry("Worldwide") }
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Filter: $selectedCountry active. Tap to reset to Worldwide.",
                            color = Color(0xFF60A5FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Filter",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Regions
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Text(text = "Regions", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "$locationsWithJobs", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                    }
                }

                // Active Jobs
                Card(
                    modifier = Modifier.weight(1.1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                            Text(text = "Active Jobs", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Text(text = "$totalActiveJobs", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                    }
                }

                // Total Locations
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            Text(text = "Locations", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "$totalLocations", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                    }
                }
            }
        }

        // 4. Ad Banner top
        item {
            RegionAdBannerCard(slotId = "4550717155")
        }

        // 5. Search field
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("regions_search_input"),
                placeholder = {
                    Text(
                        text = if (selectedCountry == "Worldwide") "Search cities or countries..." else "Search regions in $selectedCountry...",
                        color = Color(0xFF64748B)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = Color(0xFF475569)
                    )
                },
                trailingIcon = {
                    if (searchTerm.isNotEmpty()) {
                        IconButton(onClick = { searchTerm = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.2f),
                    focusedBorderColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
                    unfocusedBorderColor = Color(0xFF334155).copy(alpha = 0.8f),
                    cursorColor = Color(0xFFF59E0B)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Empty Search State
        if (filteredRegions.isEmpty()) {
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
                        text = "No Regions Found",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "No locations matched your search criteria.",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Grouped by country if "Worldwide" is active
        if (selectedCountry == "Worldwide" && regionsWithJobs.isNotEmpty()) {
            groupedByCountry.forEach { (country, countryRegions) ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val firstFlag = countryRegions.firstOrNull()?.flag ?: "🌍"
                        Text(text = firstFlag, fontSize = 18.sp)
                        Text(
                            text = country.uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = "${countryRegions.size} REGION${if (countryRegions.size > 1) "S" else ""}",
                                color = Color(0xFF60A5FA),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        val firstSlug = countryRegions.firstOrNull()?.countrySlug ?: ""
                        if (firstSlug.isNotEmpty()) {
                            Text(
                                text = "View Country →",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier
                                    .clickable { onCountryClick(firstSlug) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                itemsIndexed(countryRegions) { index, region ->
                    val isRegionActive = region.activeJobsCount > 0
                    RegionRowCard(
                        region = region,
                        onClick = {
                            viewModel.setSelectedCountry(region.country)
                            onRegionClick(region.name)
                        }
                    )

                    // Show sponsor in-feed slot after every 3 items
                    if ((index + 1) % 3 == 0 && index < countryRegions.size - 1) {
                        val adSlotId = when (((index + 1) / 3) % 3) {
                            1 -> "1805968460"
                            2 -> "9872160747"
                            else -> "5598749525"
                        }
                        InFeedAdCard(slotId = adSlotId)
                    }
                }
            }
        } else if (regionsWithJobs.isNotEmpty()) {
            // Flat list for filtered or single country
            itemsIndexed(regionsWithJobs) { index, region ->
                RegionRowCard(
                    region = region,
                    onClick = {
                        onRegionClick(region.name)
                    }
                )

                // Show sponsor in-feed slot after every 3 items
                if ((index + 1) % 3 == 0 && index < regionsWithJobs.size - 1) {
                    val adSlotId = when (((index + 1) / 3) % 3) {
                        1 -> "1805968460"
                        2 -> "9872160747"
                        else -> "5598749525"
                    }
                    InFeedAdCard(slotId = adSlotId)
                }
            }
        }

        // Standby locations with no jobs (Other Locations)
        if (regionsWithoutJobs.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "OTHER LOCATIONS (${regionsWithoutJobs.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Standby Network • No active jobs",
                            fontSize = 9.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    // Display chips for standby regions
                    OptInLayoutStandbyChips(
                        regions = regionsWithoutJobs,
                        onRegionClick = { region ->
                            viewModel.setSelectedCountry(region.country)
                            onRegionClick(region.name)
                        }
                    )
                }
            }
        }

        // 6. Ad Banner bottom
        item {
            RegionAdBannerCard(slotId = "5466053430")
        }
    }
}

@Composable
private fun RegionRowCard(
    region: RegionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSyncActive = region.activeJobsCount > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSyncActive) Color(0xFF3B82F6).copy(alpha = 0.3f) else Color(0xFF334155).copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
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
            // Rounded background circle for Flag
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = region.flag,
                    fontSize = 22.sp
                )
            }

            // Region details
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = region.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Surface(
                        color = if (isSyncActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = region.status,
                            color = if (isSyncActive) Color(0xFF34D399) else Color(0xFF94A3B8),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = region.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                ) {
                    Text(
                        text = "🌐 ${region.primaryDomain}",
                        fontSize = 11.sp,
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF475569),
                        fontSize = 11.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = if (isSyncActive) Color(0xFF34D399) else Color(0xFF64748B),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${region.activeJobsCount} Active",
                            fontSize = 11.sp,
                            color = if (isSyncActive) Color(0xFF34D399) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                        if (region.totalJobsCount > region.activeJobsCount) {
                            Text(
                                text = "(${region.totalJobsCount} total)",
                                fontSize = 10.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate to region",
                tint = Color(0xFF475569),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun OptInLayoutStandbyChips(
    regions: List<RegionItem>,
    onRegionClick: (RegionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Render standby chips in a flowing list structure using standard Rows of chunks (since FlowRow requires external libraries in older Compose, chucking works perfectly)
    val chunked = remember(regions) { regions.chunked(2) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (chunk in chunked) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (region in chunk) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(0.45f)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .clickable { onRegionClick(region) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = region.flag, fontSize = 14.sp)
                            Text(
                                text = region.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                // spacer padding for last incomplete row
                if (chunk.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
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
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.01f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
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
