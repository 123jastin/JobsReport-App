package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class JobRepository(private val jobDao: JobDao) {

    val allJobs: Flow<List<JobEntity>> = jobDao.getAllJobsFlow()
    val userProfile: Flow<UserProfileEntity?> = jobDao.getUserProfileFlow()

    suspend fun getAllJobs(): List<JobEntity> = withContext(Dispatchers.IO) {
        jobDao.getAllJobs()
    }

    fun getJobByIdFlow(id: Int): Flow<JobEntity?> = jobDao.getJobByIdFlow(id)

    suspend fun getJobById(id: Int): JobEntity? = withContext(Dispatchers.IO) {
        jobDao.getJobById(id)
    }

    suspend fun insertJob(job: JobEntity) = withContext(Dispatchers.IO) {
        jobDao.insertJob(job)
    }

    suspend fun updateJob(job: JobEntity) = withContext(Dispatchers.IO) {
        jobDao.updateJob(job)
    }

    suspend fun deleteJob(job: JobEntity) = withContext(Dispatchers.IO) {
        jobDao.deleteJob(job)
    }

    suspend fun saveProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        jobDao.insertOrUpdateProfile(profile)
    }

    suspend fun getUserProfile(): UserProfileEntity? = withContext(Dispatchers.IO) {
        jobDao.getUserProfile()
    }

    // Pre-populates the database if empty to provide immediate offline jobs
    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        // Mock prepopulate disabled - only live data from API is used
        // Clear any old/existing demo jobs in the user database
        jobDao.deleteDemoJobs()
    }
}
