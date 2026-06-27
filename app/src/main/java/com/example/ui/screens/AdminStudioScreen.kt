package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun AdminStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isAdmin by viewModel.isAdmin.collectAsState()
    var passcode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        if (!isAdmin) {
            // Locked passcode screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ADMIN ESCORT ACCESS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Requires verified personnel authorization credentials.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = passcode,
                    onValueChange = { 
                        passcode = it
                        errorMessage = ""
                    },
                    label = { Text("System Access Passcode") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF10B981),
                        unfocusedLabelColor = Color(0xFF94A3B8),
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_passcode_field")
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (passcode.equals("admin", ignoreCase = true) || passcode == "12345") {
                            viewModel.setAdmin(true)
                            passcode = ""
                        } else {
                            errorMessage = "ACCESS DENIED: INVALD SIGNATURE."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_unlock_btn")
                ) {
                    Text("DECRYPT KEY & UNLOCK", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "HINT: ENTER 'admin' OR '12345' TO UNLOCK THE INGESTION ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            // Unlocked Admin Live Command Log Studio
            AdminDashboardConsole(
                onLock = { viewModel.setAdmin(false) }
            )
        }
    }
}

@Composable
fun AdminDashboardConsole(
    onLock: () -> Unit
) {
    var isIngesting by remember { mutableStateOf(true) }
    val logs = remember { mutableStateListOf<String>() }
    val lazyListState = rememberLazyListState()

    val baseLogs = listOf(
        "⚡ SYSTEM: Ingestion worker online. Scanning targets...",
        "🔌 WEBHOOK: Incoming sync from jobsreport.online (IP: 192.168.10.45)...",
        "🗄️ DATABASE: Connecting to Cloud Spanner partition...",
        "🔎 SCRAPER: Fetching SwahiliTech Dar es Salaam endpoints...",
        "📥 INGESTED: 'Mobile Developer' -> Deduped (MD5 matching ok).",
        "📈 METRICS: Recalculated index density: +3.4% engineering.",
        "🔑 AUTH: Sync signed with Admin Swahili SSL Certificate.",
        "📊 ANALYSIS: Parsing resume keywords using AI Studio NLP nodes.",
        "🤖 GEMINI: Optimizing cover letter engine weights.",
        "⚡ SYSTEM: Sync completed successfully. Sitting in standby."
    )

    // Simulate appending live streaming raw console logs
    LaunchedEffect(isIngesting) {
        if (isIngesting) {
            while (true) {
                delay(2000)
                val logText = baseLogs.random()
                val timestamp = System.currentTimeMillis() % 100000
                logs.add("[$timestamp] $logText")
                if (logs.size > 50) {
                    logs.removeAt(0)
                }
                // Auto-scroll to bottom of terminal
                if (logs.isNotEmpty()) {
                    lazyListState.animateScrollToItem(logs.size - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Console Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Console",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INGESTION CONSOLE v2.4",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = onLock,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("admin_lock_btn")
            ) {
                Text("LOCK ENGINE", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // System Control Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INGESTION ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isIngesting) "STATUS: RUNNING" else "STATUS: PAUSED",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIngesting) Color(0xFF10B981) else Color(0xFFF59E0B),
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = { isIngesting = !isIngesting },
                modifier = Modifier.testTag("toggle_ingest_btn")
            ) {
                Icon(
                    imageVector = if (isIngesting) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isIngesting) "Pause" else "Run",
                    tint = if (isIngesting) Color(0xFFEF4444) else Color(0xFF10B981)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LIVE TELEMETRY STREAM",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B),
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Terminal Log Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            if (logs.isEmpty()) {
                Text(
                    text = "Initializing terminal socket...\nListening for secure triggers on webhook 3000...",
                    color = Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
