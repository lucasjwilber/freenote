package com.lucasjwilber.freenote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.databinding.ActivityAllNotesBinding
import kotlinx.coroutines.*
import kotlin.collections.List

class AllNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllNotesBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var allNotes: List<NoteDescriptor> = listOf()
    private var notesFromDb: List<NoteDescriptor> = listOf()
    private var swipedNotePosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        setSupportActionBar(binding.toolbar)
        val pageTitle = "My Notes"
        supportActionBar?.setTitle(pageTitle)

        //init RecyclerView with empty list first. the list is populated in onResume
        viewManager = LinearLayoutManager(this)
        viewAdapter = AllNotesAdapter(allNotes, applicationContext)
        binding.allNotesReyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        binding.deleteModalLayout.setOnClickListener { cancelDelete(swipedNotePosition) }
        binding.cancelDeleteButton.setOnClickListener { cancelDelete(swipedNotePosition) }
        binding.confirmDeleteButton.setOnClickListener { deleteNote(swipedNotePosition) }

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                swipedNotePosition = viewHolder.adapterPosition

                binding.deleteModalLayout.visibility = View.VISIBLE

                val selectedNoteTitle = notesFromDb[swipedNotePosition].title
                val prompt = "Delete \"$selectedNoteTitle\"?"
                binding.deleteModalTextView.text = prompt
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.allNotesReyclerView)


        binding.createNoteButton.setOnClickListener { goToCreateNoteActivity() }
    }



    override fun onResume() {
        super.onResume()
        val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()
        // cache the context so that we can pass it into the RecyclerView adapter
        val context = this

        GlobalScope.launch(Dispatchers.Main) {
            val allNotes: List<NoteDescriptor> = async(Dispatchers.IO) {
                return@async notesDao.getAllDescriptors()
            }.await()

            notesFromDb = allNotes

            viewAdapter = AllNotesAdapter(notesFromDb, context)
            binding.allNotesReyclerView.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }

    private fun cancelDelete(position: Int) {
        binding.deleteModalLayout.visibility = View.GONE

        viewAdapter.notifyDataSetChanged()
    }

    private fun deleteNote(position: Int) {
        val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()

        // cache id before mutating the list
        val noteId = notesFromDb[position].id

        GlobalScope.launch(Dispatchers.IO) {
            notesDao.deleteNoteById(noteId)
        }

        val updatedNotesList = notesFromDb.toMutableList()
        updatedNotesList.removeAt(position)
        notesFromDb = updatedNotesList
        viewAdapter = AllNotesAdapter(notesFromDb, this)
        binding.allNotesReyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        binding.deleteModalLayout.visibility = View.GONE
    }


    private fun goToCreateNoteActivity() {
        //make intent, go to CreateNote.kt
        intent = Intent(applicationContext, EditNoteActivity::class.java)
        startActivity(intent)
    }
}
