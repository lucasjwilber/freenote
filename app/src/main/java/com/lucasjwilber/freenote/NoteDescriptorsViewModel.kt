package com.lucasjwilber.freenote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class NoteDescriptorsViewModel(application: Application) : AndroidViewModel(application) {

//    private val notesRepository: NotesRepository
    private val notesDao: NotesDao
    internal val allNoteDescriptors: LiveData<List<NoteDescriptor>>

    init {
//        notesRepository = NotesRepository(application)
        notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()
//        allWords = notesRepository.getNoteDescriptors()
        allNoteDescriptors = notesDao.getAllDescriptors()
    }

    fun insert(note: Note) {
//        notesRepository.insert(note)
    }
}