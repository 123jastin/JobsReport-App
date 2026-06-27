package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiHelper
import com.example.network.RemoteCompany
import com.example.network.CompaniesRetrofitClient
import com.example.network.RemoteJob
import com.example.network.RemoteJobDetail
import com.example.network.MarketRetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val repository: JobRepository) : AndroidViewModel(application) {

    // Filter and search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedWorkplace = MutableStateFlow("All")
    val selectedWorkplace: StateFlow<String> = _selectedWorkplace.asStateFlow()

    private val _selectedJobType = MutableStateFlow("All")
    val selectedJobType: StateFlow<String> = _selectedJobType.asStateFlow()

    private val _selectedCountry = MutableStateFlow("Worldwide")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    // DB states
    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .map { it ?: UserProfileEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )

    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered Jobs State
    val filteredJobs: StateFlow<List<JobEntity>> = combine(
        allJobs, _searchQuery, _selectedCategory, _selectedWorkplace, _selectedJobType
    ) { jobs, query, category, workplace, jobType ->
        jobs.filter { job ->
            val matchesQuery = query.isEmpty() ||
                    job.title.contains(query, ignoreCase = true) ||
                    job.company.contains(query, ignoreCase = true) ||
                    job.description.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || job.category.equals(category, ignoreCase = true)
            val matchesWorkplace = workplace == "All" || job.workplace.equals(workplace, ignoreCase = true)
            val matchesJobType = jobType == "All" || job.type.equals(jobType, ignoreCase = true)

            matchesQuery && matchesCategory && matchesWorkplace && matchesJobType
        }
    }.combine(_selectedCountry) { list, country ->
        list.filter { job ->
            country == "Worldwide" || 
                    job.location.contains(country, ignoreCase = true) ||
                    (country == "Tanzania" && (job.location.contains("Tanzania", ignoreCase = true) || job.location.contains("Dar es Salaam", ignoreCase = true)))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Bookmarked and Applied jobs counts and listings
    val bookmarkedJobs: StateFlow<List<JobEntity>> = allJobs
        .map { list -> list.filter { it.isBookmarked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appliedJobs: StateFlow<List<JobEntity>> = allJobs
        .map { list -> list.filter { it.isApplied } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI States
    private val _aiOutput = MutableStateFlow("")
    val aiOutput: StateFlow<String> = _aiOutput.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Live Companies API States
    private val _liveCompanies = MutableStateFlow<List<RemoteCompany>?>(null)
    val liveCompanies: StateFlow<List<RemoteCompany>?> = _liveCompanies.asStateFlow()

    private val _isCompaniesLoading = MutableStateFlow(false)
    val isCompaniesLoading: StateFlow<Boolean> = _isCompaniesLoading.asStateFlow()

    // Live Market API States
    private val _isMarketLoading = MutableStateFlow(false)
    val isMarketLoading: StateFlow<Boolean> = _isMarketLoading.asStateFlow()

    // Live Job Detail API States
    private val _liveJobDetail = MutableStateFlow<RemoteJobDetail?>(null)
    val liveJobDetail: StateFlow<RemoteJobDetail?> = _liveJobDetail.asStateFlow()

    private val _isJobDetailLoading = MutableStateFlow(false)
    val isJobDetailLoading: StateFlow<Boolean> = _isJobDetailLoading.asStateFlow()

    // Live Report Detail API States
    private val _liveReportDetail = MutableStateFlow<com.example.network.RemoteReportDetail?>(null)
    val liveReportDetail: StateFlow<com.example.network.RemoteReportDetail?> = _liveReportDetail.asStateFlow()

    private val _isReportDetailLoading = MutableStateFlow(false)
    val isReportDetailLoading: StateFlow<Boolean> = _isReportDetailLoading.asStateFlow()

    // Live Category Jobs API States
    private val _isCategoryJobsLoading = MutableStateFlow(false)
    val isCategoryJobsLoading: StateFlow<Boolean> = _isCategoryJobsLoading.asStateFlow()

    // Live Home Dashboard and Categories States
    private val _liveHomeTrends = MutableStateFlow<List<com.example.network.RemoteTrend>>(emptyList())
    val liveHomeTrends: StateFlow<List<com.example.network.RemoteTrend>> = _liveHomeTrends.asStateFlow()

    private val _liveHomeReports = MutableStateFlow<List<com.example.network.RemoteReport>>(emptyList())
    val liveHomeReports: StateFlow<List<com.example.network.RemoteReport>> = _liveHomeReports.asStateFlow()

    private val _liveHomeSpotlight = MutableStateFlow<List<String>>(emptyList())
    val liveHomeSpotlight: StateFlow<List<String>> = _liveHomeSpotlight.asStateFlow()

    private val _liveCategories = MutableStateFlow<List<com.example.network.RemoteCategory>>(emptyList())
    val liveCategories: StateFlow<List<com.example.network.RemoteCategory>> = _liveCategories.asStateFlow()

    private val _isHomeDataLoading = MutableStateFlow(false)
    val isHomeDataLoading: StateFlow<Boolean> = _isHomeDataLoading.asStateFlow()

    private val _isCategoriesLoading = MutableStateFlow(false)
    val isCategoriesLoading: StateFlow<Boolean> = _isCategoriesLoading.asStateFlow()

    private val _liveLocations = MutableStateFlow<List<com.example.network.RemoteLocation>>(emptyList())
    val liveLocations: StateFlow<List<com.example.network.RemoteLocation>> = _liveLocations.asStateFlow()

    private val _isLocationsLoading = MutableStateFlow(false)
    val isLocationsLoading: StateFlow<Boolean> = _isLocationsLoading.asStateFlow()

    private val _liveReports = MutableStateFlow<List<com.example.network.RemoteReport>>(emptyList())
    val liveReports: StateFlow<List<com.example.network.RemoteReport>> = _liveReports.asStateFlow()

    private val _isReportsLoading = MutableStateFlow(false)
    val isReportsLoading: StateFlow<Boolean> = _isReportsLoading.asStateFlow()

    init {
        // Pre-populate jobs on viewmodel initialization
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
        fetchLiveCompanies()
        fetchLiveMarket()
        fetchLiveCategories()
        fetchLiveLocations()
        fetchLiveReports()
        viewModelScope.launch {
            _selectedCountry.collect { country ->
                fetchLiveHomeData(country)
            }
        }
    }

    fun fetchLiveReports() {
        viewModelScope.launch {
            _isReportsLoading.value = true
            try {
                val fetched = MarketRetrofitClient.service.getReports()
                _liveReports.value = fetched
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isReportsLoading.value = false
            }
        }
    }

    fun fetchLiveLocations() {
        viewModelScope.launch {
            _isLocationsLoading.value = true
            try {
                val fetched = MarketRetrofitClient.service.getLocations()
                _liveLocations.value = fetched
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLocationsLoading.value = false
            }
        }
    }

    fun fetchLiveCategories() {
        viewModelScope.launch {
            _isCategoriesLoading.value = true
            try {
                val fetched = MarketRetrofitClient.service.getCategories()
                _liveCategories.value = fetched
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCategoriesLoading.value = false
            }
        }
    }

    fun fetchLiveHomeData(countryName: String) {
        viewModelScope.launch {
            _isHomeDataLoading.value = true
            try {
                val countryParam = if (countryName == "Worldwide") "" else countryName
                val response = MarketRetrofitClient.service.getHomeData(countryParam)
                _liveHomeTrends.value = response.trends ?: emptyList()
                _liveHomeReports.value = response.reports ?: emptyList()
                _liveHomeSpotlight.value = response.spotlightCompanies ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isHomeDataLoading.value = false
            }
        }
    }

    fun fetchLiveCompanies() {
        viewModelScope.launch {
            _isCompaniesLoading.value = true
            try {
                val fetched = CompaniesRetrofitClient.service.getCompanies()
                _liveCompanies.value = fetched
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCompaniesLoading.value = false
            }
        }
    }

    fun fetchLiveMarket() {
        viewModelScope.launch {
            _isMarketLoading.value = true
            try {
                val response = MarketRetrofitClient.service.getMarketData(limit = 100, page = 1)
                val remoteJobs = response.activeJobs ?: response.jobs ?: emptyList()
                if (remoteJobs.isNotEmpty()) {
                    val existingJobs = repository.getAllJobs()
                    for (rj in remoteJobs) {
                        val existing = existingJobs.find { 
                            it.company.equals(rj.company, ignoreCase = true) && 
                            it.title.equals(rj.title, ignoreCase = true) 
                        }
                        if (existing != null) {
                            val updated = existing.copy(
                                location = rj.location,
                                salary = rj.salary ?: existing.salary,
                                type = rj.type ?: existing.type,
                                workplace = rj.workplace ?: existing.workplace,
                                datePosted = rj.datePosted ?: existing.datePosted,
                                description = rj.description ?: existing.description,
                                requirements = rj.requirements ?: existing.requirements,
                                benefits = rj.benefits ?: existing.benefits,
                                category = rj.category ?: existing.category,
                                companyWebsite = rj.companyWebsite ?: existing.companyWebsite,
                                logoResName = rj.logoResName ?: existing.logoResName,
                                remoteId = rj.id ?: existing.remoteId
                            )
                            repository.updateJob(updated)
                        } else {
                            val newJob = JobEntity(
                                title = rj.title,
                                company = rj.company,
                                logoResName = rj.logoResName ?: "",
                                location = rj.location,
                                salary = rj.salary ?: "Tshs / Neg",
                                type = rj.type ?: "Full-time",
                                workplace = rj.workplace ?: "Remote",
                                datePosted = rj.datePosted ?: "Just now",
                                description = rj.description ?: "",
                                requirements = rj.requirements ?: "",
                                benefits = rj.benefits ?: "",
                                category = rj.category ?: "General",
                                companyWebsite = rj.companyWebsite ?: "https://jobsreport.online",
                                remoteId = rj.id ?: ""
                            )
                            repository.insertJob(newJob)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isMarketLoading.value = false
            }
        }
    }

    fun fetchLiveJobDetail(remoteId: String) {
        if (remoteId.isEmpty()) return
        viewModelScope.launch {
            _isJobDetailLoading.value = true
            try {
                val detail = MarketRetrofitClient.service.getJobDetail(remoteId)
                _liveJobDetail.value = detail
                
                // Keep local SQLite DB in sync with live detail values if found
                val existing = repository.getAllJobs().find { it.remoteId == remoteId }
                if (existing != null) {
                    val updated = existing.copy(
                        title = detail.title,
                        company = detail.company,
                        location = detail.location,
                        salary = detail.salary ?: existing.salary,
                        type = detail.type ?: existing.type,
                        workplace = detail.workplace ?: existing.workplace,
                        datePosted = detail.datePosted ?: existing.datePosted,
                        description = detail.description ?: existing.description,
                        requirements = detail.requirements ?: existing.requirements,
                        benefits = detail.benefits ?: existing.benefits,
                        category = detail.category ?: existing.category,
                        companyWebsite = detail.companyWebsite ?: existing.companyWebsite,
                        logoResName = detail.logoResName ?: existing.logoResName
                    )
                    repository.updateJob(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isJobDetailLoading.value = false
            }
        }
    }

    fun clearLiveJobDetail() {
        _liveJobDetail.value = null
    }

    fun fetchLiveReportDetail(slug: String) {
        if (slug.isEmpty()) return
        viewModelScope.launch {
            _isReportDetailLoading.value = true
            try {
                val detail = MarketRetrofitClient.service.getReportDetail(slug)
                _liveReportDetail.value = detail
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isReportDetailLoading.value = false
            }
        }
    }

    fun clearLiveReportDetail() {
        _liveReportDetail.value = null
    }

    fun ensureRemoteJobSaved(rj: com.example.network.RemoteJob, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val existingJobs = repository.getAllJobs()
            val existing = existingJobs.find { 
                (rj.id != null && it.remoteId == rj.id) || 
                (it.company.equals(rj.company, ignoreCase = true) && it.title.equals(rj.title, ignoreCase = true))
            }
            if (existing != null) {
                val updated = existing.copy(
                    location = rj.location,
                    salary = rj.salary ?: existing.salary,
                    type = rj.type ?: existing.type,
                    workplace = rj.workplace ?: existing.workplace,
                    datePosted = rj.datePosted ?: existing.datePosted,
                    description = rj.description ?: existing.description,
                    requirements = rj.requirements ?: existing.requirements,
                    benefits = rj.benefits ?: existing.benefits,
                    category = rj.category ?: existing.category,
                    companyWebsite = rj.companyWebsite ?: existing.companyWebsite,
                    logoResName = rj.logoResName ?: existing.logoResName,
                    remoteId = rj.id ?: existing.remoteId
                )
                repository.updateJob(updated)
                onComplete(existing.id)
            } else {
                val newJob = JobEntity(
                    title = rj.title,
                    company = rj.company,
                    logoResName = rj.logoResName ?: "",
                    location = rj.location,
                    salary = rj.salary ?: "Tshs / Neg",
                    type = rj.type ?: "Full-time",
                    workplace = rj.workplace ?: "Remote",
                    datePosted = rj.datePosted ?: "Just now",
                    description = rj.description ?: "",
                    requirements = rj.requirements ?: "",
                    benefits = rj.benefits ?: "",
                    category = rj.category ?: "General",
                    companyWebsite = rj.companyWebsite ?: "https://jobsreport.online",
                    remoteId = rj.id ?: ""
                )
                repository.insertJob(newJob)
                val freshlyAdded = repository.getAllJobs().find { 
                    (rj.id != null && it.remoteId == rj.id) || 
                    (it.company.equals(rj.company, ignoreCase = true) && it.title.equals(rj.title, ignoreCase = true))
                }
                if (freshlyAdded != null) {
                    onComplete(freshlyAdded.id)
                }
            }
        }
    }

    fun fetchLiveCategoryJobs(categoryName: String) {
        if (categoryName.isEmpty()) return
        viewModelScope.launch {
            _isCategoryJobsLoading.value = true
            try {
                val response = MarketRetrofitClient.service.getCategoryJobs(categoryName)
                val remoteJobs = response.jobs ?: emptyList()
                if (remoteJobs.isNotEmpty()) {
                    val existingJobs = repository.getAllJobs()
                    for (rj in remoteJobs) {
                        val existing = existingJobs.find { 
                            it.company.equals(rj.company, ignoreCase = true) && 
                            it.title.equals(rj.title, ignoreCase = true) 
                        }
                        if (existing != null) {
                            val updated = existing.copy(
                                location = rj.location,
                                salary = rj.salary ?: existing.salary,
                                type = rj.type ?: existing.type,
                                workplace = rj.workplace ?: existing.workplace,
                                datePosted = rj.datePosted ?: existing.datePosted,
                                description = rj.description ?: existing.description,
                                requirements = rj.requirements ?: existing.requirements,
                                benefits = rj.benefits ?: existing.benefits,
                                category = rj.category ?: existing.category,
                                companyWebsite = rj.companyWebsite ?: existing.companyWebsite,
                                logoResName = rj.logoResName ?: existing.logoResName,
                                remoteId = rj.id ?: existing.remoteId
                            )
                            repository.updateJob(updated)
                        } else {
                            val newJob = com.example.data.JobEntity(
                                title = rj.title,
                                company = rj.company,
                                logoResName = rj.logoResName ?: "",
                                location = rj.location,
                                salary = rj.salary ?: "Tshs / Neg",
                                type = rj.type ?: "Full-time",
                                workplace = rj.workplace ?: "Remote",
                                datePosted = rj.datePosted ?: "Just now",
                                description = rj.description ?: "",
                                requirements = rj.requirements ?: "",
                                benefits = rj.benefits ?: "",
                                category = rj.category ?: categoryName,
                                companyWebsite = rj.companyWebsite ?: "https://jobsreport.online",
                                remoteId = rj.id ?: ""
                            )
                            repository.insertJob(newJob)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCategoryJobsLoading.value = false
            }
        }
    }

    // Setters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectWorkplace(workplace: String) {
        _selectedWorkplace.value = workplace
    }

    fun selectJobType(jobType: String) {
        _selectedJobType.value = jobType
    }

    fun setSelectedCountry(country: String) {
        _selectedCountry.value = country
    }

    fun setAdmin(admin: Boolean) {
        _isAdmin.value = admin
    }

    fun clearAiOutput() {
        _aiOutput.value = ""
    }

    // Database updates
    fun toggleBookmark(job: JobEntity) {
        viewModelScope.launch {
            repository.updateJob(job.copy(isBookmarked = !job.isBookmarked))
        }
    }

    fun applyToJob(job: JobEntity, notes: String = "") {
        viewModelScope.launch {
            repository.updateJob(
                job.copy(
                    isApplied = true,
                    status = "Applied",
                    notes = notes
                )
            )
        }
    }

    fun updateJobStatus(job: JobEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateJob(job.copy(status = newStatus))
        }
    }

    fun deleteJob(job: JobEntity) {
        viewModelScope.launch {
            repository.deleteJob(job)
        }
    }

    fun saveProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    // AI Career Agent Tools

    fun generateCoverLetter(job: JobEntity) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOutput.value = ""

            val profile = repository.getUserProfile() ?: UserProfileEntity()
            
            val systemPrompt = """
                You are a highly professional, elite career consultant and executive recruiter representing JobsReport.online.
                Your task is to write an outstanding, highly tailored, professional Cover Letter for the user.
                Always write in a confident, engaging tone. Do not use generic, boring placeholders. 
                Keep the structure elegant, spacing generous, and length perfect for one page (300-400 words).
                Format the response nicely in clean, styled text with clear paragraphs.
            """.trimIndent()

            val userPrompt = """
                Please write a Cover Letter for this job opportunity:
                JOB TITLE: ${job.title}
                COMPANY: ${job.company}
                CATEGORY: ${job.category}
                JOB DESCRIPTION: ${job.description}
                JOB REQUIREMENTS:
                ${job.requirements}
                
                My Professional Profile:
                FULL NAME: ${profile.fullName.ifEmpty { "Alex Mercer" }}
                TARGET TITLE: ${profile.targetTitle.ifEmpty { "Software Specialist" }}
                SKILLS: ${profile.skills.ifEmpty { "Kotlin, Java, Compose, SQLite, Git, API Integration" }}
                EXPERIENCE SUMMARY: 
                ${profile.experience.ifEmpty { "Over 3 years of building modern digital interfaces, working in agile squads, and shipping mobile products." }}
                EDUCATION: ${profile.education.ifEmpty { "Bachelor of Science in Computer Science / Information Technology" }}
                PROFESSIONAL BIO: ${profile.bio.ifEmpty { "Enthusiastic tech specialist committed to crafting exceptional, high-performance user experiences." }}
            """.trimIndent()

            val response = GeminiHelper.getAiAdvice(systemPrompt, userPrompt)
            _aiOutput.value = response
            _isAiLoading.value = false
        }
    }

    fun getInterviewPractice(job: JobEntity) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOutput.value = ""

            val profile = repository.getUserProfile() ?: UserProfileEntity()

            val systemPrompt = """
                You are an elite Tech Recruiter and hiring manager representing JobsReport.online.
                Your goal is to prepare the candidate for an interview for their selected job.
                Create exactly 3 highly relevant, job-specific interview questions.
                Provide the underlying intent of each question ("Why We Ask This"), followed by a custom "Recommended Answer" strategy tailored to the candidate's background.
                Format the output with professional layout headers, list points, and clear spacing.
            """.trimIndent()

            val userPrompt = """
                Please generate interview preparation questions and strategic answers:
                JOB TITLE: ${job.title}
                COMPANY: ${job.company}
                JOB DESCRIPTION: ${job.description}
                JOB REQUIREMENTS:
                ${job.requirements}
                
                Candidate Credentials:
                TARGET TITLE: ${profile.targetTitle.ifEmpty { "Software Developer" }}
                SKILLS: ${profile.skills.ifEmpty { "Kotlin, Jetpack Compose, REST APIs, Room DB, Git" }}
                EXPERIENCE SUMMARY: ${profile.experience.ifEmpty { "Building responsive user interfaces, refactoring legacy repositories, and implementing client-side logic." }}
            """.trimIndent()

            val response = GeminiHelper.getAiAdvice(systemPrompt, userPrompt)
            _aiOutput.value = response
            _isAiLoading.value = false
        }
    }

    fun optimizeResume(job: JobEntity) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOutput.value = ""

            val profile = repository.getUserProfile() ?: UserProfileEntity()

            val systemPrompt = """
                You are an expert Resume Editor and Professional Brand Writer representing JobsReport.online.
                Your goal is to critique the candidate's skills and summary against a specific job posting.
                Analyze gaps between the job requirements and the user's skills.
                Provide concrete, bulleted recommendations on:
                1. Keywords to inject (to beat applicant tracking systems).
                2. Summary optimization (how to rewrite their professional bio).
                3. Skills matrix alignment (specific technical or soft skills to highlight).
                Be concise, constructive, and highly practical.
            """.trimIndent()

            val userPrompt = """
                Critique my details and optimize them for this target job:
                JOB TITLE: ${job.title}
                COMPANY: ${job.company}
                JOB DESCRIPTION: ${job.description}
                JOB REQUIREMENTS:
                ${job.requirements}
                
                My Current Details:
                MY TITLE: ${profile.targetTitle.ifEmpty { "Software Professional" }}
                MY SKILLS: ${profile.skills.ifEmpty { "Kotlin, Mobile design, SQL, Git" }}
                MY EXPERIENCE: ${profile.experience.ifEmpty { "Developing native user features, troubleshooting client databases, and integrating background workers." }}
                MY BIO: ${profile.bio.ifEmpty { "Detail-oriented designer and coder looking for dynamic team environments." }}
            """.trimIndent()

            val response = GeminiHelper.getAiAdvice(systemPrompt, userPrompt)
            _aiOutput.value = response
            _isAiLoading.value = false
        }
    }
}

// Factory to create MainViewModel with Repository
class MainViewModelFactory(private val application: Application, private val repository: JobRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
