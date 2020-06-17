package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.models.NoteDescriptor
import com.lucasjwilber.freenote.database.NoteRepository
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    private val noteRepository: NoteRepository
    var allNoteDescriptors: LiveData<List<NoteDescriptor>>
    var swipedNoteId: Long? = null
    var swipedNotePosition: Int? = null

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
        noteRepository.setSortedNoteDescriptors(menuItemId)
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

