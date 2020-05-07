package com.lucasjwilber.freenote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.lucasjwilber.freenote.databinding.ActivityViewNoteBinding
import kotlinx.coroutines.*

class ViewNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var bundle: Bundle ?= intent.extras
        var message = bundle!!.getString("noteTitle").toString()
        Log.i("ljw", message)
        val title = message?.substring(13)
        binding.viewNoteTitle.text = title

        GlobalScope.launch(Dispatchers.Main) {

            val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()
            val context = this

//            val note: Note = async(Dispatchers.IO) {
//                return@async notesDao.getNote(message)
//            }.await()

//            binding.noteText.text = note.segments

//            viewAdapter = AllNotesAdapter(allNotes, context)
//            binding.allNotesReyclerView.apply {
//                setHasFixedSize(true)
//                layoutManager = viewManager
//                adapter = viewAdapter
//            }
        }
    }
}
