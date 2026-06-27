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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("privacy_policy_back_button")
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
                .testTag("privacy_policy_column"),
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
                        text = "PRIVACY POLICY",
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
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "SECURITY & DATA PROTECTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Privacy Policy",
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "JobsReport.online (\"we\", \"our\", or \"us\") values your privacy and is committed to protecting your personal information. This Privacy Policy explains how we collect, use, disclose, and safeguard information when you access or use our website, services, job listings, notifications, and related features.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 22.sp
                    )
                }
            }

            // 3. Information We Collect Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "1. Information We Collect",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Text(
                            text = "We may collect information directly from users, automatically through website usage, and from third-party service providers.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )

                        val collectItems = listOf(
                            "Name and email address when contacting us.",
                            "Browser type, operating system, and device information.",
                            "IP address and approximate geographic location.",
                            "Website usage statistics and analytics data.",
                            "Notification subscription preferences.",
                            "Information submitted through forms and feedback channels."
                        )

                        BulletList(items = collectItems)
                    }
                }
            }

            // 4. How We Use Information Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "2. How We Use Your Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = "We use collected information for legitimate business purposes, including:",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )

                        val useItems = listOf(
                            "Providing job listings and career opportunities.",
                            "Improving website functionality and user experience.",
                            "Sending job alerts and platform notifications.",
                            "Monitoring website performance and security.",
                            "Preventing abuse, fraud, and unauthorized access.",
                            "Complying with legal obligations."
                        )

                        BulletList(items = useItems)
                    }
                }
            }

            // 5. Remaining Detailed Sections
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PolicySection(
                        title = "3. Cookies and Tracking Technologies",
                        text = "JobsReport.online may use cookies, local storage, analytics tools, and similar technologies to improve performance, remember user preferences, analyze traffic patterns, and personalize content. Users can manage cookie preferences through browser settings."
                    )

                    PolicySection(
                        title = "4. Google AdSense and Advertising",
                        text = "We may display advertisements through Google AdSense and other advertising networks. These providers may use cookies and similar technologies to deliver relevant advertisements based on browsing behavior and interests. Third-party advertising partners may collect information according to their own privacy policies."
                    )

                    PolicySection(
                        title = "5. Analytics Services",
                        text = "We may use analytics tools, including Google Analytics, to understand visitor behavior, monitor website performance, and improve our services. Analytics providers may collect anonymized information about interactions with our website."
                    )

                    PolicySection(
                        title = "6. Push Notifications",
                        text = "If you subscribe to push notifications, we may send updates regarding job opportunities, career news, platform announcements, and related information. You may unsubscribe at any time through browser or device notification settings."
                    )

                    PolicySection(
                        title = "7. Third-Party Links",
                        text = "Job listings may contain links to external employer websites, application portals, and third-party services. We do not control and are not responsible for the privacy practices or content of those websites."
                    )

                    PolicySection(
                        title = "8. Data Security",
                        text = "We implement reasonable technical and organizational measures to protect information from unauthorized access, alteration, disclosure, or destruction. However, no online platform can guarantee absolute security."
                    )

                    PolicySection(
                        title = "9. Data Retention",
                        text = "Information is retained only for as long as necessary to fulfill the purposes described in this policy, comply with legal obligations, resolve disputes, and enforce agreements."
                    )

                    PolicySection(
                        title = "10. User Rights",
                        text = "Depending on applicable laws, users may have rights to request access to, correction of, or deletion of personal information. Requests may be submitted through our contact channels."
                    )

                    PolicySection(
                        title = "11. Children's Privacy",
                        text = "JobsReport.online is intended for general audiences seeking employment information and is not directed toward children under the age of 13. We do not knowingly collect personal information from children."
                    )

                    PolicySection(
                        title = "12. Changes to This Privacy Policy",
                        text = "We may update this Privacy Policy from time to time. Updates will be posted on this page together with the revised effective date. Continued use of the website after updates constitutes acceptance of the revised policy."
                    )

                    PolicySection(
                        title = "13. Contact Us",
                        text = "If you have questions regarding this Privacy Policy or your personal information, please contact us at jjovinatha@gmail.com."
                    )
                }
            }

            // 6. Go back CTA
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
                        Text(
                            text = "Your Privacy Matters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Thank you for reading our Privacy Policy. If you have any inquiries, feel free to email jjovinatha@gmail.com.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("privacy_policy_back_btn")
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
private fun BulletList(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (bullet in items) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "•",
                    color = Color(0xFF3B82F6),
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

@Composable
private fun PolicySection(
    title: String,
    text: String,
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
                    .background(Color(0xFF10B981))
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            lineHeight = 20.sp
        )
    }
}
