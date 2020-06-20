package com.lucasjwilber.freenote.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getInt("theme", THEME_CAFE)
        Log.i("ljw", "theme in prefs is $theme, in app is " + getTheme().toString())
        when (theme) {
            THEME_CAFE -> setTheme(R.style.CafeTheme)
            THEME_CITY -> setTheme(R.style.CityTheme)
        }
        Log.i("ljw", "theme in prefs is $theme, in app is " + getTheme().toString())

        super.onCreate(savedInstanceState)
        binding = ActivityAllNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.my_notes)
        binding.toolbar.inflateMenu(R.menu.all_notes_menu)

        // init recyclerview and add a LiveData observer to the dataset
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

        // event listeners
        binding.createNewNoteOrListButton.setOnClickListener { binding.selectTypeBackground.visibility = View.VISIBLE }
        binding.selectTypeBackground.setOnClickListener { binding.selectTypeBackground.visibility = View.GONE }
        binding.selectNoteButton.setOnClickListener { goToEditNoteActivity(NOTE) }
        binding.selectListButton.setOnClickListener { goToEditNoteActivity(LIST) }
        binding.deleteModalLayout.setOnClickListener { cancelDeleteNote() }
        binding.cancelDeleteButton.setOnClickListener { cancelDeleteNote() }
        binding.confirmDeleteButton.setOnClickListener { deleteNote() }
        // swipe-to-delete recyclerview listener
        initSwipeListener()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // change the layout of the note type options to best fit the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.selectTypeOptionsContainer.orientation = LinearLayout.HORIZONTAL
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.selectTypeOptionsContainer.orientation = LinearLayout.VERTICAL
        }
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
                item.itemId == R.id.menu_theme_city) {
            viewModel?.setThemeInPrefs(item.itemId)
            recreate()
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        if (binding.selectTypeBackground.visibility == View.VISIBLE) {
            binding.selectTypeBackground.visibility = View.GONE
            return
        } else {
            finish()
        }
    }


    private fun cancelDeleteNote() {
        binding.deleteModalLayout.visibility = View.GONE
        // re-insert the swiped away view
        viewAdapter.notifyItemChanged(viewModel?.swipedNotePosition!!)
    }


    private fun deleteNote() {
        viewModel?.deleteSwipedNote()
        binding.deleteModalLayout.visibility = View.GONE
        showToast(this, getString(R.string.note_deleted))
    }


    private fun goToEditNoteActivity(type: Int) {
        val destination =
            if (type == NOTE) EditNoteActivity::class.java
            else EditListActivity::class.java
        intent = Intent(applicationContext, destination)

        binding.selectTypeBackground.visibility = View.GONE
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

                binding.deleteModalLayout.visibility = View.VISIBLE
                val selectedNoteTitle = allNotes[swipedNotePosition].title
                val prompt = "Permanently delete \"$selectedNoteTitle\"?"
                binding.deleteModalTextView.text = prompt
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.allNotesRecyclerView)
    }
}
