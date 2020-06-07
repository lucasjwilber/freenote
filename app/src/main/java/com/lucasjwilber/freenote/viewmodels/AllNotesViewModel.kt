package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.models.NoteDescriptor
import com.lucasjwilber.freenote.database.NoteRepository
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    private val noteRepository: NoteRepository
//    internal lateinit var allNoteDescriptors: LiveData<List<NoteDescriptor>>
    var allNoteDescriptors: LiveData<List<NoteDescriptor>>
    var swipedNoteId: Long? = null
    var swipedNotePosition: Int? = null
    var showDeleteNoteModal: Boolean = false
//    var sortType = application.getSharedPreferences("freenote_prefs", Context.MODE_PRIVATE)
//        .getInt("sortType", SORT_TYPE_LAST_UPDATED_FIRST)

//    private val prefs: SharedPreferences = application.getSharedPreferences("freenote_prefs", Context.MODE_PRIVATE)

    init {
        val noteDao: NoteDao = NoteDatabase.getDatabase(application).noteDao()
        // Application context is passed to the repository for shared preferences access:
        noteRepository =
            NoteRepository(
                noteDao,
                application
            )
        allNoteDescriptors = noteRepository.allNoteDescriptors
    }


    fun updateSortType(menuItemId: Int): LiveData<List<NoteDescriptor>> {
        val sortType = when (menuItemId) {
            R.id.menu_sort_newest_first -> SORT_TYPE_NEWEST_FIRST
            R.id.menu_sort_oldest_first -> SORT_TYPE_OLDEST_FIRST
            R.id.menu_sort_notes_first -> SORT_TYPE_NOTES_FIRST
            R.id.menu_sort_lists_first -> SORT_TYPE_LISTS_FIRST
            else -> SORT_TYPE_LAST_UPDATED_FIRST
        }
        noteRepository.setSortedNoteDescriptors(sortType)
        allNoteDescriptors = noteRepository.allNoteDescriptors
        return allNoteDescriptors

    }


    fun deleteSwipedNote() {
        GlobalScope.launch(Dispatchers.IO) {
            NoteDatabase.getDatabase(
                getApplication()
            ).noteDao().deleteNoteById(swipedNoteId!!)
        }
    }


}

