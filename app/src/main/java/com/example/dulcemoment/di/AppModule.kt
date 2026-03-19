package com.example.dulcemoment.di

import com.example.dulcemoment.data.network.ApiService
import com.example.dulcemoment.data.network.RetrofitClient
import com.example.dulcemoment.data.repo.CakeRepository
import com.example.dulcemoment.data.repo.LocalDulceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideApiService(): ApiService = RetrofitClient.api
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCakeRepository(repository: LocalDulceRepository): CakeRepository
}
