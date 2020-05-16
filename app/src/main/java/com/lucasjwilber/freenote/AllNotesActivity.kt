package com.lucasjwilber.freenote

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
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
    private var swipedNotePosition: Int? = null
    private var allNotesActivityContext: Context = this
    private var viewModel: NoteDescriptorsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.my_notes)

        //TODO: sorting
        //binding.toolbar.inflateMenu(R.menu.all_notes_menu)

        // init recyclerview and add a LiveData observer to the dataset
        viewManager = LinearLayoutManager(this)
        viewModel = ViewModelProviders.of(this).get(NoteDescriptorsViewModel::class.java)
        viewModel?.allNoteDescriptors?.observe(this, Observer { data ->
                allNotes = data!!
                viewAdapter = AllNotesAdapter(allNotes, allNotesActivityContext)
                binding.allNotesRecyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            })

        binding.createNoteButton.setOnClickListener { binding.selectTypeBackground.visibility = View.VISIBLE }
        binding.selectTypeBackground.setOnClickListener { binding.selectTypeBackground.visibility = View.GONE }
        binding.selectNoteButton.setOnClickListener { goToEditNoteActivity(NOTE) }
        binding.selectListButton.setOnClickListener { goToEditNoteActivity(LIST) }

        binding.deleteModalLayout.setOnClickListener { cancelDelete() }
        binding.cancelDeleteButton.setOnClickListener { cancelDelete() }
        binding.confirmDeleteButton.setOnClickListener { deleteNote(swipedNotePosition!!) }

        initSwipeListener()
    }

    override fun onResume() {
        super.onResume()
        newSegmentEditText = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.selectTypeOptionsContainer.orientation = LinearLayout.HORIZONTAL
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.selectTypeOptionsContainer.orientation = LinearLayout.VERTICAL
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.all_notes_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        //
//        return super.onOptionsItemSelected(item)
//    }

    override fun onBackPressed() {
        if (binding.selectTypeBackground.visibility == View.VISIBLE) {
            binding.selectTypeBackground.visibility = View.GONE
            return
        } else {
            finish()
        }
    }

    private fun cancelDelete() {
        binding.deleteModalLayout.visibility = View.GONE
        viewAdapter.notifyItemChanged(swipedNotePosition!!)
    }

    private fun deleteNote(position: Int) {
        // cache id before mutating the list
        val noteId = allNotes[position].id

        GlobalScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase(application).noteDao().deleteNoteById(noteId)
        }

        val updatedNotesList = allNotes.toMutableList()
        updatedNotesList.removeAt(position)
        allNotes = updatedNotesList
        viewAdapter.notifyItemRemoved(position)
        binding.deleteModalLayout.visibility = View.GONE
    }

    private fun goToEditNoteActivity(type: Int) {
        intent = Intent(applicationContext, EditNoteActivity::class.java)
        currentNote = CurrentNote()
        currentNote.type = type
        currentNote.isNew = true
        startActivity(intent)
    }

    private fun initSwipeListener() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                binding.deleteModalLayout.visibility = View.VISIBLE

                swipedNotePosition = viewHolder.adapterPosition
                val selectedNoteTitle = allNotes[swipedNotePosition!!].title
                val prompt = "Permanently delete \"$selectedNoteTitle\"?"
                binding.deleteModalTextView.text = prompt
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.allNotesRecyclerView)
    }
}
