package com.lucasjwilber.freenote.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.ListSegmentsAdapter
import com.lucasjwilber.freenote.R
import com.lucasjwilber.freenote.databinding.ActivityEditListBinding
import com.lucasjwilber.freenote.databinding.ActivityEditNoteBinding
import com.lucasjwilber.freenote.models.Note
import com.lucasjwilber.freenote.viewmodels.EditListViewModel
import com.lucasjwilber.freenote.viewmodels.EditNoteViewModel

class EditListActivity : EditNoteActivity() {
    private lateinit var binding: ActivityEditListBinding
    private lateinit var viewModel: EditNoteViewModel
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProviders.of(this).get(EditListViewModel::class.java)
        viewManager = LinearLayoutManager(this)

        getIdFromIntent()
        setSupportActionBar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.edit_note_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        binding.noteTitleTV.setOnClickListener { changeTitle() }
        binding.deleteModalLayout.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.cancelDeleteButton.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.confirmDeleteButton.setOnClickListener { onDeleteNoteClicked() }

        // if the intent included an id, use it to load the note
        if (id >= 0) {
            supportActionBar?.title = getString(R.string.edit_list)

            viewModel.setNote(id)

            // update the UI when the note is retrieved from the db
            noteObserver = Observer { note ->
                if (note == null) {
                    Log.i("ljw", "note is null")
                    return@Observer
                }
                setTitle(note.title)

                viewAdapter = ListSegmentsAdapter(
                    (viewModel as EditListViewModel).segments,
                    (viewModel as EditListViewModel).deletedSegments
                )

                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }

            viewModel.note?.observe(this, noteObserver)

        } else { //if we got here from clicking the 'new note' button:

            supportActionBar?.title = getString(R.string.create_list)

            // todo: is this actually better than just leaving the title as an ET?
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()



            viewAdapter = ListSegmentsAdapter(
                (viewModel as EditListViewModel).segments,
                (viewModel as EditListViewModel).deletedSegments
            )

            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }


    private fun changeTitle() {
        binding.noteTitleTV.visibility = View.INVISIBLE
        binding.noteTitleEditText.visibility = View.VISIBLE
        binding.noteTitleEditText.requestFocus()
        binding.noteTitleEditText.setSelection(binding.noteTitleEditText.text.length)
        viewModel.titleHasBeenSet = true
    }
}
