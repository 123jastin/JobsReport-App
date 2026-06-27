package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val company: String,
    val logoResName: String, // String representation of drawable res (e.g., "img_tech_logo")
    val location: String,
    val salary: String,
    val type: String, // Full-time, Part-time, Contract, etc.
    val workplace: String, // Remote, Hybrid, On-site
    val datePosted: String,
    val description: String,
    val requirements: String, // Comma-separated or bullet lines
    val benefits: String, // Comma-separated or bullet lines
    val isBookmarked: Boolean = false,
    val isApplied: Boolean = false,
    val status: String = "None", // "None", "Saved", "Applied", "Interviewing", "Offered", "Rejected"
    val notes: String = "",
    val category: String, // "Engineering", "Marketing", "Design", "Product Management", "Finance"
    val companyWebsite: String = "https://jobsreport.online",
    val active: Boolean = true,
    val remoteId: String = ""
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // Single-row constraint
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val targetTitle: String = "",
    val skills: String = "", // Comma-separated list of skills
    val experience: String = "", // Text summary of work experience
    val education: String = "", // Text summary of education
    val bio: String = "" // Professional summary
)
