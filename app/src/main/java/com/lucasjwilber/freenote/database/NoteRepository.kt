package com.lucasjwilber.freenote.database

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.models.Note
import com.lucasjwilber.freenote.models.NoteDescriptor
import java.util.*

class NoteRepository(private val noteDao: NoteDao, context: Context) {

    lateinit var allNoteDescriptors: LiveData<List<NoteDescriptor>>
    private val prefs: SharedPreferences = context.getSharedPreferences("freenote_prefs", Context.MODE_PRIVATE)
    private var sortType: Int = prefs.getInt("sortType", R.id.menu_sort_last_updated_first)

    init {
        this.setSortedNoteDescriptors(sortType)
    }

    fun setSortedNoteDescriptors(sortType: Int) {
        allNoteDescriptors = when (sortType) {
            R.id.menu_sort_newest_first -> noteDao.getAllDescriptorsNewestFirst()
            R.id.menu_sort_oldest_first -> noteDao.getAllDescriptorsOldestFirst()
            R.id.menu_sort_notes_first -> noteDao.getAllDescriptorsNotesFirst()
            R.id.menu_sort_lists_first -> noteDao.getAllDescriptorsListsFirst()
            else -> noteDao.getAllDescriptorsLastUpdatedFirst()
        }
        prefs.edit().putInt("sortType", sortType).apply()
    }

    fun getNoteById(id: Long): LiveData<Note> {
        return noteDao.getNoteById(id)
    }

    suspend fun insert(note: Note): Long {
        return noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        // update timestamp
        note.timestamp = Date().time
        noteDao.update(note)
    }

    suspend fun deleteNoteById(id: Long) {
        noteDao.deleteNoteById(id)
    }

    fun getAllDescriptorsNewestFirst(): LiveData<List<NoteDescriptor>> {
        return noteDao.getAllDescriptorsNewestFirst()
    }

    fun getAllDescriptorsOldestFirst(): LiveData<List<NoteDescriptor>> {
        return noteDao.getAllDescriptorsOldestFirst()
    }

    fun getAllDescriptorsLastUpdatedFirst(): LiveData<List<NoteDescriptor>> {
        return noteDao.getAllDescriptorsLastUpdatedFirst()
    }

    fun getAllDescriptorsNotesFirst(): LiveData<List<NoteDescriptor>> {
        return noteDao.getAllDescriptorsNotesFirst()
    }

    fun getAllDescriptorsListsFirst(): LiveData<List<NoteDescriptor>> {
        return noteDao.getAllDescriptorsListsFirst()
    }

}