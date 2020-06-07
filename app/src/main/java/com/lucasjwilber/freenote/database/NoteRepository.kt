package com.lucasjwilber.freenote.database

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.models.Note
import com.lucasjwilber.freenote.models.NoteDescriptor

class NoteRepository(private val noteDao: NoteDao, context: Context) {

    lateinit var allNoteDescriptors: LiveData<List<NoteDescriptor>>
    private val prefs: SharedPreferences = context.getSharedPreferences("freenote_prefs", Context.MODE_PRIVATE)
    private var sortType: Int = prefs.getInt("sortType", SORT_TYPE_LAST_UPDATED_FIRST)


    init {
        this.setSortedNoteDescriptors(sortType)
    }

    fun setSortedNoteDescriptors(sortType: Int) {
        allNoteDescriptors = when (sortType) {
            SORT_TYPE_NEWEST_FIRST -> noteDao.getAllDescriptorsNewestFirst()
            SORT_TYPE_OLDEST_FIRST -> noteDao.getAllDescriptorsOldestFirst()
            SORT_TYPE_NOTES_FIRST -> noteDao.getAllDescriptorsNotesFirst()
            SORT_TYPE_LISTS_FIRST -> noteDao.getAllDescriptorsListsFirst()
            else -> noteDao.getAllDescriptorsLastUpdatedFirst()
        }
        prefs.edit().putInt("sortType", sortType).apply()
    }


    suspend fun insert(note: Note): Long {
        return noteDao.insert(note)
    }


    suspend fun update(note: Note) {
        noteDao.update(note)
    }


    suspend fun deleteNoteById(id: Long) {
        noteDao.deleteNoteById(id)
    }

}