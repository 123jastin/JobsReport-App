package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserProfileEntity
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val savedProfile by viewModel.userProfile.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Sync state with db loaded value
    LaunchedEffect(savedProfile) {
        name = savedProfile.fullName
        email = savedProfile.email
        phone = savedProfile.phone
        title = savedProfile.targetTitle
        skills = savedProfile.skills
        experience = savedProfile.experience
        education = savedProfile.education
        bio = savedProfile.bio
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val updatedProfile = UserProfileEntity(
                        id = 1,
                        fullName = name,
                        email = email,
                        phone = phone,
                        targetTitle = title,
                        skills = skills,
                        experience = experience,
                        education = education,
                        bio = bio
                    )
                    viewModel.saveProfile(updatedProfile)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Profile Synced & Ready for AI Tools! ✅",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                modifier = Modifier.testTag("save_profile_fab"),
                icon = { Icon(Icons.Default.Save, "Save") },
                text = { Text("Sync Profile") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Offline Resume Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fill in your background to enable instant AI cover letter composing, resume critiquing, and personalized interview practice templates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Section: Contact details
            ProfileSectionHeader(icon = Icons.Outlined.Person, title = "Personal Details")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                placeholder = { Text("e.g. Alex Mercer") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Target Professional Title") },
                placeholder = { Text("e.g. Senior Software Engineer") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_title_input"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("alex@mercer.io") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_email_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    placeholder = { Text("+1 (555) 0199") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_phone_input"),
                    singleLine = true
                )
            }

            // Section: Resume Core Details
            ProfileSectionHeader(icon = Icons.Outlined.ContactMail, title = "Skills & Profile")

            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Core Skills (Comma-separated)") },
                placeholder = { Text("Kotlin, Jetpack Compose, REST APIs, Room DB, Git, Python, SQL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_skills_input")
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Professional Summary / Bio") },
                placeholder = { Text("Enthusiastic and results-driven software professional with 3+ years experience designing fluid customer interfaces...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("profile_bio_input"),
                maxLines = 4
            )

            // Section: Work Experience and Education
            ProfileSectionHeader(icon = Icons.Outlined.HistoryEdu, title = "Experience & Education")

            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Work Experience") },
                placeholder = { Text("• Senior Mobile Dev at NovaTech (2024-Present): Lead Android compostable UI system overhaul.\n• Software Eng at ByteSized Corp (2022-2024): Built API integrations.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("profile_experience_input"),
                maxLines = 6
            )

            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                label = { Text("Education Summary") },
                placeholder = { Text("B.S. in Computer Science - University of Texas at Austin (2021)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_education_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(64.dp)) // Padding to stay clear of the FAB
        }
    }
}

@Composable
fun ProfileSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
