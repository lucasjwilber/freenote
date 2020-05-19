package com.lucasjwilber.freenote

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<Note>

    @Query("SELECT id, title, type FROM notes ORDER BY id DESC")
    fun getAllDescriptorsNewestFirst(): LiveData<List<NoteDescriptor>>

    @Query("SELECT id, title, type FROM notes ORDER BY id ASC")
    fun getAllDescriptorsOldestFirst(): LiveData<List<NoteDescriptor>>

    @Query("SELECT id, title, type FROM notes ORDER BY timestamp DESC")
    fun getAllDescriptorsLastUpdatedFirst(): LiveData<List<NoteDescriptor>>

    @Query("SELECT id, title, type FROM notes ORDER BY type DESC")
    fun getAllDescriptorsNotesFirst(): LiveData<List<NoteDescriptor>>

    @Query("SELECT id, title, type FROM notes ORDER BY type ASC")
    fun getAllDescriptorsListsFirst(): LiveData<List<NoteDescriptor>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note

    @Insert
    suspend fun insert(vararg note: Note)

    @Update
    suspend fun update(vararg note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}