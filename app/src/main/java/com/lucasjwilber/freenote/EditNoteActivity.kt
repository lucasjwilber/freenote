package com.lucasjwilber.freenote

import android.content.Context
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
import java.util.*
import kotlin.collections.ArrayList

class EditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var newNote = true
    private var noteId: Int? = null
    private val context: Context = this
    private var noteType: Int = NOTE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Create Note"
        binding.toolbar.inflateMenu(R.menu.action_bar_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.deleteModalLayout.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.cancelDeleteButton.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.confirmDeleteButton.setOnClickListener { deleteNote() }

        currentNoteSegments = ArrayList<String>()
        currentNoteDeletedSegments = Stack()
        currentNoteHasBeenChanged = false

        val bundle: Bundle ?= intent.extras
        noteId = bundle?.getInt("noteId")
        newNote = bundle?.getBoolean("isNew") == true
        if (bundle?.getInt("type") == LIST) noteType = LIST

        val context = this
        viewManager = LinearLayoutManager(context)

//        if (noteId != null) { // if we got here from clicking on an existing note
        if (!newNote) { // if we got here from clicking on an existing note

            supportActionBar?.title = "Edit Note"


            GlobalScope.launch(Dispatchers.Main) {
                val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()

                val note: Note = async(Dispatchers.IO) {
                    return@async notesDao.getNoteById(noteId!!)
                }.await()

                binding.noteTitleTV.setText(note.title)
                binding.noteTitleTV.setOnClickListener { changeTitle() }
                binding.noteTitleEditText.setText(note.title)

                if (noteType == LIST) {
                    if (note.segments!!.isNotEmpty()) {
                        // if there was only one segment the delimiter won't be there
                        if (!note.segments!!.contains(segmentDelimiter)) {
                            currentNoteSegments.add(note.segments.toString())
                        } else {
                            currentNoteSegments = note.segments?.split(segmentDelimiter) as ArrayList<String>

                            //find elements that should be strike-through'd
//                            for (segment: IndexedValue<String> in currentNoteSegments.withIndex()) {
//                                if (segment.toString().contains(strikeThroughIndicator)) {
//                                    currentNoteStrikeThroughIndicies.add(segment.index)
//                                    currentNoteSegments[segment.index] = currentNoteSegments[segment.index].substring(3)
//                                }
//                            }
//                            Log.i("ljw", currentNoteStrikeThroughIndicies.toString())
                        }

                        //strike through the lines that start with the indicator

                    }
                } else {
                    currentNoteBody = note.segments.toString()
                }

                viewAdapter = EditNoteAdapter(newNote, context, noteType)
                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }

            }
        } else { //if we got here from clicking the 'new note' button
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()

            viewAdapter = EditNoteAdapter(newNote, context, noteType)
            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }

    }

    override fun onStop() {
        super.onStop()
        saveNote()
        currentNewSegmentText = ""
        currentNoteBody = ""
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        if (newNote) {
            menu?.getItem(1)?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_save) {
//            saveNote()
//        } else
        if (item.itemId == R.id.action_undo) {
            if (currentNoteDeletedSegments.isNotEmpty()) undoSegmentDelete()
        } else if (item.itemId == R.id.action_delete) {
            binding.deleteModalLayout.visibility = View.VISIBLE
        }
        return super.onOptionsItemSelected(item)
    }

    private fun undoSegmentDelete() {
        val delseg: DeletedSegment = currentNoteDeletedSegments.pop()
        currentNoteSegments.add(delseg.position, delseg.text)
        viewAdapter.notifyItemInserted(delseg.position)
    }


    private fun saveNote() {
        val title: String =
            if (binding.noteTitleEditText.text.toString().isEmpty())
                "Untitled"
            else
                binding.noteTitleEditText.text.toString()

        if (title != binding.noteTitleTV.text.toString() ||
            currentNoteDeletedSegments.size > 0 ||
            currentNewSegmentText.isNotEmpty()
        ) {
            currentNoteHasBeenChanged = true
        }

        if (!currentNoteHasBeenChanged) {
            Log.i("ljw", "no changes to save")
            return
        }

        if (currentNewSegmentText.isNotEmpty()) {
            currentNoteSegments.add(currentNewSegmentText)
        }

        //for each index in strikethroughindicies prepend the strikethrough indicator
//        for (segment in currentNoteSegments.withIndex()) {
//            if (currentNoteStrikeThroughIndicies.contains(segment.index)) {
//                currentNoteSegments[segment.index] = strikeThroughIndicator + currentNoteSegments[segment.index]
//            }
//        }

        val db = AppDatabase.getDatabase(this, CoroutineScope(Dispatchers.IO))
        val serializedSegments = currentNoteSegments.joinToString(segmentDelimiter)
        val id = if (newNote) null else noteId
        val text = if (noteType == NOTE) currentNoteBody else serializedSegments
        val note = Note(id, noteType, title, text)

        GlobalScope.launch(Dispatchers.IO) {
           if (newNote) {
               db.noteDao().insert(note)
               newNote = false
           } else {
               db.noteDao().update(note)
           }
        }

        showToast(this, "Saved")
    }

    private fun deleteNote() {
        GlobalScope.launch {
            val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()
            notesDao.deleteNoteById(noteId!!)
        }
        finish()
    }

    fun changeTitle() {
        binding.noteTitleEditText.setText(binding.noteTitleTV.text)
        binding.noteTitleTV.visibility = View.INVISIBLE
        binding.noteTitleEditText.visibility = View.VISIBLE
        binding.noteTitleEditText.requestFocus()
    }
}
