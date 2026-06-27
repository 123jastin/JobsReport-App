package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobEntity
import com.example.viewmodel.MainViewModel

import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedJobsScreen(
    viewModel: MainViewModel,
    onJobClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val bookmarked by viewModel.bookmarkedJobs.collectAsState()
    val applied by viewModel.appliedJobs.collectAsState()
    val uriHandler = LocalUriHandler.current

    val tabs = listOf("Bookmarked (${bookmarked.size})", "Applied Tracker (${applied.size})")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Headers
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("tracker_tab_$index"),
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Content with elegant animations
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "TabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> BookmarkedList(bookmarked = bookmarked, onJobClick = onJobClick, onRemoveBookmark = { viewModel.toggleBookmark(it) })
                    1 -> AppliedTrackerList(applied = applied, onJobClick = onJobClick, onStatusChange = { job, status -> viewModel.updateJobStatus(job, status) })
                }
            }
        }

        // Professional red Post Job button
        Button(
            onClick = {
                try {
                    uriHandler.openUri("https://jobsreport.online/post-job")
                } catch (e: Exception) {
                    // fallback
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444), // Red
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp)
                .testTag("post_job_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Post Job",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun BookmarkedList(
    bookmarked: List<JobEntity>,
    onJobClick: (Int) -> Unit,
    onRemoveBookmark: (JobEntity) -> Unit
) {
    if (bookmarked.isEmpty()) {
        EmptyTrackerState(
            icon = Icons.Outlined.WorkOutline,
            title = "No Saved Jobs",
            subtitle = "Jobs you bookmark from the main board will appear here for easy offline tracking."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(bookmarked, key = { it.id }) { job ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onJobClick(job.id) }
                        .testTag("bookmarked_job_card_${job.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${job.company} • ${job.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Salary: ${job.salary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { onRemoveBookmark(job) },
                            modifier = Modifier.testTag("remove_bookmark_btn_${job.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkRemove,
                                contentDescription = "Remove bookmark",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppliedTrackerList(
    applied: List<JobEntity>,
    onJobClick: (Int) -> Unit,
    onStatusChange: (JobEntity, String) -> Unit
) {
    if (applied.isEmpty()) {
        EmptyTrackerState(
            icon = Icons.Outlined.CheckCircle,
            title = "No Applications Yet",
            subtitle = "When you click 'Quick Apply' on any position, we will move them here to organize your hiring stages."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(applied, key = { it.id }) { job ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onJobClick(job.id) }
                        .testTag("applied_job_card_${job.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = job.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = job.company,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Distinct dynamic badge representing pipeline status
                            Surface(
                                color = when (job.status) {
                                    "Interviewing" -> Color(0xFFFEF3C7) // soft amber
                                    "Offered" -> Color(0xFFD1FAE5) // soft green
                                    "Rejected" -> Color(0xFFFEE2E2) // soft red
                                    else -> Color(0xFFDBEAFE) // soft blue ("Applied")
                                },
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = job.status,
                                    color = when (job.status) {
                                        "Interviewing" -> Color(0xFFD97706)
                                        "Offered" -> Color(0xFF059669)
                                        "Rejected" -> Color(0xFFDC2626)
                                        else -> Color(0xFF2563EB)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (job.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✍️ Note: ${job.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive hiring stage selector row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hiring Stage:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val stages = listOf("Applied", "Interviewing", "Offered", "Rejected")
                                stages.forEach { stage ->
                                    val isSelected = job.status == stage
                                    val stageColor = when (stage) {
                                        "Interviewing" -> Color(0xFFF59E0B)
                                        "Offered" -> Color(0xFF10B981)
                                        "Rejected" -> Color(0xFFEF4444)
                                        else -> Color(0xFF3B82F6)
                                    }

                                    InputChip(
                                        selected = isSelected,
                                        onClick = { onStatusChange(job, stage) },
                                        label = { Text(stage, fontSize = 10.sp) },
                                        modifier = Modifier.testTag("stage_chip_${job.id}_$stage"),
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = stageColor,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTrackerState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
