package com.lucasjwilber.freenote

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
    private var swipedNotePosition: Int = 0
    private var allNotesActivityContext: Context = this
    private var viewModel: NoteDescriptorsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "My Notes"

        // init recyclerview and add a LiveData observer
        viewManager = LinearLayoutManager(this)
        viewModel = ViewModelProviders.of(this).get(NoteDescriptorsViewModel::class.java)
        viewModel?.allNoteDescriptors?.observe(this, object : Observer<List<NoteDescriptor>> {
            override fun onChanged(data: List<NoteDescriptor>?) {
                allNotes = data!!
                viewAdapter = AllNotesAdapter(allNotes, allNotesActivityContext)
                binding.allNotesReyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }
        })

        binding.deleteModalLayout.setOnClickListener { cancelDelete() }
        binding.cancelDeleteButton.setOnClickListener { cancelDelete() }
        binding.confirmDeleteButton.setOnClickListener { deleteNote(swipedNotePosition) }
        binding.createNoteButton.setOnClickListener { goToCreateNoteActivity() }

        // swipe listener
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                swipedNotePosition = viewHolder.adapterPosition

                binding.deleteModalLayout.visibility = View.VISIBLE

                val selectedNoteTitle = allNotes[swipedNotePosition].title
                val prompt = "Permanently delete \"$selectedNoteTitle\"?"
                binding.deleteModalTextView.text = prompt
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.allNotesReyclerView)
    }

    private fun cancelDelete() {
        binding.deleteModalLayout.visibility = View.GONE
        viewAdapter.notifyDataSetChanged()
    }

    private fun deleteNote(position: Int) {
        val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()

        // cache id before mutating the list
        val noteId = allNotes[position].id

        GlobalScope.launch(Dispatchers.IO) {
            notesDao.deleteNoteById(noteId)
        }

        val updatedNotesList = allNotes.toMutableList()
        updatedNotesList.removeAt(position)
        allNotes = updatedNotesList
        viewAdapter = AllNotesAdapter(allNotes, this)
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
