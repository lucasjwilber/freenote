package com.lucasjwilber.freenote

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class NoteDescriptorsViewModel(application: Application) : AndroidViewModel(application) {
    private val notesDao: NotesDao = AppDatabase.getDatabase(application).noteDao()
    internal lateinit var allNoteDescriptors: LiveData<List<NoteDescriptor>>
    private val prefs: SharedPreferences = application.getSharedPreferences("freenote_prefs", Context.MODE_PRIVATE)

    init {
        refreshSortType()
    }

    fun refreshSortType() {
        allNoteDescriptors = when (prefs.getInt("sortType", SORT_TYPE_LAST_UPDATED_FIRST)) {
            SORT_TYPE_NEWEST_FIRST -> notesDao.getAllDescriptorsNewestFirst()
            SORT_TYPE_OLDEST_FIRST -> notesDao.getAllDescriptorsOldestFirst()
            SORT_TYPE_NOTES_FIRST-> notesDao.getAllDescriptorsNotesFirst()
            SORT_TYPE_LISTS_FIRST -> notesDao.getAllDescriptorsListsFirst()
            else -> notesDao.getAllDescriptorsLastUpdatedFirst()
        }
    }
}