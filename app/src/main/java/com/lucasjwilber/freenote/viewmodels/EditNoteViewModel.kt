package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import com.lucasjwilber.freenote.database.NoteRepository
import com.lucasjwilber.freenote.models.Note

class EditNoteViewModel(application: Application): AndroidViewModel(application) {
    private val noteRepository: NoteRepository
    var note: LiveData<Note>? = null

    init {
        val noteDao: NoteDao = NoteDatabase.getDatabase(application).noteDao()
        // Application context is passed to the repository for shared preferences access:
        noteRepository =
            NoteRepository(
                noteDao,
                application
            )
    }

    fun setNote(noteId: Long) {
        note = noteRepository.getNoteById(noteId)
    }
}