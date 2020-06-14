package com.lucasjwilber.freenote.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.ListSegmentsAdapter
import com.lucasjwilber.freenote.R
import com.lucasjwilber.freenote.SEGMENT_DELIMITER
import com.lucasjwilber.freenote.databinding.ActivityEditListBinding
import com.lucasjwilber.freenote.viewmodels.EditListViewModel

class EditListActivity : BaseActivity() {
    private var binding = ActivityEditListBinding.inflate(layoutInflater)
    private lateinit var viewModel: EditListViewModel
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = binding.root
        setContentView(view)

        this.setSupportActionBar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.edit_note_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(EditListViewModel::class.java)
        viewManager = LinearLayoutManager(this)

        getIdFromIntent()

        binding.noteTitleTV.setOnClickListener { changeTitle() }
        binding.deleteModalLayout.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.cancelDeleteButton.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.confirmDeleteButton.setOnClickListener { onDeleteNoteClicked() }


        // if the intent included an id, use it to load the note
        if (id >= 0) {
            supportActionBar?.title = getString(R.string.edit_list)

            viewModel.getNote(id)

            // update the UI when the note is retrieved from the db
            noteObserver = Observer { note ->
                if (note == null) {
                    Log.i("ljw", "note is null")
                    return@Observer
                }
                setTitle(note.title)

                viewModel.note = note

                viewModel.segments.value = note.segments.split(SEGMENT_DELIMITER)

                viewAdapter = ListSegmentsAdapter(
                    viewModel.segments,
                    viewModel.deletedSegments
                )

                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }

            viewModel.noteLiveData?.observe(this, noteObserver)

        } else { //if we got here from clicking the 'new note' button:

            supportActionBar?.title = getString(R.string.create_list)

            // todo: is this actually better than just leaving the title as an ET?
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()

            viewAdapter = ListSegmentsAdapter(
                viewModel.segments,
                viewModel.deletedSegments
            )

            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (saveOnStop) {
            val title: String = binding.noteTitleEditText.text.toString()
            val body: String = viewModel.segments.value!!.joinToString(SEGMENT_DELIMITER)

            viewModel.note.title = title
            viewModel.note.segments = body

            viewModel.saveNote()

            deleteButton?.isVisible = true
        }
    }


    private fun changeTitle() {

        binding.noteTitleTV.visibility = View.INVISIBLE
        binding.noteTitleEditText.visibility = View.VISIBLE
        binding.noteTitleEditText.requestFocus()
        binding.noteTitleEditText.setSelection(binding.noteTitleEditText.text.length)
        viewModel.titleHasBeenSet = true
    }

    fun setTitle(title: String) {
        val titleText = if (title.isEmpty()) getString(R.string.untitled) else title
        binding.noteTitleTV.text = titleText
        binding.noteTitleEditText.setText(titleText)
    }


    fun onDeleteNoteClicked() {
        Log.i("ljw", "delete note clicked")
        saveOnStop = false
        viewModel.deleteNote()
        finish()
    }
}
