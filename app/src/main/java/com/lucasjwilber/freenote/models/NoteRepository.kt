package com.lucasjwilber.freenote.models

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.*

class NoteRepository(private val noteDao: NoteDao, context: Context) {

    var allNoteDescriptors: LiveData<List<NoteDescriptor>> = this.getSortedNoteDescriptors()
    private val prefs: SharedPreferences = context.getSharedPreferences("freenote_prefs", Context.MODE_PRIVATE)

    private fun getSortedNoteDescriptors(): LiveData<List<NoteDescriptor>> {
        if (prefs == null) return noteDao.getAllDescriptorsLastUpdatedFirst()

        return when (prefs.getInt("sortType", SORT_TYPE_LAST_UPDATED_FIRST)) {
            SORT_TYPE_NEWEST_FIRST -> noteDao.getAllDescriptorsNewestFirst()
            SORT_TYPE_OLDEST_FIRST -> noteDao.getAllDescriptorsOldestFirst()
            SORT_TYPE_NOTES_FIRST -> noteDao.getAllDescriptorsNotesFirst()
            SORT_TYPE_LISTS_FIRST -> noteDao.getAllDescriptorsListsFirst()
            else -> noteDao.getAllDescriptorsLastUpdatedFirst()
        }
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