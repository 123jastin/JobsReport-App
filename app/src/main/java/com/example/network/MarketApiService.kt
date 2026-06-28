package com.example.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class RemoteJobImage(
    val url: String,
    val thumbnail: String? = null,
    val name: String? = null,
    val type: String? = null,
    val seoTitle: String? = null,
    val seoDescription: String? = null,
    val caption: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteJob(
    val id: String? = null,
    val title: String,
    val company: String,
    val logoResName: String? = null,
    val location: String,
    val salary: String? = null,
    val type: String? = null,
    val workplace: String? = null,
    val datePosted: String? = null,
    val description: String? = null,
    val requirements: String? = null,
    val benefits: String? = null,
    val category: String? = null,
    val companyWebsite: String? = null,
    val active: Boolean? = null,
    val postedAt: String? = null,
    val expiresAt: String? = null,
    val url: String? = null,
    val logoUrl: String? = null,
    val whatsapp_number: String? = null,
    val application_instructions: String? = null,
    val salary_currency_flag: String? = null,
    val employment_type: String? = null,
    val workplace_type: String? = null,
    val education_level: String? = null,
    val experience_months: Int? = null,
    val images: List<RemoteJobImage>? = null
)

@JsonClass(generateAdapter = true)
data class RemoteJobDetail(
    val id: String? = null,
    val title: String,
    val company: String,
    val logoResName: String? = null,
    val location: String,
    val salary: String? = null,
    val type: String? = null,
    val workplace: String? = null,
    val datePosted: String? = null,
    val description: String? = null,
    val requirements: String? = null,
    val benefits: String? = null,
    val category: String? = null,
    val companyWebsite: String? = null,
    val active: Boolean? = null,
    val relatedJobs: List<RemoteJob>? = null,
    val postedAt: String? = null,
    val expiresAt: String? = null,
    val url: String? = null,
    val logoUrl: String? = null,
    val whatsapp_number: String? = null,
    val application_instructions: String? = null,
    val salary_currency_flag: String? = null,
    val employment_type: String? = null,
    val workplace_type: String? = null,
    val education_level: String? = null,
    val experience_months: Int? = null,
    val images: List<RemoteJobImage>? = null
)

@JsonClass(generateAdapter = true)
data class MarketStats(
    val totalJobs: Int? = null,
    val activeJobs: Int? = null
)

@JsonClass(generateAdapter = true)
data class MarketResponse(
    val activeJobs: List<RemoteJob>? = null,
    val jobs: List<RemoteJob>? = null,
    val companies: List<RemoteCompany>? = null,
    val roles: List<String>? = null,
    val stats: MarketStats? = null
)

@JsonClass(generateAdapter = true)
data class CategoryJobsResponse(
    val jobs: List<RemoteJob>? = null
)

@JsonClass(generateAdapter = true)
data class RemoteCategory(
    val name: String,
    val slug: String? = null,
    val id: String? = null,
    val jobCount: Int? = null,
    val jobsCount: Int? = null,
    val count: Int? = null
)

@JsonClass(generateAdapter = true)
data class RemoteMetric(
    val label: String,
    val value: Float
)

@JsonClass(generateAdapter = true)
data class RemoteReport(
    val id: String? = null,
    val slug: String? = null,
    val title: String,
    val author: String? = null,
    val date: String? = null,
    val monthYear: String? = null,
    val category: String? = null,
    val excerpt: String? = null,
    val summary: String? = null,
    val country: String? = null,
    val role: String? = null,
    val updatedAt: String? = null,
    val metrics: List<RemoteMetric>? = null
)

@JsonClass(generateAdapter = true)
data class RemoteReportStats(
    val companies: Int? = null,
    val growth: Int? = null
)

@JsonClass(generateAdapter = true)
data class RemoteChartDataItem(
    val name: String,
    val demand: Float
)

@JsonClass(generateAdapter = true)
data class RemoteDistributionItem(
    val name: String,
    val value: Float
)

@JsonClass(generateAdapter = true)
data class RemoteCompanyItem(
    val name: String,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class RemoteReportDetail(
    val id: String? = null,
    val slug: String? = null,
    val title: String,
    val author: String? = null,
    val date: String? = null,
    val monthYear: String? = null,
    val category: String? = null,
    val excerpt: String? = null,
    val content: String? = null,
    val summary: String? = null,
    val country: String? = null,
    val role: String? = null,
    val updatedAt: String? = null,
    val createdAt: String? = null,
    val metrics: List<RemoteMetric>? = null,
    val stats: RemoteReportStats? = null,
    val chartData: List<RemoteChartDataItem>? = null,
    val distribution: List<RemoteDistributionItem>? = null,
    val companies: List<RemoteCompanyItem>? = null,
    val jobs: List<RemoteJob>? = null
)

@JsonClass(generateAdapter = true)
data class RemoteTrend(
    val phrase: String? = null,
    val volume: String? = null,
    val trend: String? = null,
    val name: String? = null,
    val growth: String? = null
)

@JsonClass(generateAdapter = true)
data class HomeDataResponse(
    val trends: List<RemoteTrend>? = null,
    val reports: List<RemoteReport>? = null,
    val spotlightCompanies: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class RemoteLocation(
    val name: String,
    val country: String,
    val region: String? = null,
    val postcode: String? = null
)

interface MarketApiService {
    @GET("api/market")
    suspend fun getMarketData(
        @Query("limit") limit: Int = 100,
        @Query("page") page: Int = 1
    ): MarketResponse

    @GET("api/job-detail/{id}")
    suspend fun getJobDetail(
        @Path("id") id: String
    ): RemoteJobDetail

    @GET("api/category-jobs")
    suspend fun getCategoryJobs(
        @Query("category") category: String
    ): CategoryJobsResponse

    @GET("api/categories")
    suspend fun getCategories(): List<RemoteCategory>

    @GET("api/home")
    suspend fun getHomeData(
        @Query("country") country: String
    ): HomeDataResponse

    @GET("api/locations")
    suspend fun getLocations(): List<RemoteLocation>

    @GET("api/reports")
    suspend fun getReports(): List<RemoteReport>

    @GET("api/reports/{slug}")
    suspend fun getReportDetail(
        @Path("slug") slug: String
    ): RemoteReportDetail

    @GET("api/company-jobs/{companyId}")
    suspend fun getCompanyJobs(
        @Path("companyId") companyId: String
    ): List<RemoteJob>
}

object MarketRetrofitClient {
    private const val BASE_URL = "https://jobsreport.online/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: MarketApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MarketApiService::class.java)
    }
}
