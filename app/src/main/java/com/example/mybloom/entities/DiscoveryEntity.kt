package com.example.mybloom.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discoveries")
data class DiscoveryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val plantName: String,
    val aiFact: String,
    val localImagePath: String,
    val timestamp: Long = System.currentTimeMillis()
)
