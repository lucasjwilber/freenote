package com.lucasjwilber.freenote

import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<Note>

    @Query("SELECT id, title FROM notes")
    suspend fun getAllDescriptors(): List<NoteDescriptor>

//    @Query("SELECT * FROM note WHERE uid IN (:noteIds)")
//    fun loadAllByIds(noteIds: IntArray): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Note

    @Insert
    suspend fun insert(vararg note: Note)

    @Update
    suspend fun update(vararg note: Note)

    @Delete
    suspend fun delete(note: Note)
}