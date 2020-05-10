package com.lucasjwilber.freenote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.databinding.ActivityEditNoteBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var newNote = true
    private var noteId: Int? = null
    private var segments = ArrayList<String>()
    private var originalSegments = ArrayList<String>()
    private val segmentDelimiter = "|{]"
    class DeletedSegment(val position: Int, val text: String)
    private var deletedSegments: Stack<DeletedSegment> = Stack()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Create Note"
        binding.toolbar.inflateMenu(R.menu.action_bar_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle: Bundle ?= intent.extras
        val message = bundle?.getInt("noteId")


        testString += "oncreate"



        val context = this
        viewManager = LinearLayoutManager(context)

        if (message != null) { // if we got here from clicking on an existing note
            newNote = false

            supportActionBar?.title = "Edit Note"


            GlobalScope.launch(Dispatchers.Main) {
                val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()

                val note: Note = async(Dispatchers.IO) {
                    return@async notesDao.getNoteById(message)
                }.await()

                Log.i("ljw", note.segments.toString())

                binding.noteTitleTV.setText(note.title)
                binding.noteTitleEditText.setText(note.title)

                if (note.segments!!.isNotEmpty()) {
                    // if there was only one segment the delimiter won't be there
                    if (!note.segments!!.contains(segmentDelimiter)) {
                        segments.add(note.segments.toString())
                    } else {
                        segments = note.segments?.split(segmentDelimiter) as ArrayList<String>
                    }
                    originalSegments = segments
                }

                viewAdapter = EditNoteAdapter(segments, newNote, deletedSegments)
                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }

                noteId = note.id
            }
        } else { //if we got here from clicking the 'new note' button
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()

            viewAdapter = EditNoteAdapter(segments, newNote, deletedSegments)
            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }

    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            saveNote()
        } else if (item.itemId == R.id.action_undo) {
            if (deletedSegments.isEmpty()) return false

            undoSegmentDelete()

            if (deletedSegments.isEmpty()) {
                //todo: hide button or discolor it
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun undoSegmentDelete() {
        val delseg: DeletedSegment = deletedSegments.pop()
        segments.add(delseg.position, delseg.text)
        viewAdapter.notifyItemInserted(delseg.position)
    }


    private fun saveNote() {
        val title: String = binding.noteTitleEditText.text.toString()

        if (title.isEmpty()) {
            showToast(this, "Please add a title")
            return
        }


        Log.i("ljw", testString)


        //todo: get the new-segment-edittext text and append to segments

        val db = AppDatabase.getDatabase(this, CoroutineScope(Dispatchers.IO))
        val serializedSegments = segments.joinToString(segmentDelimiter)
        val note = Note(noteId, title, serializedSegments)

        GlobalScope.launch(Dispatchers.IO) {
            if (newNote)
                db.noteDao().insert(note)
            else
                db.noteDao().update(note)
        }
        showToast(this, "Saved")
    }

    public fun changeTitle(view: View) {
        binding.noteTitleEditText.setText(binding.noteTitleTV.text)
        binding.noteTitleTV.visibility = View.GONE
        binding.noteTitleEditText.visibility = View.VISIBLE
    }
}