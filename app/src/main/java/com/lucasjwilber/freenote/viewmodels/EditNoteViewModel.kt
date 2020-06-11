package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import com.lucasjwilber.freenote.database.NoteRepository
import com.lucasjwilber.freenote.models.Note
import kotlinx.android.synthetic.main.activity_edit_note.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EditNoteViewModel(application: Application): AndroidViewModel(application) {
    private val noteRepository: NoteRepository
    var note: LiveData<Note>? = null
    var noteNeedsToBeSaved: Boolean = false
    var undoButtonIsVisible: Boolean = false

    // segments is updated in the observer in EditNoteActivity
    var segments: MutableLiveData<List<String>> = MutableLiveData()
    var deletedSegments: MutableLiveData<Stack<DeletedSegment>> = MutableLiveData()

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


    fun saveNote(title: String) {
//        var title: String = binding.noteTitleEditText.text.toString()

//        if (title != binding.noteTitleTV.text.toString() ||
//            currentNote.deletedSegments.size > 0 ||
//            currentNote.newSegmentText.isNotEmpty() ||
//            (currentNote.type == NOTE && currentNote.body != segmentsOnOpen) ||
//            (currentNote.type == LIST && currentNote.segments.joinToString(
//                SEGMENT_DELIMITER
//            ) != segmentsOnOpen)
//        ) {
//            currentNote.hasBeenChanged = true
//        }
//
//        if (!currentNote.hasBeenChanged) {
//            Log.i("ljw", "no changes to save")
//            return
//        } else if (currentNote.title.isEmpty() &&
//            noteTitleEditText.text.isEmpty() &&
//            currentNote.body.isEmpty() &&
//            currentNote.segments.size == 0 &&
//            currentNote.newSegmentText.isEmpty()) {
//            Log.i("ljw","no use saving an empty note")
//            return
//        }


        if (currentNote.newSegmentText.isNotEmpty()) {
            currentNote.segments.add(currentNote.newSegmentText)
            currentNote.newSegmentText = ""
            if (newSegmentEditText != null) newSegmentEditText!!.text.clear()
        }

        val text = if (currentNote.type == NOTE) currentNote.body else currentNote.segments.joinToString(
            SEGMENT_DELIMITER
        )
        val note = Note(
            currentNote.id,
            currentNote.type,
            if (title.isEmpty()) "Untitled" else title,
            text,
            Date().time
        )

        GlobalScope.launch(Dispatchers.IO) {
            if (currentNote.isNew) {
                currentNote.id = noteRepository.insert(note)
                currentNote.isNew = false
            } else {
                noteRepository.update(note)
            }
        }

//        showToast(this, getString(R.string.saved))
    }



}