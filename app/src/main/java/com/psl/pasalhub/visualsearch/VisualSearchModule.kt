package com.psl.pasalhub.visualsearch

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VisualSearchModule {

    @Provides
    @Singleton
    fun provideVisualSearchEngine(@ApplicationContext context: Context): VisualSearchEngine {
        return VisualSearchEngine(context)
    }
}
