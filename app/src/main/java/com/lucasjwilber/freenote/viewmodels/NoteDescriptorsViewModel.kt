package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.models.NoteDescriptor
import com.lucasjwilber.freenote.models.NoteRepository

class NoteDescriptorsViewModel(application: Application) : AndroidViewModel(application) {

    private val noteRepository: NoteRepository

//    internal lateinit var allNoteDescriptors: LiveData<List<NoteDescriptor>>
    var allNoteDescriptors: LiveData<List<NoteDescriptor>>

//    private val prefs: SharedPreferences = application.getSharedPreferences("freenote_prefs", Context.MODE_PRIVATE)

    init {
        val noteDao: NoteDao = NoteDatabase.getDatabase(application).noteDao()
        // Application context is passed to the repository for shared preferences access:
        noteRepository = NoteRepository(noteDao, application)
        allNoteDescriptors = noteRepository.allNoteDescriptors
//        refreshSortType()
    }

//    fun refreshSortType() {
//        allNoteDescriptors = when (prefs.getInt("sortType",
//            SORT_TYPE_LAST_UPDATED_FIRST
//        )) {
//            SORT_TYPE_NEWEST_FIRST -> noteDao.getAllDescriptorsNewestFirst()
//            SORT_TYPE_OLDEST_FIRST -> noteDao.getAllDescriptorsOldestFirst()
//            SORT_TYPE_NOTES_FIRST -> noteDao.getAllDescriptorsNotesFirst()
//            SORT_TYPE_LISTS_FIRST -> noteDao.getAllDescriptorsListsFirst()
//            else -> noteDao.getAllDescriptorsLastUpdatedFirst()
//        }
//    }
}