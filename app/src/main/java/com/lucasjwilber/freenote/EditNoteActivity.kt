package com.lucasjwilber.freenote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
    class DeletedSegment(val position: Int, val text: String)
    private var deletedSegments: Stack<DeletedSegment> = Stack()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        val pageTitle = "Create Note"
        supportActionBar?.title = pageTitle
        binding.toolbar.inflateMenu(R.menu.action_bar_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.noteTitleTextView.setOnClickListener { editNoteTitle() }

        val bundle: Bundle ?= intent.extras
        val message = bundle?.getInt("noteId")

        val context = this
        viewManager = LinearLayoutManager(context)

        if (message != null) { // if we got here from clicking on an existing note
            newNote = false

            val pageTitle = "Edit Note"
            supportActionBar?.title = pageTitle


            GlobalScope.launch(Dispatchers.Main) {
                val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()

                val note: Note = async(Dispatchers.IO) {
                    return@async notesDao.getNoteById(message)
                }.await()

                Log.i("ljw", "segments is ${note.segments.toString()}")
                if (note.segments!!.length == 1) {
                    segments.add(note.segments.toString())
                } else if (note.segments!!.length > 1) {
                    segments = note.segments?.split("|{]") as ArrayList<String>
                }

                viewAdapter = NoteAdapter(segments, newNote, deletedSegments)
                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }

                binding.noteTitleTextView.setText(note.title)
                noteId = note.id
            }
        } else { //if we got here from clicking the 'new note' button
            binding.noteTitleEditText.requestFocus()

            viewAdapter = NoteAdapter(segments, newNote, deletedSegments)
            binding.noteSegmentsRV.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }

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
        Log.i("ljw", delseg.text)
        segments.add(delseg.position, delseg.text)

        viewAdapter = NoteAdapter(segments, newNote, deletedSegments)
        binding.noteSegmentsRV.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }


    private fun saveNote() {
        val title: String = binding.noteTitleEditText.text.toString()

        if (title.isEmpty()) {
            //todo: toast
            return
        }

        val db = AppDatabase.getDatabase(this, CoroutineScope(Dispatchers.IO))
        val serializedSegments = segments.joinToString("|{]")
        val note = Note(noteId, title, serializedSegments)

        GlobalScope.launch(Dispatchers.IO) {
            if (newNote)
                db.noteDao().insert(note)
            else
                db.noteDao().update(note)
        }

        finish()
    }

    private fun editNoteTitle() {
        binding.noteTitleTextView.visibility = View.GONE
        binding.noteTitleEditText.setText(binding.noteTitleTextView.text.toString())
        binding.noteTitleEditText.visibility = View.VISIBLE
    }



    class NoteAdapter(private val segments: ArrayList<String>, var newNote: Boolean, var deletedSegments: Stack<DeletedSegment>) :
        RecyclerView.Adapter<NoteAdapter.MyViewHolder>() {

        private val SEGMENT: Int = 0
        private val NEW_SEGMENT: Int = 1
        private lateinit var newSegmentEditText: EditText
//        private class DeletedSegment(position: Int, text: String)
//        private var deletedSegments: Stack<DeletedSegment> = deletedSegments

        class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout) { }

        override fun getItemViewType(position: Int): Int {
            return if (position == segments.size)
                NEW_SEGMENT
            else
                SEGMENT
        }

        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): MyViewHolder {

            if (viewType == SEGMENT) {
                val constraintLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.segment, parent, false) as ConstraintLayout

                return MyViewHolder(constraintLayout)
            }
            else { //(viewType == NEW_SEGMENT)
                val constraintLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.new_segment, parent, false) as ConstraintLayout
                // set the view's size, margins, paddings and layout parameters
                newSegmentEditText = constraintLayout.findViewById(R.id.newSegmentEditText)
                return MyViewHolder(constraintLayout)
            }
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (getItemViewType(position) == SEGMENT) {
                val textView: TextView = holder.constraintLayout.findViewById(R.id.segmentTextView)
                textView.text = segments[position]
                val deleteButton: Button = holder.constraintLayout.findViewById(R.id.segmentDeleteButton)
                deleteButton.setOnClickListener { deleteSegment(position)}

            } else { //(getItemViewType(position) == NEW_SEGMENT)
                val editText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)
                val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
                saveButton.setOnClickListener { onNewSegmentSaveButtonClick(editText.text.toString()) }
                if (!newNote) {
                    editText.requestFocus()
                }
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = segments.size + 1

        private fun onNewSegmentSaveButtonClick(text: String) {
            if (text.isEmpty()) return

            segments.add(text)
            newSegmentEditText.text.clear()
            this.notifyItemInserted(segments.size)

            // flip newNote so the cursor focus will go to the new segment EditText
            newNote = false
        }

        private fun deleteSegment(position: Int) {
            Log.i("ljw", "segments is $segments, position is $position")

            deletedSegments.push(DeletedSegment(position, segments[position]))
            segments.removeAt(position)

            //todo: un-hide or re-color the undo button


            // notifyItemRemoved() is unreliable here because position here was set when the view was bound, so
            // if new views/segments were created after this was bound then the position will be out of date
            // which could lead to an outOfBounds exception. so notifyDataSetChanged() is used.
            this.notifyDataSetChanged()
        }
    }
}
