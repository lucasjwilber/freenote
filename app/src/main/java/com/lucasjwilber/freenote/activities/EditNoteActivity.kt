package com.lucasjwilber.freenote.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.database.NoteDatabase
import com.lucasjwilber.freenote.databinding.ActivityEditNoteBinding
import com.lucasjwilber.freenote.EditNoteAdapter
import com.lucasjwilber.freenote.models.Note
import com.lucasjwilber.freenote.models.NoteDescriptor
import com.lucasjwilber.freenote.viewmodels.AllNotesViewModel
import com.lucasjwilber.freenote.viewmodels.EditNoteViewModel
import kotlinx.android.synthetic.main.activity_edit_note.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class EditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var segmentsOnOpen = ""
    private var viewModel: EditNoteViewModel? = null
    private lateinit var observer: Observer<in Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.edit_note_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        viewModel = ViewModelProviders.of(this).get(EditNoteViewModel::class.java)

        binding.deleteModalLayout.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.cancelDeleteButton.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.confirmDeleteButton.setOnClickListener { deleteNote() }

        supportActionBar?.title =
            if (currentNote.type == NOTE) getString(
                R.string.create_note
            ) else getString(R.string.create_list)

        val context = this
        viewManager = LinearLayoutManager(context)

        if (!currentNote.isNew) { // if we got here from clicking on an existing note

            supportActionBar?.title = if (currentNote.type == NOTE) getString(
                R.string.edit_note
            ) else getString(R.string.edit_list)
            currentNote.titleWasSet = true

            viewModel?.setNote(currentNote.id!!)

            observer = Observer { note ->

                val titleText = if (note.title.isEmpty()) getString(R.string.untitled) else note.title
                binding.noteTitleTV.text = titleText
                binding.noteTitleTV.setOnClickListener { changeTitle() }
                binding.noteTitleEditText.setText(titleText)

                segmentsOnOpen = note.segments

                if (currentNote.type == LIST) {
                    if (note.segments.isNotEmpty()) {
                        // if there was only one segment the delimiter won't be there
                        if (!note.segments.contains(SEGMENT_DELIMITER)) {
                            currentNote.segments.add(note.segments)
                        } else {
                            currentNote.segments = note.segments.split(
                                SEGMENT_DELIMITER
                            ) as ArrayList<String>
                        }
                    }
                } else {
                    currentNote.body = note.segments
                }

                viewAdapter =
                    EditNoteAdapter(context)
                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }

            viewModel?.note?.observe(this, observer)


        } else { //if we got here from clicking the 'new note' button:
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()

            viewAdapter =
                EditNoteAdapter(context)
            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {
        super.onStop()
        saveNote()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_note_menu, menu)
        undoButton = menu?.findItem(R.id.action_undo)
        if (currentNote.isNew) {
            //hide the delete option
            menu?.getItem(1)?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_save) {
//            saveNote()
//        } else
        if (item.itemId == R.id.action_undo) {
            if (currentNote.deletedSegments.isNotEmpty()) undoSegmentDelete()
        } else if (item.itemId == R.id.action_delete) {
            binding.deleteModalLayout.visibility = View.VISIBLE
        }
        return super.onOptionsItemSelected(item)
    }

    private fun undoSegmentDelete() {
        val delseg: DeletedSegment = currentNote.deletedSegments.pop()
        currentNote.segments.add(delseg.position, delseg.text)
        viewAdapter.notifyItemInserted(delseg.position)

        if (currentNote.deletedSegments.isEmpty()) {
            undoButton?.isVisible = false
        }

        if (currentNote.currentlyEditedSegmentPosition != null) {
            if (currentNote.currentlyEditedSegmentPosition!! > delseg.position) {
                currentNote.currentlyEditedSegmentPosition =
                    currentNote.currentlyEditedSegmentPosition!! + 1
            } else {
                currentNote.currentlyEditedSegmentPosition =
                    currentNote.currentlyEditedSegmentPosition!! - 1
            }
        }
    }


    private fun saveNote() {
        var title: String = binding.noteTitleEditText.text.toString()

        if (title != binding.noteTitleTV.text.toString() ||
                currentNote.deletedSegments.size > 0 ||
                currentNote.newSegmentText.isNotEmpty() ||
                (currentNote.type == NOTE && currentNote.body != segmentsOnOpen) ||
                (currentNote.type == LIST && currentNote.segments.joinToString(
                    SEGMENT_DELIMITER
                ) != segmentsOnOpen)
        ) {
            currentNote.hasBeenChanged = true
        }

        if (!currentNote.hasBeenChanged) {
            Log.i("ljw", "no changes to save")
            return
        } else if (currentNote.title.isEmpty() &&
            noteTitleEditText.text.isEmpty() &&
            currentNote.body.isEmpty() &&
            currentNote.segments.size == 0 &&
            currentNote.newSegmentText.isEmpty()) {
            Log.i("ljw","no use saving an empty note")
            return
        }

        if (currentNote.newSegmentText.isNotEmpty()) {
            currentNote.segments.add(currentNote.newSegmentText)
            currentNote.newSegmentText = ""
            if (newSegmentEditText != null) newSegmentEditText!!.text.clear()
        }

        val db =
            NoteDatabase.getDatabase(this)
        if (title.isEmpty()) title = "Untitled"
        val text = if (currentNote.type == NOTE) currentNote.body else currentNote.segments.joinToString(
            SEGMENT_DELIMITER
        )
        val note = Note(
            currentNote.id,
            currentNote.type,
            title,
            text,
            Date().time
        )

        GlobalScope.launch(Dispatchers.IO) {
           if (currentNote.isNew) {
               currentNote.id = db.noteDao().insert(note)
               currentNote.isNew = false
           } else {
               db.noteDao().update(note)
           }
        }

//        showToast(this, getString(R.string.saved))
    }

    private fun deleteNote() {
        GlobalScope.launch {
            val notesDao = NoteDatabase.getDatabase(
                application
            ).noteDao()
            notesDao.deleteNoteById(currentNote.id!!)
        }
        showToast(
            this,
            getString(R.string.note_deleted)
        )
        finish()
    }

    private fun changeTitle() {
        binding.noteTitleTV.visibility = View.INVISIBLE
        binding.noteTitleEditText.visibility = View.VISIBLE
        binding.noteTitleEditText.requestFocus()
        binding.noteTitleEditText.setSelection(binding.noteTitleEditText.text.length)
    }
}
