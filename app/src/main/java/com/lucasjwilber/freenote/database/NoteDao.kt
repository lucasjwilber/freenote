package com.lucasjwilber.freenote.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.lucasjwilber.freenote.models.Note
import com.lucasjwilber.freenote.models.NoteDescriptor

@Dao
interface NoteDao {

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
    fun getNoteById(id: Long): LiveData<Note>

    // insert returns the id of the created Note
    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}