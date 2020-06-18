package com.lucasjwilber.freenote.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "notes")
data class Note(
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "text") var segments: String
) {
    @PrimaryKey(autoGenerate = true) var id: Long? = null
    @ColumnInfo(name = "timestamp") var timestamp: Long = Date().time
}
