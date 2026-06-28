package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JobEntity
import com.example.viewmodel.MainViewModel
import com.example.network.RemoteCompany
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

// Local domain classes
data class CompanyMetaInfo(
    val website: String,
    val industry: String,
    val description: String,
    val foundedYear: String?,
    val employeeCount: String?,
    val streetAddress: String?,
    val area: String?,
    val locality: String?,
    val district: String?,
    val postalCode: String?,
    val country: String?
)

data class Company(
    val id: String,
    val name: String,
    val website: String,
    val description: String,
    val industry: String,
    val foundedYear: String?,
    val employeeCount: String?,
    val streetAddress: String?,
    val area: String?,
    val locality: String?,
    val district: String?,
    val postalCode: String?,
    val country: String?,
    val logoResName: String?,
    val totalJobs: Int,
    val activeJobs: Int,
    val jobs: List<JobEntity>,
    val logoUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompaniesScreen(
    viewModel: MainViewModel,
    onCompanyClick: (String) -> Unit,
    onJobClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allJobs by viewModel.allJobs.collectAsState()
    val liveCompaniesState by viewModel.liveCompanies.collectAsState()
    val isCompaniesLoading by viewModel.isCompaniesLoading.collectAsState()
    val selectedCompanyJobs by viewModel.selectedCompanyJobs.collectAsState()
    val isCompanyJobsLoading by viewModel.isCompanyJobsLoading.collectAsState()
    
    // Group jobs dynamically into actual Company structures
    val companies = remember(allJobs, liveCompaniesState) {
        val remoteList = liveCompaniesState
        if (!remoteList.isNullOrEmpty()) {
            remoteList.map { rc ->
                val jobsForCompany = allJobs.filter { it.company.equals(rc.name, ignoreCase = true) }
                Company(
                    id = rc.id ?: rc.name,
                    name = rc.name,
                    website = rc.website ?: "https://jobsreport.online",
                    description = rc.description ?: "Verified partner of JobsReport platform.",
                    industry = rc.industry ?: "Technology",
                    foundedYear = rc.foundedYear,
                    employeeCount = rc.employeeCount,
                    streetAddress = rc.streetAddress,
                    area = rc.area,
                    locality = rc.locality ?: "Remote",
                    district = rc.district,
                    postalCode = rc.postalCode,
                    country = rc.country ?: "US",
                    logoResName = rc.logoUrl ?: jobsForCompany.firstOrNull()?.logoResName,
                    totalJobs = rc.totalJobs ?: jobsForCompany.size,
                    activeJobs = rc.activeJobs ?: jobsForCompany.size,
                    jobs = jobsForCompany,
                    logoUrl = rc.logoUrl
                )
            }.sortedByDescending { it.activeJobs }
        } else {
            allJobs.groupBy { it.company }.map { (companyName, jobsForCompany) ->
                val firstJob = jobsForCompany.firstOrNull()
                
                // Map known companies with premium descriptions & locations matching react page spec
                val companyMetadata = when (companyName) {
                    "NMB Bank Tanzania" -> CompanyMetaInfo(
                        website = "https://nmbbank.co.tz",
                        industry = "Financial Services",
                        description = "NMB Bank Plc is Tanzania's leading digital banking and financial services institution, driving financial inclusion with micro-loans, retail banking, and modern fintech integrations across the nation.",
                        foundedYear = "1997",
                        employeeCount = "3,000+",
                        streetAddress = "Ohio Street / Ali Hassan Mwinyi Road",
                        area = "Kisutu",
                        locality = "Dar es Salaam",
                        district = "Ilala",
                        postalCode = "P.O. Box 9213",
                        country = "TZ"
                    )
                    "SwahiliTech Solutions" -> CompanyMetaInfo(
                        website = "https://swahilitech.co.tz",
                        industry = "Software Development",
                        description = "SwahiliTech is the fastest-growing mobile startup in Tanzania. We specialize in robust fintech, mobile commerce systems, logistics, and customized local API integrations for merchants in East Africa.",
                        foundedYear = "2020",
                        employeeCount = "20-50",
                        streetAddress = "Binti-Khamis Building",
                        area = "Kijitonyama",
                        locality = "Dar es Salaam",
                        district = "Kinondoni",
                        postalCode = "P.O. Box 1024",
                        country = "TZ"
                    )
                    "NovaTech Solutions" -> CompanyMetaInfo(
                        website = "https://novatech.io",
                        industry = "Technology",
                        description = "NovaTech Solutions is an enterprise tech developer building high-performance native Android application suites, machine learning components, and distributed server infrastructure globally.",
                        foundedYear = "2021",
                        employeeCount = "50-100",
                        streetAddress = "100 Pine Street",
                        area = "Financial District",
                        locality = "San Francisco",
                        district = "California",
                        postalCode = "94111",
                        country = "US"
                    )
                    "Aether Creative Agency" -> CompanyMetaInfo(
                        website = "https://aethercreate.com",
                        industry = "Creative Studio",
                        description = "Aether Creative is a premium award-winning digital design agency, establishing modern visual identity standards, robust design systems, and Figma UI/UX architecture workshops.",
                        foundedYear = "2018",
                        employeeCount = "10-25",
                        streetAddress = "450 Broadway",
                        area = "Soho",
                        locality = "New York",
                        district = "New York",
                        postalCode = "10012",
                        country = "US"
                    )
                    "Apex Scaling Co." -> CompanyMetaInfo(
                        website = "https://apexscale.co",
                        industry = "Marketing & Growth",
                        description = "Apex Scaling helps SaaS startups double their customer acquisition rates. We specialize in ROI-positive performance marketing campaigns, conversion-focused landing experiences, and SEO strategy.",
                        foundedYear = "2019",
                        employeeCount = "15-50",
                        streetAddress = "701 Brazos Street",
                        area = "Downtown",
                        locality = "Austin",
                        district = "Texas",
                        postalCode = "78701",
                        country = "US"
                    )
                    "OmniCore Cloud Services" -> CompanyMetaInfo(
                        website = "https://omnicore.cloud",
                        industry = "Cloud Computing",
                        description = "OmniCore Cloud Services is an infrastructure pioneer, designing scalable microservices platforms, high-availability clusters, Kubernetes orchestrators, and private cloud APIs.",
                        foundedYear = "2016",
                        employeeCount = "100-250",
                        streetAddress = "1201 3rd Avenue",
                        area = "Downtown",
                        locality = "Seattle",
                        district = "Washington",
                        postalCode = "98101",
                        country = "US"
                    )
                    "Logos Media" -> CompanyMetaInfo(
                        website = "https://logosmedia.pub",
                        industry = "Content Publishing",
                        description = "Logos Media operates a network of high-authority finance, real estate, and ecommerce publications. We specialize in premium SEO content engineering and developer relations blogging.",
                        foundedYear = "2017",
                        employeeCount = "10-20",
                        streetAddress = "Remote First",
                        area = "Remote",
                        locality = "Remote",
                        district = "Remote",
                        postalCode = "00000",
                        country = "US"
                    )
                    "Synergy Support Systems" -> CompanyMetaInfo(
                        website = "https://synergycrm.com",
                        industry = "Customer Support",
                        description = "Synergy Support Systems is an enterprise client-relations platform provider, deploying AI-augmented customer success tools, CRM workflows, and live ticket resolution workspaces.",
                        foundedYear = "2015",
                        employeeCount = "50-150",
                        streetAddress = "222 West Merchandise Mart Plaza",
                        area = "River North",
                        locality = "Chicago",
                        district = "Illinois",
                        postalCode = "60654",
                        country = "US"
                    )
                    else -> CompanyMetaInfo(
                        website = firstJob?.companyWebsite ?: "https://jobsreport.online",
                        industry = firstJob?.category ?: "General",
                        description = "Verified partner of JobsReport platform offering dynamic growth and career advancement pathways.",
                        foundedYear = "2022",
                        employeeCount = "10-50",
                        streetAddress = null,
                        area = null,
                        locality = firstJob?.location?.substringBefore(",") ?: "Remote",
                        district = null,
                        postalCode = null,
                        country = if (firstJob?.location?.contains("Tanzania", ignoreCase = true) == true) "TZ" else "US"
                    )
                }

                Company(
                    id = companyName,
                    name = companyName,
                    website = companyMetadata.website,
                    description = companyMetadata.description,
                    industry = companyMetadata.industry,
                    foundedYear = companyMetadata.foundedYear,
                    employeeCount = companyMetadata.employeeCount,
                    streetAddress = companyMetadata.streetAddress,
                    area = companyMetadata.area,
                    locality = companyMetadata.locality,
                    district = companyMetadata.district,
                    postalCode = companyMetadata.postalCode,
                    country = companyMetadata.country,
                    logoResName = firstJob?.logoResName,
                    totalJobs = jobsForCompany.size,
                    activeJobs = jobsForCompany.size,
                    jobs = jobsForCompany
                )
            }.sortedByDescending { it.activeJobs }
        }
    }

    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    val displayCompanyJobs = remember(selectedCompanyJobs, selectedCompany, isCompanyJobsLoading) {
        val currentCompany = selectedCompany
        if (currentCompany == null || isCompanyJobsLoading) {
            emptyList()
        } else if (selectedCompanyJobs.isNotEmpty()) {
            selectedCompanyJobs
        } else {
            currentCompany.jobs
        }
    }
    var searchTerm by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    
    val COMPANIES_PER_PAGE = 12

    // Filtered companies list
    val filteredCompanies = remember(companies, searchTerm) {
        companies.filter {
            it.name.contains(searchTerm, ignoreCase = true) ||
            it.industry.contains(searchTerm, ignoreCase = true)
        }
    }

    val totalPages = remember(filteredCompanies) {
        val count = filteredCompanies.size
        if (count == 0) 1 else Math.ceil(count.toDouble() / COMPANIES_PER_PAGE).toInt()
    }

    // Adjust page if it exceeds bounds after search
    LaunchedEffect(filteredCompanies, totalPages) {
        if (currentPage > totalPages) {
            currentPage = totalPages
        }
    }

    val paginatedCompanies = remember(filteredCompanies, currentPage) {
        val start = (currentPage - 1) * COMPANIES_PER_PAGE
        val end = minOf(start + COMPANIES_PER_PAGE, filteredCompanies.size)
        if (start < filteredCompanies.size) {
            filteredCompanies.subList(start, end)
        } else {
            emptyList()
        }
    }

    val totalActiveJobs = remember(companies) {
        companies.sumOf { it.activeJobs }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("companies_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Employer Directory Header Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "EMPLOYER DIRECTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6),
                        letterSpacing = 1.5.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = selectedCompany?.name ?: "Companies & Employers",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = if (selectedCompany != null) {
                        "Browse open positions and career growth opportunities at ${selectedCompany?.name}."
                    } else {
                        "Browse top companies actively hiring verified professionals."
                    },
                    fontSize = 15.sp,
                    color = Color(0xFF94A3B8)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if (selectedCompany != null) 1 else companies.size} Companies",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${selectedCompany?.activeJobs ?: totalActiveJobs} Active Jobs",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isCompaniesLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color(0xFF3B82F6),
                                strokeWidth = 1.5.dp
                            )
                            Text(
                                text = "Syncing...",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else if (liveCompaniesState != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                text = "Live Sync",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Search Input (Only when no company is selected)
        if (selectedCompany == null) {
            item {
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { 
                        searchTerm = it
                        currentPage = 1
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("companies_search_bar"),
                    placeholder = { Text("Search ${companies.size} companies...", color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF64748B)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6).copy(alpha = 0.6f),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        focusedPlaceholderColor = Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }
        }

        // Render selected company details or listed companies
        if (selectedCompany != null) {
            val company = selectedCompany!!

            item {
                Button(
                    onClick = { selectedCompany = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BACK TO ALL COMPANIES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Company Profile Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Adaptive Logo Placeholder or actual image
                        val context = LocalContext.current
                        val logoUrlToUse = company.logoUrl ?: company.logoResName ?: ""
                        val isUrl = logoUrlToUse.startsWith("http://") || logoUrlToUse.startsWith("https://")
                        val imageResId = remember(logoUrlToUse) {
                            if (!isUrl && logoUrlToUse.isNotEmpty()) {
                                context.resources.getIdentifier(logoUrlToUse, "drawable", context.packageName)
                            } else 0
                        }

                        if (isUrl) {
                            AsyncImage(
                                model = logoUrlToUse,
                                contentDescription = company.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = company.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF2563EB), Color(0xFF8B5CF6))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = company.name.firstOrNull()?.toString()?.uppercase() ?: "",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = company.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = company.industry,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA78BFA)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${company.activeJobs} active jobs",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "•",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = "${company.totalJobs} total listings",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // Location & Business Info double grid row (or columns depending on screen width)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Location Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "LOCATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF10B981),
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                company.streetAddress?.let {
                                    InfoRow(label = "Street", value = it)
                                }
                                company.area?.let {
                                    InfoRow(label = "Area", value = it)
                                }
                                company.locality?.let {
                                    InfoRow(label = "City", value = it)
                                }
                                company.district?.let {
                                    InfoRow(label = "District", value = it)
                                }
                                company.postalCode?.let {
                                    InfoRow(label = "Postal", value = it)
                                }
                                company.country?.let {
                                    val displayCountry = if (it == "TZ") "🇹🇿 Tanzania" else it
                                    InfoRow(label = "Country", value = displayCountry, highlightColor = Color(0xFF10B981))
                                }
                            }
                        }
                    }

                    // Business Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "BUSINESS INFO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF3B82F6),
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoRow(label = "Website", value = company.website.replace("https://", ""), isLink = true, linkUrl = company.website)
                                InfoRow(label = "Industry", value = company.industry, highlightColor = Color(0xFFA78BFA))
                                company.foundedYear?.let {
                                    InfoRow(label = "Founded", value = it)
                                }
                                company.employeeCount?.let {
                                    InfoRow(label = "Employees", value = it)
                                }
                            }
                        }
                    }
                }
            }

            // About Company Description
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "About ${company.name}".uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF59E0B),
                            letterSpacing = 1.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = company.description,
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Openings Title
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .background(Color(0xFF3B82F6), RoundedCornerShape(2.dp))
                    )
                    val jobsCount = if (isCompanyJobsLoading) 0 else displayCompanyJobs.size
                    Text(
                        text = "Job Openings ($jobsCount)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Openings List
            if (isCompanyJobsLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                }
            } else if (displayCompanyJobs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No job listings available for this company.",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(displayCompanyJobs) { job ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onJobClick(job.id) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF3B82F6).copy(alpha = 0.1f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                    Text(
                                        text = job.type,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = job.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = job.location,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = job.salary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }

        } else {
            // Listed Companies view
            if (filteredCompanies.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No companies found matching \"$searchTerm\"",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                items(paginatedCompanies) { company ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .clickable {
                                selectedCompany = company
                                viewModel.fetchCompanyJobs(company.id)
                            }
                            .testTag("company_card_${company.name.replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Custom Adaptive Logo or actual image
                                val context = LocalContext.current
                                val logoUrlToUse = company.logoUrl ?: company.logoResName ?: ""
                                val isUrl = logoUrlToUse.startsWith("http://") || logoUrlToUse.startsWith("https://")
                                val imageResId = remember(logoUrlToUse) {
                                    if (!isUrl && logoUrlToUse.isNotEmpty()) {
                                        context.resources.getIdentifier(logoUrlToUse, "drawable", context.packageName)
                                    } else 0
                                }

                                if (isUrl) {
                                    AsyncImage(
                                        model = logoUrlToUse,
                                        contentDescription = company.name,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (imageResId != 0) {
                                    Image(
                                        painter = painterResource(id = imageResId),
                                        contentDescription = company.name,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFF2563EB), Color(0xFF8B5CF6))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = company.name.firstOrNull()?.toString()?.uppercase() ?: "",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = company.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Text(
                                        text = company.industry,
                                        fontSize = 12.sp,
                                        color = Color(0xFFA78BFA),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "View Company Details",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFF334155).copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Work,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${company.activeJobs} active jobs",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (company.totalJobs > company.activeJobs) {
                                        Text(
                                            text = "(${company.totalJobs} total)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = company.locality ?: "Remote",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Pagination Row
                if (totalPages > 1) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { if (currentPage > 1) currentPage-- },
                                enabled = currentPage > 1,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E293B),
                                    disabledContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                                    contentColor = Color(0xFF94A3B8),
                                    disabledContentColor = Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Previous Page",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Prev", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Page $currentPage of $totalPages",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = { if (currentPage < totalPages) currentPage++ },
                                enabled = currentPage < totalPages,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E293B),
                                    disabledContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                                    contentColor = Color(0xFF94A3B8),
                                    disabledContentColor = Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Next", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Next Page",
                                        modifier = Modifier.size(16.dp)
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
fun InfoRow(
    label: String,
    value: String,
    isLink: Boolean = false,
    linkUrl: String = "",
    highlightColor: Color = Color(0xFFCBD5E1)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            modifier = Modifier.width(72.dp)
        )
        
        if (isLink) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B82F6)
                )
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6).copy(alpha = 0.6f),
                    modifier = Modifier.size(10.dp)
                )
            }
        } else {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
        }
    }
}
