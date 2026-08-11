package com.dev.trackone.core.network.di

import com.dev.trackone.core.network.BuildConfig
import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.interceptor.AuthInterceptor
import com.dev.trackone.core.network.interceptor.BaseUrlInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Never actually contacted: [BaseUrlInterceptor] replaces the origin on every
     * request from the runtime setting. Retrofit only needs a syntactically valid
     * base to resolve the relative paths in [TrackOneApi] against.
     */
    private const val PLACEHOLDER_BASE_URL = "http://api.invalid/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        baseUrlInterceptor: BaseUrlInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(baseUrlInterceptor)
        .addInterceptor(authInterceptor)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        // BASIC, not BODY: request bodies here contain passwords.
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(PLACEHOLDER_BASE_URL)
        .client(client)
        // withNullSerialization so clearing an optional field (notes, amount…)
        // actually sends `null` and clears it server-side, instead of Moshi
        // dropping the key and the partial update keeping the old value.
        .addConverterFactory(MoshiConverterFactory.create(moshi).withNullSerialization())
        .build()

    @Provides
    @Singleton
    fun provideTrackOneApi(retrofit: Retrofit): TrackOneApi = retrofit.create(TrackOneApi::class.java)
}
