package com.example.mybloom.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discoveries")
data class Discovery(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plantName: String,
    val imagePath: String,
    val timestamp: Long,
    val funFact: String = ""
)

