package com.lucasjwilber.freenote.views

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.databinding.ActivityAllNotesBinding
import com.lucasjwilber.freenote.AllNotesAdapter
import com.lucasjwilber.freenote.models.NoteDescriptor
import com.lucasjwilber.freenote.viewmodels.AllNotesViewModel
import kotlin.collections.List

class AllNotesActivity : AppCompatActivity() {
    lateinit var binding: ActivityAllNotesBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var viewModel: AllNotesViewModel? = null
    private var allNotes: List<NoteDescriptor> = listOf()
    private lateinit var observer: Observer<in List<NoteDescriptor>>
    private lateinit var deleteModal: ConstraintLayout
    private lateinit var selectTypeModal: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.init(application)
        setTheme(ThemeManager.getTheme())

        super.onCreate(savedInstanceState)
        binding = ActivityAllNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.all_notes_menu)
        supportActionBar?.title = getString(R.string.my_notes)

        // init recyclerview and add a LiveData observer to the dataset
        binding.allNotesRecyclerView.setBackgroundColor(resources.getColor(ThemeManager.getRecyclerViewBackgroundColor()))
        viewManager = LinearLayoutManager(this)
        viewModel = ViewModelProviders.of(this).get(AllNotesViewModel::class.java)
        observer = Observer { data ->
            allNotes = data!!
            viewAdapter = AllNotesAdapter(
                allNotes,
                this
            )
            binding.allNotesRecyclerView.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
        viewModel?.allNoteDescriptors?.observe(this, observer)

        selectTypeModal = findViewById(R.id.selectTypeModalFragment)
        binding.createNewNoteOrListButton.setOnClickListener {
            selectTypeModal.visibility = View.VISIBLE
        }
        selectTypeModal.setOnClickListener { selectTypeModal.visibility = View.GONE }
        selectTypeModal.findViewById<ImageButton>(R.id.selectNoteButton).setOnClickListener { goToEditNoteActivity(NOTE) }
        selectTypeModal.findViewById<ImageButton>(R.id.selectListButton).setOnClickListener { goToEditNoteActivity(LIST) }

        deleteModal = findViewById(R.id.deleteModalFragment)
        deleteModal.setOnClickListener { cancelDeleteNote() }
        deleteModal.findViewById<Button>(R.id.cancelDeleteButton).setOnClickListener { cancelDeleteNote() }
        deleteModal.findViewById<Button>(R.id.confirmDeleteButton).setOnClickListener { deleteNote() }
        // swipe-to-delete recyclerview listener
        initSwipeListener()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // change the layout of the note type options to best fit the screen
        selectTypeModal.findViewById<LinearLayout>(R.id.selectTypeOptionsContainer).orientation =
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                LinearLayout.HORIZONTAL
            else
                LinearLayout.VERTICAL
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.all_notes_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_sort_lists_first ||
            item.itemId == R.id.menu_sort_notes_first ||
            item.itemId == R.id.menu_sort_last_updated_first ||
            item.itemId == R.id.menu_sort_oldest_first ||
            item.itemId == R.id.menu_sort_newest_first
        ) {
            viewModel?.updateSortType(item.itemId)
            // force an observer update to update the recyclerview contents
            viewModel?.allNoteDescriptors?.observe(this, observer)
        } else if (item.itemId == R.id.menu_theme_cafe ||
                item.itemId == R.id.menu_theme_city ||
                item.itemId == R.id.menu_theme_rose ||
                item.itemId == R.id.menu_theme_lavender ||
                item.itemId == R.id.menu_theme_arctic ||
                item.itemId == R.id.menu_theme_honey) {
            ThemeManager.changeTheme(item.itemId)
            recreate()
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        if (selectTypeModal.visibility == View.VISIBLE) {
            selectTypeModal.visibility = View.GONE
            return
        } else if (deleteModal.visibility == View.VISIBLE) {
            cancelDeleteNote()
        } else {
            finish()
        }
    }


    private fun cancelDeleteNote() {
        deleteModal.visibility = View.GONE
        // re-insert the swiped away view
        viewAdapter.notifyItemChanged(viewModel?.swipedNotePosition!!)
    }


    private fun deleteNote() {
        viewModel?.deleteSwipedNote()
        deleteModal.visibility = View.GONE
        showToast(this, getString(R.string.note_deleted))
    }


    private fun goToEditNoteActivity(type: Int) {
        val destination =
            if (type == NOTE) EditNoteActivity::class.java
            else EditListActivity::class.java
        intent = Intent(applicationContext, destination)

        selectTypeModal.visibility = View.GONE
        startActivity(intent)
    }


    private fun initSwipeListener() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                return false
            }

            // display delete modal, hold a reference to the swiped away note
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                // cache the swiped note position/id in the viewmodel for deletion or re-insertion
                val swipedNotePosition = viewHolder.adapterPosition
                viewModel?.swipedNotePosition = swipedNotePosition
                viewModel?.swipedNoteId = allNotes[swipedNotePosition].id

                deleteModal.visibility = View.VISIBLE
                val selectedNoteTitle = allNotes[swipedNotePosition].title
                val prompt = "Permanently delete \"$selectedNoteTitle\"?"
                deleteModal.findViewById<TextView>(R.id.deleteModalTextView).text = prompt
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.allNotesRecyclerView)
    }

}
