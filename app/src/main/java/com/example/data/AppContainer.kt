package com.example.data

import android.content.Context
import com.example.BuildConfig
import com.example.data.api.CompanionApiService
import com.example.data.api.AuthApiService
import com.example.data.database.AppDatabase
import com.example.data.repository.CompanionRepository
import com.example.data.repository.AuthRepository

object AppContainer {
  private var repository: CompanionRepository? = null
  private var authRepository: AuthRepository? = null

  fun getRepository(context: Context): CompanionRepository {
    return repository ?: synchronized(this) {
      val db = AppDatabase.getDatabase(context)
      val baseUrl = if (BuildConfig.BACKEND_BASE_URL.isNotEmpty()) BuildConfig.BACKEND_BASE_URL else "http://10.0.2.2:8000/"
      val api = CompanionApiService.create(baseUrl)
      val repo = CompanionRepository(db.companionDao(), api, context.applicationContext)
      repository = repo
      repo
    }
  }

  fun getAuthRepository(context: Context): AuthRepository {
    return authRepository ?: synchronized(this) {
      val db = AppDatabase.getDatabase(context)
      val baseUrl = if (BuildConfig.BACKEND_BASE_URL.isNotEmpty()) BuildConfig.BACKEND_BASE_URL else "http://10.0.2.2:8000/"
      val authApi = AuthApiService.create(baseUrl)
      val authRepo = AuthRepository(db.companionDao(), authApi, context.applicationContext)
      authRepository = authRepo
      authRepo
    }
  }
}
