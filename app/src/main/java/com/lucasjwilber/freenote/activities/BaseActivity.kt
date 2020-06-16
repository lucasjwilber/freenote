package com.lucasjwilber.freenote.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.lucasjwilber.freenote.R
import com.lucasjwilber.freenote.models.Note

abstract class BaseActivity : AppCompatActivity() {

    var id: Long = -1
    var deleteButton: MenuItem? = null
    lateinit var noteObserver: Observer<in Note>


    fun getIdFromIntent() {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            id = extras.getLong("id")
        }
    }


}
