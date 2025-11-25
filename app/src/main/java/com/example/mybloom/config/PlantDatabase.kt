package com.example.mybloom.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mybloom.dao.DiscoveryDao
import com.example.mybloom.entities.DiscoveryEntity

@Database(
    entities = [DiscoveryEntity::class],
    version = 1,  // ⬅️ Garde version 1 (ou réinitialise à 1)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun discoveryDao(): DiscoveryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plantdiscovery_database"
                )
                    .fallbackToDestructiveMigration()  // ⬅️ Supprime et recrée la DB
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
