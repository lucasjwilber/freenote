package com.lucasjwilber.freenote.activities

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.databinding.ActivityEditListBinding
import com.lucasjwilber.freenote.viewmodels.EditListViewModel
import java.util.*

class EditListActivity : BaseActivity() {
    lateinit var binding: ActivityEditListBinding
    lateinit var viewModel: EditListViewModel
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var deletedSegmentsObserver: Observer<in Stack<EditListViewModel.DeletedSegment>>
    private var undoButton: MenuItem? = null
    private var titleTextWatcher: TextWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.note.title = s.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getInt("theme", THEME_CAFE)
        Log.i("ljw", "theme in prefs is $theme, in app is " + getTheme().toString())
        when (theme) {
            THEME_CAFE -> setTheme(R.style.CafeTheme)
            THEME_CITY -> setTheme(R.style.CityTheme)
        }
        Log.i("ljw", "theme in prefs is $theme, in app is " + getTheme().toString())

        super.onCreate(savedInstanceState)

        binding = ActivityEditListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        this.setSupportActionBar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.edit_note_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(EditListViewModel::class.java)
        viewManager = LinearLayoutManager(this)


        binding.noteTitleTV.setOnClickListener { changeTitle() }
        binding.deleteModalLayout.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.cancelDeleteButton.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.confirmDeleteButton.setOnClickListener { onDeleteNoteClicked() }


        getIdFromIntent()
        // if the intent included an id, use it to load the note
        if (id != null) {
            supportActionBar?.title = getString(R.string.edit_list)

            viewModel.getNote(id!!)

            // update the UI when the note is retrieved from the db
            noteObserver = Observer { note ->
                if (note == null) {
                    Log.i("ljw", "note is null")
                    return@Observer
                }
                setTitle(note.title)
                viewModel.note = note
                viewModel.titleOnStart = note.title
                viewModel.segmentsOnStart = note.segments

                // if note.segments is empty, splitting it will create an empty element, so use an
                // empty ArrayList instead:
                viewModel.segments = if (note.segments.isNotEmpty())
                    ArrayList(note.segments.split(viewModel.SEGMENT_DELIMITER))
                else
                    ArrayList<String>()

                viewAdapter = ListSegmentsAdapter(viewModel)
                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }

            viewModel.noteLiveData?.observe(this, noteObserver)

        } else { //if we got here from clicking the 'new note' button:
            supportActionBar?.title = getString(R.string.create_list)

            // "suggest" the user set the title first by focusing it
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()
            binding.noteTitleEditText.addTextChangedListener(titleTextWatcher)

            viewAdapter = ListSegmentsAdapter(viewModel)
            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }

        deletedSegmentsObserver = Observer { stack ->
            undoButton?.isVisible = stack.isNotEmpty()
        }
        viewModel.deletedSegments.observe(this, deletedSegmentsObserver)
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveNote()
        deleteButton?.isVisible = true
    }


    private fun changeTitle() {
        binding.noteTitleTV.visibility = View.INVISIBLE
        binding.noteTitleEditText.visibility = View.VISIBLE
        binding.noteTitleEditText.requestFocus()
        binding.noteTitleEditText.setSelection(binding.noteTitleEditText.text.length)
        binding.noteTitleEditText.addTextChangedListener(titleTextWatcher)
        viewModel.titleHasBeenSet = true
    }

    private fun setTitle(title: String) {
        val titleText = if (title.isEmpty()) getString(R.string.untitled) else title
        binding.noteTitleTV.text = titleText
        binding.noteTitleEditText.setText(titleText)
        viewModel.note.title = titleText
    }


    private fun onDeleteNoteClicked() {
        viewModel.deleteNote()
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_note_menu, menu)

        deleteButton = menu?.getItem(1)

        // the same menu is used for notes and lists. notes don't have an undo feature, so hide it.
        undoButton = menu?.findItem(R.id.action_undo)

        // hide the delete option on notes that haven't been saved yet
        if (viewModel.noteLiveData?.value == null) {
            deleteButton?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            binding.deleteModalLayout.visibility = View.VISIBLE
        } else if (item.itemId == R.id.action_undo) {
            undoSegmentDelete()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun undoSegmentDelete() {
        val delseg: EditListViewModel.DeletedSegment = viewModel.deletedSegments.value!!.pop()
        viewModel.deletedSegments.value = viewModel.deletedSegments.value
        viewModel.segments.add(delseg.position, delseg.text)
        viewAdapter.notifyItemInserted(delseg.position)
    }
}
