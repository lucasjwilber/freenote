package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.models.Note
import java.util.*

class EditListViewModel(application: Application): BaseViewModel(application) {
    var undoButtonIsVisible: Boolean = false

    override var note: Note = Note(
        null,
        LIST,
        "",
        "",
        Date().time
    )

    // segments is updated in the observer in EditNoteActivity
    var segments: MutableLiveData<List<String>> = MutableLiveData()
    var deletedSegments: MutableLiveData<Stack<DeletedSegment>> = MutableLiveData()

    init {
        segments.value = ArrayList<String>()
        deletedSegments.value = Stack<DeletedSegment>()
    }

}