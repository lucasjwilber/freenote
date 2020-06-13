package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.lucasjwilber.freenote.*
import java.util.*

class EditListViewModel(application: Application): EditNoteViewModel(application) {
    var undoButtonIsVisible: Boolean = false

    // segments is updated in the observer in EditNoteActivity
    var segments: MutableLiveData<List<String>> = MutableLiveData()
    var deletedSegments: MutableLiveData<Stack<DeletedSegment>> = MutableLiveData()

    init {
        segments.value = ArrayList<String>()
        deletedSegments.value = Stack<DeletedSegment>()
    }

    override fun setNote(noteId: Long) {
        note = noteRepository.getNoteById(noteId)
        titleHasBeenSet = true
        segments.value = note?.value?.segments?.split(SEGMENT_DELIMITER)
    }

}