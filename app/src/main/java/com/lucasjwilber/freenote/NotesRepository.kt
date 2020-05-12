package com.lucasjwilber.freenote

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//class NotesRepository(application: Application) {
//
//    private val notesDao: NotesDao
//    private val noteDescriptorsLiveData: LiveData<List<NoteDescriptor>>
//
//    init {
//        val db = AppDatabase.getDatabase(application, GlobalScope)
//        notesDao = db.noteDao()
//        noteDescriptorsLiveData = notesDao.getAllDescriptors()
//    }
//
//    fun getNoteDescriptors(): LiveData<List<NoteDescriptor>> {
//        return noteDescriptorsLiveData
//    }
//
//    fun insert(note: Note) {
//        GlobalScope.launch {
//            notesDao.insert(note)
//        }
//    }
//
//    fun update(note: Note) {
//        GlobalScope.launch {
//            notesDao.insert(note)
//        }
//    }
//}