package com.lucasjwilber.freenote

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList

const val SEGMENT_DELIMITER = "|{]"
const val STRIKE_THROUGH_INDICATOR = "[}|"
const val LIST = 0
const val NOTE = 1
const val TW_NEW_SEGMENT = 2
const val TW_UPDATED_SEGMENT = 3
const val TW_NOTE_BODY = 4
var newSegmentEditText: EditText? = null
var undoButton: MenuItem? = null

class DeletedSegment(val position: Int, val text: String)
class CurrentNote(
    var id: Int? = null,
    var type: Int = NOTE,
    var isNew: Boolean = false,
    var title: String = "",
    var body: String = "",
    var segments: ArrayList<String> = ArrayList(),
    var deletedSegments: Stack<DeletedSegment> = Stack(),
    var newSegmentText: String = "",
    var currentlyEditedSegmentPosition: Int? = null,
    var hasBeenChanged: Boolean = false
)
var currentNote: CurrentNote = CurrentNote()

fun showToast(context: Context, message: String) {
    val toast: Toast = Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    )
    val toastView = toast.view
    toastView.background = context.resources.getDrawable(R.drawable.toast_background)
    toast.show()
}

fun makeTextWatcher(type: Int): TextWatcher? {
    if (type == TW_NEW_SEGMENT) {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNote.newSegmentText = s.toString()
            }
            override fun afterTextChanged(s: Editable) {}
        }
    } else if (type == TW_UPDATED_SEGMENT) {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNote.segments[currentNote.currentlyEditedSegmentPosition!!] = s.toString()
                currentNote.hasBeenChanged = true
            }
            override fun afterTextChanged(s: Editable) {}
        }
    } else {//if (type == TW_NOTE_BODY) {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNote.hasBeenChanged = true
                currentNote.body = s.toString()
            }
            override fun afterTextChanged(s: Editable) {}
        }
    }
}

