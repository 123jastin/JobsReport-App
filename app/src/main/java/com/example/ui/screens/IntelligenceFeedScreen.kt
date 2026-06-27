package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun IntelligenceFeedScreen(
    viewModel: MainViewModel,
    onNavigateToJobs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    
    // Filtering indicators based on active country context
    val countryJobs = remember(allJobs, selectedCountry) {
        if (selectedCountry == "Worldwide") allJobs 
        else allJobs.filter { it.location.contains(selectedCountry, ignoreCase = true) }
    }

    var liveFeedCount by remember { mutableStateOf(128) }
    var activeCampaigns by remember { mutableStateOf(42) }
    
    // Simulate real-time stream counter increases
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            liveFeedCount += (1..3).random()
            if (Math.random() > 0.7) {
                activeCampaigns += (-1..1).random()
            }
        }
    }

    // Interactive custom simulated event logs dynamically using real jobs
    val customLogs = remember(countryJobs, selectedCountry) {
        val logs = mutableListOf<String>()
        if (countryJobs.isNotEmpty()) {
            countryJobs.take(3).forEach { job ->
                logs.add("💼 LIVE ROLE: ${job.company} is hiring a ${job.title} in ${job.location}.")
            }
            logs.add("🔋 TELEMETRY: Telecom recruiter activity surged by 12% in $selectedCountry region.")
            logs.add("🤖 AI ANALYSIS: Local sector seeks increased experience with Material 3 and Jetpack Compose.")
            logs.add("📈 TRENDING: Key competencies in $selectedCountry include Kotlin, Room, and Coroutines.")
        } else {
            logs.add("💼 INTELLIGENCE: Real-time telemetry feed active for $selectedCountry.")
            logs.add("🔋 TELEMETRY: System listening for incoming API jobs ingestion campaigns.")
            logs.add("🤖 AI ANALYSIS: Applicant matching engines analyzing local demand signals.")
        }
        logs
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep Dark Slate
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(100))
                                .background(Color(0xFF10B981)) // Glow Green
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REAL-TIME INTELLIGENCE MATRIX",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (selectedCountry == "Worldwide") "Global Hiring Telemetry" else "Telemetry: $selectedCountry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Aggregating ingestion metrics, deduplication indices, and live job vacancy telemetry from top employers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Real-Time System Metrics GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "INGESTED VAULT",
                    value = "$liveFeedCount",
                    sub = "Real-time records",
                    color = Color(0xFF3B82F6), // Blue
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ACTIVE RECRUITING",
                    value = "$activeCampaigns",
                    sub = "Live campaigns",
                    color = Color(0xFF8B5CF6), // Violet
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Feed Updates (Event logs)
        item {
            Text(
                text = "LIVE SYSTEM INGEST STREAM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        items(customLogs) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(100))
                            .background(Color(0xFF3B82F6))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Call To Action to View All Jobs
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNavigateToJobs,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("intel_view_jobs_btn")
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXPLORE ACTIVE FEED (${countryJobs.size} JOBS AVAILABLE)",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
        }
    }
}
