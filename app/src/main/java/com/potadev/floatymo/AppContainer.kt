package com.potadev.floatymo

import android.content.Context
import com.potadev.floatymo.data.local.LocalStorage
import com.potadev.floatymo.data.remote.GiphyApi
import com.potadev.floatymo.data.repository.GifRepositoryImpl
import com.potadev.floatymo.data.repository.SettingsRepositoryImpl
import com.potadev.floatymo.domain.repository.GifRepository
import com.potadev.floatymo.domain.repository.SettingsRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppContainer {
    private const val GIPHY_BASE_URL = "https://api.giphy.com/"

    private var localStorage: LocalStorage? = null
    private var giphyApi: GiphyApi? = null
    private var gifRepository: GifRepository? = null
    private var settingsRepository: SettingsRepository? = null

    fun init(context: Context) {
        if (localStorage == null) {
            localStorage = LocalStorage(context.applicationContext)
        }

        if (giphyApi == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(GIPHY_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            giphyApi = retrofit.create(GiphyApi::class.java)
        }

        if (gifRepository == null) {
            gifRepository = GifRepositoryImpl(localStorage!!)
        }

        if (settingsRepository == null) {
            settingsRepository = SettingsRepositoryImpl(localStorage!!)
        }
    }

    fun provideGifRepository(): GifRepository {
        return gifRepository ?: throw IllegalStateException("AppContainer not initialized")
    }

    fun provideSettingsRepository(): SettingsRepository {
        return settingsRepository ?: throw IllegalStateException("AppContainer not initialized")
    }

    fun provideGiphyApi(): GiphyApi {
        return giphyApi ?: throw IllegalStateException("AppContainer not initialized")
    }
}
