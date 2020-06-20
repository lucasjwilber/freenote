package com.lucasjwilber.freenote.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.lucasjwilber.freenote.models.Note

abstract class BaseActivity : AppCompatActivity() {
    var id: Long? = null
    var deleteButton: MenuItem? = null
    lateinit var noteObserver: Observer<in Note>

    fun getIdFromIntent() {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            id = extras.getLong("id")
        }
    }

}
