package com.lucasjwilber.freenote

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView

class TextWatcher {

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
}