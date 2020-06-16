package com.lucasjwilber.freenote.viewmodels

import android.app.Application
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EditListViewModel(application: Application): BaseViewModel(application) {

    val SEGMENT_DELIMITER = "|{]"
    val STRIKE_THROUGH_INDICATOR = "[}|"
    override var note: Note = Note(
        null,
        LIST,
        "",
        "",
        Date().time
    )
    class DeletedSegment(val position: Int, val text: String)

    var segments = ArrayList<String>()
    var deletedSegments: MutableLiveData<Stack<DeletedSegment>> = MutableLiveData()
    var newSegmentText: MutableLiveData<String> = MutableLiveData()

    init {
        deletedSegments.value = Stack<DeletedSegment>()
    }

    override fun saveNote() {
        // don't save notes that haven't been changed, because this would make the sort-by-last-updated
        // method effectively mean sort-by-last-viewed
        if (noteIsBeingDeleted) return

        // turn the segments ArrayList into a String
        note.segments = segments.joinToString(SEGMENT_DELIMITER)
        // don't save if nothing has changed
        if (note.title == titleOnStart && note.segments == segmentsOnStart) return

        GlobalScope.launch(Dispatchers.IO) {
            if (note.id == null) { // save a new note
                // insert returns the autogenerated id
                note.id = noteRepository.insert(note)
            } else {
                noteRepository.update(note)
            }
        }

        showToast(getApplication(), "Saved")
    }

}