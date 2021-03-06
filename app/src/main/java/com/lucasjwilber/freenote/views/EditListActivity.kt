package com.lucasjwilber.freenote.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.databinding.ActivityEditListBinding
import com.lucasjwilber.freenote.viewmodels.EditListViewModel
import kotlinx.android.synthetic.main.activity_edit_list.*
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
    private lateinit var deleteModal: ConstraintLayout
    private var newSegmentET: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeManager.getTheme())
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
        deleteModal = findViewById(R.id.deleteModalFragment)
        deleteModal.setOnClickListener { deleteModal.visibility = View.GONE }
        deleteModal.findViewById<Button>(R.id.cancelDeleteButton).setOnClickListener { deleteModal.visibility = View.GONE }
        deleteModal.findViewById<Button>(R.id.confirmDeleteButton).setOnClickListener { onDeleteNoteClicked() }

        //attach swipe-to-delete listener
        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(noteSegmentsRV)

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

                viewAdapter = ListSegmentsAdapter(viewModel, newSegmentET)
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

            viewAdapter = ListSegmentsAdapter(viewModel, newSegmentET)
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
        if (newSegmentET != null) newSegmentET!!.setText("")
        deleteButton?.isVisible = true
    }


    override fun onBackPressed() {
        if (deleteModal.visibility == View.VISIBLE) {
            deleteModal.visibility = View.GONE
        } else {
            finish()
        }
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
        undoButton!!.isVisible = viewModel.deletedSegments.value!!.isNotEmpty()

        // hide the delete option on notes that haven't been saved yet
        if (viewModel.noteLiveData?.value == null) {
            deleteButton?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            deleteModal.visibility = View.VISIBLE
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


    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return createSwipeFlags(viewHolder.adapterPosition, viewHolder)
        }

        private fun createSwipeFlags(position: Int, viewHolder: RecyclerView.ViewHolder): Int {
            // the new segment viewholder at the bottom should not be swipeable
            return if (position == viewAdapter.itemCount - 1)
                0
            else
                super.getSwipeDirs(binding.noteSegmentsRV, viewHolder)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            deleteSegment(viewHolder.adapterPosition)
        }
    }

    private fun deleteSegment(position: Int) {
        val delSegs = viewModel.deletedSegments.value!!
        delSegs.push(
            EditListViewModel.DeletedSegment(
                position,
                viewModel.segments[position]
            )
        )
        viewModel.deletedSegments.value = delSegs
        viewModel.segments.removeAt(position)
        viewAdapter.notifyItemRemoved(position)
    }

}
