package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.lucasjwilber.freenote.NOTE
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import com.lucasjwilber.freenote.database.NoteRepository
import com.lucasjwilber.freenote.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

abstract class BaseViewModel(application: Application): AndroidViewModel(application)  {
    val noteRepository: NoteRepository
    var noteLiveData: LiveData<Note>? = null
    var noteIsBeingDeleted: Boolean = false
    var titleHasBeenSet: Boolean = false
    var titleOnStart: String? = null
    var segmentsOnStart: String? = null

    open var note: Note = Note(
        null,
        NOTE,
        "",
        "",
        Date().time
    )

    init {
        val noteDao: NoteDao = NoteDatabase.getDatabase(application).noteDao()
        // application context is passed to the repository for shared preferences access
        noteRepository =
            NoteRepository(
                noteDao,
                application
            )
    }

    fun getNote(noteId: Long) {
        this.noteLiveData = noteRepository.getNoteById(noteId)
    }

    abstract fun saveNote()

    fun deleteNote() {
        if (noteLiveData?.value?.id != null) {
            // saveNote() is called in onStop(), and deleting a note from the activity calls finish() which calls onStop,
            // so this flag is used to prevent saveNote() being called after a note is deleted
            noteIsBeingDeleted = true

            GlobalScope.launch(Dispatchers.IO) {
                Log.i("ljw", "deleting note " + noteLiveData?.value?.id)
                noteRepository.deleteNoteById(noteLiveData?.value?.id!!)
            }
        }
    }
}