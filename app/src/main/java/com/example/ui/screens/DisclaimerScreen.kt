package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Disclaimer",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("disclaimer_back_button")
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
                .testTag("disclaimer_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Breadcrumb
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        text = "DISCLAIMER",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            // 2. Hero Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.alpha(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "LEGAL INFORMATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B),
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Disclaimer",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 42.sp
                    )

                    Text(
                        text = "Last Updated: June 14, 2026",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // 3. General Disclaimer Alert Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "General Disclaimer",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "The information provided on JobsReport.online is for general informational and educational purposes only. While we strive to keep the information accurate and up to date, we make no representations or warranties of any kind, express or implied, about the completeness, accuracy, reliability, suitability, or availability of the information contained on the website.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // 4. Detailed Sections
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    DisclaimerSection(
                        barColor = Color(0xFF3B82F6),
                        title = "Job Listings & Employment Information",
                        paragraphs = listOf(
                            "JobsReport.online publishes job vacancies, career opportunities, and employment-related information sourced from various channels including employer websites, recruitment portals, government announcements, and public notices. We do not guarantee that any job listing will still be available at the time of application, as employers may withdraw or modify vacancies without notice.",
                            "Users are strongly advised to verify all job details, requirements, deadlines, and application procedures directly with the respective employers or recruiting organizations before applying."
                        )
                    )

                    DisclaimerSection(
                        barColor = Color(0xFF8B5CF6),
                        title = "No Employment or Agency Relationship",
                        paragraphs = listOf(
                            "JobsReport.online is an independent information platform. We are not an employment agency, recruitment firm, or hiring organization. We do not employ, recommend, or endorse any job seeker, employer, or organization listed on our platform. Any communication, application, or interaction between users and employers is solely between those parties."
                        )
                    )

                    DisclaimerSection(
                        barColor = Color(0xFF10B981),
                        title = "Third-Party Links & External Websites",
                        paragraphs = listOf(
                            "Our website may contain links to external websites, employer career portals, application platforms, and third-party services. These links are provided for convenience and informational purposes only. We have no control over the content, availability, security, or privacy practices of external sites.",
                            "The inclusion of any link does not imply endorsement, recommendation, or approval by JobsReport.online. Users access external links at their own risk."
                        )
                    )

                    DisclaimerSection(
                        barColor = Color(0xFFF43F5E),
                        title = "No Guarantee of Results",
                        paragraphs = listOf(
                            "JobsReport.online does not guarantee that use of our website will result in employment, interviews, job offers, scholarships, admissions, or any other outcomes. Application outcomes depend entirely on the hiring decisions of respective employers and organizations."
                        )
                    )

                    DisclaimerSection(
                        barColor = Color(0xFFF59E0B),
                        title = "Financial & Payment Disclaimer",
                        paragraphs = listOf(
                            "JobsReport.online does not charge job seekers for accessing job listings or career information. We do not request, collect, or process payments related to job applications. Users should never pay money to any person or organization claiming to offer employment through our platform.",
                            "If you encounter any request for payment in connection with a job listing found on JobsReport.online, please report it immediately."
                        )
                    )
                }
            }

            // 5. Limitation of Liability Highlight Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Limitation of Liability",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "Under no circumstances shall JobsReport.online, its owners, operators, contributors, or affiliates be liable for any direct, indirect, incidental, consequential, special, or exemplary damages arising from or in connection with the use of this website or reliance on any information provided.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 19.sp
                            )
                            Text(
                                text = "This includes, but is not limited to, damages for loss of opportunities, loss of income, emotional distress, or any other losses resulting from:",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 19.sp
                            )
                            
                            val listItems = listOf(
                                "Use or inability to use the website",
                                "Reliance on information published on the platform",
                                "Errors, omissions, or inaccuracies in job listings",
                                "Links to third-party websites or services",
                                "Actions taken based on information found on this website"
                            )
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (bullet in listItems) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "•",
                                            color = Color(0xFFFBBF24),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = bullet,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Changes Section
            item {
                DisclaimerSection(
                    barColor = Color(0xFF06B6D4),
                    title = "Changes to This Disclaimer",
                    paragraphs = listOf(
                        "We reserve the right to update or modify this Disclaimer at any time without prior notice. Changes will be effective immediately upon posting. Continued use of the website after modifications constitutes acceptance of the updated Disclaimer."
                    )
                )
            }

            // 7. Questions & Contact CTA
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Questions About This Disclaimer?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "If you have questions, concerns, or require clarification regarding this Disclaimer, please contact us.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("disclaimer_contact_btn")
                        ) {
                            Text(
                                text = "BACK TO HOME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclaimerSection(
    barColor: Color,
    title: String,
    paragraphs: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .background(barColor)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        for (paragraph in paragraphs) {
            Text(
                text = paragraph,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 20.sp
            )
        }
    }
}
