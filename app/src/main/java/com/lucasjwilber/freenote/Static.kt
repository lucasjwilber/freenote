package com.lucasjwilber.freenote

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList

var currentNoteTitle: String = ""
var currentNoteSegments: ArrayList<String> = ArrayList<String>()
class DeletedSegment(val position: Int, val text: String)
var currentNoteDeletedSegments: Stack<DeletedSegment> = Stack()
var currentNoteHasBeenChanged: Boolean = false


fun showToast(context: Context, message: String) {
    val toast: Toast = Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    )
    val toastView = toast.view
    toastView.setBackground(context.getResources().getDrawable(R.drawable.toast_background))
    toast.show()
}

fun makeTextWatcher(
    editText: EditText,
    counter: TextView,
    maxLength: Int
): TextWatcher? {
    return object : TextWatcher {
        var currentText: String? = null
        var cursorPosition = 0
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
            currentText = editText.text.toString()
            cursorPosition = editText.selectionStart - 1
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
        }

        override fun afterTextChanged(s: Editable) {
            if (editText.lineCount > editText.maxLines ||
                editText.length() - 1 >= maxLength
            ) {
                editText.setText(currentText)
                editText.setSelection(cursorPosition)
            } else {
                val counterText = editText.length().toString() + "/" + maxLength
                counter.text = counterText
            }
            val alpha = editText.length().toFloat() / maxLength
            counter.alpha = alpha
        }
    }
}