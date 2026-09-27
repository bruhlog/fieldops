package com.zaidsiddique.fieldops.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.database.FirebaseDatabase
import com.zaidsiddique.fieldops.data.AppDatabase
import com.zaidsiddique.fieldops.data.CheckInDao
import com.zaidsiddique.fieldops.location.LocationHelper
import com.zaidsiddique.fieldops.network.GeocodingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fieldops.db"
        ).build()

    @Provides
    fun provideDao(
        db: AppDatabase
    ): CheckInDao =
        db.checkInDao()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase =
        FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideGeocodingService(): GeocodingService =
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(GeocodingService::class.java)

    @Provides
    @Singleton
    fun provideLocationHelper(
        @ApplicationContext context: Context
    ): LocationHelper =
        LocationHelper(context)
}