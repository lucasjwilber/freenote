package com.lucasjwilber.freenote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.databinding.ActivityCreateNoteBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNoteBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var isNew = true
    private var noteId: Int? = null

    private var segments = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val context = this

        setSupportActionBar(binding.toolbar)
        val pageTitle = "Create Note"
        supportActionBar?.setTitle(pageTitle)
        binding.toolbar.inflateMenu(R.menu.action_bar_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        binding.saveNoteButton.setOnClickListener { saveNote() }

        val bundle: Bundle ?= intent.extras
        val message = bundle?.getInt("noteId")
        if (message != null) {
            isNew = false
            GlobalScope.launch(Dispatchers.Main) {

                val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()

                val note: Note = async(Dispatchers.IO) {
                    return@async notesDao.getNoteById(message)
                }.await()

                if (note.segments!!.isNotEmpty()) {
                    val arrayOfSegments = note.segments?.split("|{]")
                    segments = arrayOfSegments as ArrayList<String>
                }

                viewManager = LinearLayoutManager(context)
                viewAdapter = NoteAdapter(segments)
                binding.noteSegmentsRV.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }

                binding.noteTitleEditText.setText(note.title)
                noteId = note.id
            }
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = NoteAdapter(segments)
        binding.noteSegmentsRV.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            saveNote()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun saveNote() {
        val title: String = binding.noteTitleEditText.text.toString()

        if (title.isEmpty()) {
            //todo: toast
            return
        }

        val db = AppDatabase.getDatabase(this, CoroutineScope(Dispatchers.IO))

        var serializedSegments = ""
        for (string: String in segments) {
            serializedSegments += "$string|{]"
        }

        val note = Note(noteId, title, serializedSegments)

        GlobalScope.launch(Dispatchers.IO) {
            if (isNew)
                db.noteDao().insert(note)
            else
                db.noteDao().update(note)
        }
        finish()

    }



    class NoteAdapter(private val segments: ArrayList<String>) :
        RecyclerView.Adapter<NoteAdapter.MyViewHolder>() {

        private val SEGMENT: Int = 0
        private val NEW_SEGMENT: Int = 1
        private lateinit var newSegmentEditText: EditText

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout) {
            //currently each viewholder type is just a constraint layout with no data
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == segments.size)
                NEW_SEGMENT
            else
                SEGMENT
        }

        // Create new views (invoked by the layout manager)
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
                var textView: TextView = holder.constraintLayout.findViewById(R.id.segmentTextView)
                textView.text = segments[position]
                val deleteButton: Button = holder.constraintLayout.findViewById(R.id.segmentDeleteButton)
                deleteButton.setOnClickListener { deleteSegment(position)}

            } else { //(getItemViewType(position) == NEW_SEGMENT)
                var editText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)
                editText.requestFocus()
                var saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
                saveButton.setOnClickListener { onNewSegmentSaveButtonClick(editText.text.toString()) }
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = segments.size + 1

        private fun onNewSegmentSaveButtonClick(text: String) {
            segments.add(text)
            newSegmentEditText.text.clear()
            this.notifyItemInserted(segments.size)
            Log.i("ljw", segments.toString())
        }

        private fun deleteSegment(position: Int) {
            Log.i("ljw", "segments is $segments, position is $position")
            segments.removeAt(position)

            // notifyItemRemoved() is unreliable here because position here was set when the view was bound, so
            // if new views/segments were created after this was bound then the position will be out of date
            // which could lead to an outOfBounds exception. so notifyDataSetChanged() is used.
            this.notifyDataSetChanged()
            
        }
    }
}
