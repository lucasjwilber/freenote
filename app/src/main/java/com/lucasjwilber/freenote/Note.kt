package com.lucasjwilber.freenote

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name="type") var type: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "segments") var segments: String?
)