package com.lucasjwilber.freenote

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.databinding.ActivityEditNoteBinding
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class EditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.action_bar_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.deleteModalLayout.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.cancelDeleteButton.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.confirmDeleteButton.setOnClickListener { deleteNote() }

        supportActionBar?.title = getString(R.string.create_) +
                if (currentNote.type == NOTE) getString(R.string.note) else getString(R.string.list)

        val context = this
        viewManager = LinearLayoutManager(context)

        if (!currentNote.isNew) { // if we got here from clicking on an existing note

            supportActionBar?.title = if (currentNote.type == NOTE) getString(R.string.edit_note) else getString(R.string.edit_list)

            GlobalScope.launch(Dispatchers.Main) {
                val notesDao = AppDatabase.getDatabase(application).noteDao()

                val note: Note = async(Dispatchers.IO) {
                    return@async notesDao.getNoteById(currentNote.id!!)
                }.await()

                binding.noteTitleTV.text = if (note.title.isEmpty()) getString(R.string.untitled) else note.title
                binding.noteTitleTV.setOnClickListener { changeTitle() }
                binding.noteTitleEditText.setText(note.title)

                if (currentNote.type == LIST) {
                    if (note.segments!!.isNotEmpty()) {
                        // if there was only one segment the delimiter won't be there
                        if (!note.segments!!.contains(SEGMENT_DELIMITER)) {
                            currentNote.segments.add(note.segments.toString())
                        } else {
                            currentNote.segments = note.segments?.split(SEGMENT_DELIMITER) as ArrayList<String>
                        }
                    }
                } else {
                    currentNote.body = note.segments.toString()
                }

                viewAdapter = EditNoteAdapter(context)
                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }

            }
        } else { //if we got here from clicking the 'new note' button:
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()

            viewAdapter = EditNoteAdapter(context)
            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentNote.newSegmentText = ""
    }

    override fun onStop() {
        super.onStop()
        saveNote()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
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
        val title: String =
            if (binding.noteTitleEditText.text.toString().isEmpty())
                getString(R.string.untitled)
            else
                binding.noteTitleEditText.text.toString()

        if (title != binding.noteTitleTV.text.toString() ||
            currentNote.deletedSegments.size > 0 ||
            currentNote.newSegmentText.isNotEmpty()
        ) {
            currentNote.hasBeenChanged = true
        }

        if (!currentNote.hasBeenChanged) {
            Log.i("ljw", "no changes to save")
            return
        }

        if (currentNote.newSegmentText.isNotEmpty()) {
            currentNote.segments.add(currentNote.newSegmentText)
            currentNote.newSegmentText = ""
            if (newSegmentEditText != null) newSegmentEditText!!.text.clear()
        }

        val db = AppDatabase.getDatabase(this)
        val serializedSegments = currentNote.segments.joinToString(SEGMENT_DELIMITER)
        val text = if (currentNote.type == NOTE) currentNote.body else serializedSegments
        val note = Note(currentNote.id, currentNote.type, title, text)

        GlobalScope.launch(Dispatchers.IO) {
           if (currentNote.isNew) {
               db.noteDao().insert(note)
               currentNote.isNew = false
           } else {
               db.noteDao().update(note)
           }
        }

        showToast(this, getString(R.string.saved))
    }

    private fun deleteNote() {
        GlobalScope.launch {
            val notesDao = AppDatabase.getDatabase(application).noteDao()
            notesDao.deleteNoteById(currentNote.id!!)
        }
        showToast(this, getString(R.string.note_deleted))
        finish()
    }

    private fun changeTitle() {
        binding.noteTitleEditText.setText(binding.noteTitleTV.text)
        binding.noteTitleTV.visibility = View.INVISIBLE
        binding.noteTitleEditText.visibility = View.VISIBLE
        binding.noteTitleEditText.requestFocus()
    }
}
