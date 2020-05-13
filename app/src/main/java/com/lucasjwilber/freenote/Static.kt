package com.lucasjwilber.freenote

import android.content.Context
import android.view.MenuItem
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList

const val SEGMENT_DELIMITER = "|{]"
const val STRIKE_THROUGH_INDICATOR = "[}|"
const val LIST = 0
const val NOTE = 1
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

//fun makeTextWatcher(
//    editText: EditText,
//    counter: TextView,
//    maxLength: Int
//): TextWatcher? {
//    return object : TextWatcher {
//        var currentText: String? = null
//        var cursorPosition = 0
//        override fun beforeTextChanged(
//            s: CharSequence,
//            start: Int,
//            count: Int,
//            after: Int
//        ) {
//            currentText = editText.text.toString()
//            cursorPosition = editText.selectionStart - 1
//        }
//
//        override fun onTextChanged(
//            s: CharSequence,
//            start: Int,
//            before: Int,
//            count: Int
//        ) {
//        }
//
//        override fun afterTextChanged(s: Editable) {
//            if (editText.lineCount > editText.maxLines ||
//                editText.length() - 1 >= maxLength
//            ) {
//                editText.setText(currentText)
//                editText.setSelection(cursorPosition)
//            } else {
//                val counterText = editText.length().toString() + "/" + maxLength
//                counter.text = counterText
//            }
//            val alpha = editText.length().toFloat() / maxLength
//            counter.alpha = alpha
//        }
//    }
//}
