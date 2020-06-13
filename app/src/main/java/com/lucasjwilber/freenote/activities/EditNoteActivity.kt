package com.lucasjwilber.freenote.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lucasjwilber.freenote.*
import com.lucasjwilber.freenote.databinding.ActivityEditNoteBinding
import com.lucasjwilber.freenote.models.Note
import com.lucasjwilber.freenote.viewmodels.EditNoteViewModel

open class EditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditNoteBinding
    private lateinit var viewModel: EditNoteViewModel
    lateinit var noteObserver: Observer<in Note>
    var id: Long = -1
    var saveOnStop: Boolean = true
    var deleteButton: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProviders.of(this).get(EditNoteViewModel::class.java)

        getIdFromIntent()

        binding.noteTitleTV.setOnClickListener { changeTitle() }
        binding.deleteModalLayout.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.cancelDeleteButton.setOnClickListener { binding.deleteModalLayout.visibility = View.GONE }
        binding.confirmDeleteButton.setOnClickListener { onDeleteNoteClicked() }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.edit_note_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // if the intent included an id, use it to load the note
        if (id >= 0) {
            supportActionBar?.title = getString(R.string.edit_note)

            viewModel.setNote(id)

            // update the UI when the note is retrieved from the db
            noteObserver = Observer { note ->
                if (note == null) {
                    Log.i("ljw", "note is null")
                    return@Observer
                }
                setTitle(note.title)

                val bodyText = note.segments
                binding.noteBodyEditText.setText(bodyText)
            }

            viewModel.note?.observe(this, noteObserver)

        } else { //if we got here from clicking the 'new note' button:

            supportActionBar?.title = getString(R.string.create_note)

            // todo: is this actually better than just leaving the title as an ET?
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()
        }
    }

    override fun onStop() {
        Log.i("ljw", "onstop")
        super.onStop()
        //todo: store title in VM instead of passing it here:
        if (saveOnStop) {
            viewModel.saveNote(
                binding.noteTitleEditText.text.toString(),
                binding.noteBodyEditText.text.toString()
            )
            deleteButton?.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_note_menu, menu)

        deleteButton = menu?.getItem(1)

        // the same menu is used for notes and lists. notes don't have an undo feature, so hide it.
        menu?.findItem(R.id.action_undo)?.isVisible = false

        //hide the delete option on notes that haven't been saved yet
        if (viewModel.note?.value == null) {
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
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeTitle() {
        binding.noteTitleTV.visibility = View.INVISIBLE
        binding.noteTitleEditText.visibility = View.VISIBLE
        binding.noteTitleEditText.requestFocus()
        binding.noteTitleEditText.setSelection(binding.noteTitleEditText.text.length)
        viewModel.titleHasBeenSet = true
    }

    fun onDeleteNoteClicked() {
        Log.i("ljw", "delete note clicked")
        saveOnStop = false
        viewModel.deleteNote()
        finish()
    }

    fun getIdFromIntent() {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            id = extras.getLong("id")
        }
    }

    fun setTitle(title: String) {
        val titleText = if (title.isEmpty()) getString(R.string.untitled) else title
        binding.noteTitleTV.text = titleText
        binding.noteTitleEditText.setText(titleText)
    }

}
