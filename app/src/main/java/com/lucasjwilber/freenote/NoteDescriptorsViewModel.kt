package com.lucasjwilber.freenote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class NoteDescriptorsViewModel(application: Application) : AndroidViewModel(application) {
    private val notesDao: NotesDao = AppDatabase.getDatabase(application).noteDao()
    internal val allNoteDescriptors: LiveData<List<NoteDescriptor>>
    init {
        allNoteDescriptors = notesDao.getAllDescriptors()
    }
}