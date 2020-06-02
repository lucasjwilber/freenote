package com.lucasjwilber.freenote.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "text") var segments: String,
    @ColumnInfo(name = "timestamp") var timestamp: Long
)