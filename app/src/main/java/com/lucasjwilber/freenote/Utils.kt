package com.lucasjwilber.freenote

import android.content.Context
import android.widget.Toast

const val LIST = 0
const val NOTE = 1
const val THEME_CAFE = 2
const val THEME_CITY = 3
const val PREFERENCES = "freenote-prefs"

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

