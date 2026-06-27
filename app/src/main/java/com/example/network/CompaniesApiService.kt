package com.example.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class RemoteCompany(
    val name: String,
    val website: String? = null,
    val description: String? = null,
    val industry: String? = null,
    val foundedYear: String? = null,
    val employeeCount: String? = null,
    val streetAddress: String? = null,
    val area: String? = null,
    val locality: String? = null,
    val district: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val logoResName: String? = null,
    val totalJobs: Int? = null,
    val activeJobs: Int? = null
)

interface CompaniesApiService {
    @GET("api/companies-jobs")
    suspend fun getCompanies(): List<RemoteCompany>
}

object CompaniesRetrofitClient {
    private const val BASE_URL = "https://ais-dev-7lny3ht7rt4jqes2yrodr3-264764251135.europe-west2.run.app/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: CompaniesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CompaniesApiService::class.java)
    }
}
