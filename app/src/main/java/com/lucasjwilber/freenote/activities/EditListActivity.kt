package com.lucasjwilber.freenote.activities

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
import com.lucasjwilber.freenote.DeletedSegment
import com.lucasjwilber.freenote.ListSegmentsAdapter
import com.lucasjwilber.freenote.R
import com.lucasjwilber.freenote.SEGMENT_DELIMITER
import com.lucasjwilber.freenote.databinding.ActivityEditListBinding
import com.lucasjwilber.freenote.viewmodels.EditListViewModel
import java.util.*

class EditListActivity : BaseActivity() {
    private lateinit var binding: ActivityEditListBinding
    private lateinit var viewModel: EditListViewModel
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var deletedSegmentsObserver: Observer<in Stack<DeletedSegment>>
    private var undoButton: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditListBinding.inflate(layoutInflater)
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

            // todo: is this actually better than just leaving the title as an ET?
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()

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
        if (saveOnStop) {
//            val title: String = binding.noteTitleEditText.text.toString()
            val body: String = viewModel.segments.value!!.joinToString(SEGMENT_DELIMITER)

//            viewModel.note.title = title
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
        binding.noteTitleEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.note.title = s.toString()
            }
        })
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_note_menu, menu)

        deleteButton = menu?.getItem(1)

        // the same menu is used for notes and lists. notes don't have an undo feature, so hide it.
//        menu?.findItem(R.id.action_undo)?.isVisible = false
        undoButton = menu?.findItem(R.id.action_undo)

        //hide the delete option on notes that haven't been saved yet
        if (viewModel.noteLiveData?.value == null) {
            deleteButton?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_save) {
//            saveNote()
//        } else
        if (item.itemId == R.id.action_delete) {
            binding.deleteModalLayout.visibility = View.VISIBLE
        } else if (item.itemId == R.id.action_undo) {
            undoSegmentDelete()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun undoSegmentDelete() {
        val delseg: DeletedSegment = viewModel.deletedSegments.value!!.pop()
        viewModel.deletedSegments.value = viewModel.deletedSegments.value

        val segs = viewModel.segments.value as ArrayList<String>
        segs.add(delseg.position, delseg.text)
        viewModel.segments.value = segs
        viewAdapter.notifyItemInserted(delseg.position)


//        if (currentNote.currentlyEditedSegmentPosition != null) {
//            if (currentNote.currentlyEditedSegmentPosition!! > delseg.position) {
//                currentNote.currentlyEditedSegmentPosition =
//                    currentNote.currentlyEditedSegmentPosition!! + 1
//            } else {
//                currentNote.currentlyEditedSegmentPosition =
//                    currentNote.currentlyEditedSegmentPosition!! - 1
//            }
//        }
    }
}
