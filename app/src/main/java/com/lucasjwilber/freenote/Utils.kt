package com.lucasjwilber.freenote

import android.content.Context
import android.widget.Toast

const val LIST = 0
const val NOTE = 1
const val PREFERENCES = "freenote-prefs"

fun showToast(context: Context, message: String) {
    val toast: Toast = Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    )
    toast.view.background = context.resources.getDrawable(ThemeManager.getToastBackground())
    toast.show()
}




