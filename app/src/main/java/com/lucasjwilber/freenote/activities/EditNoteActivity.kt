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

open class EditNoteActivity : BaseActivity() {

    private val binding = ActivityEditNoteBinding.inflate(layoutInflater)
    private lateinit var viewModel: EditNoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

            viewModel.getNote(id)

            // update the UI when the note is retrieved from the db
            noteObserver = Observer { note ->
                if (note == null) {
                    Log.i("ljw", "note is null")
                    return@Observer
                }

                viewModel.titleHasBeenSet = true
                viewModel.note = note

                setTitle(note.title)
                binding.noteBodyEditText.setText(note.segments)
            }

            viewModel.noteLiveData?.observe(this, noteObserver)

        } else { //if we got here from clicking the 'new note' button:

            supportActionBar?.title = getString(R.string.create_note)

            // todo: is this actually better than just leaving the title as an ET?
            binding.noteTitleTV.visibility = View.GONE
            binding.noteTitleEditText.visibility = View.VISIBLE
            binding.noteTitleEditText.requestFocus()
        }
    }

    override fun onStop() {
        super.onStop()
        //todo: store title in VM instead of passing it here:
        if (saveOnStop) {
            val title: String = binding.noteTitleEditText.text.toString()
            val body: String = binding.noteBodyEditText.text.toString()

            viewModel.note.title = title
            viewModel.note.segments = body

            viewModel.saveNote()

            deleteButton?.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_note_menu, menu)

        deleteButton = menu?.getItem(1)

        // the same menu is used for notes and lists. notes don't have an undo feature, so hide it.
        menu?.findItem(R.id.action_undo)?.isVisible = false

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

    fun setTitle(title: String) {
        val titleText = if (title.isEmpty()) getString(R.string.untitled) else title
        binding.noteTitleTV.text = titleText
        binding.noteTitleEditText.setText(titleText)
    }
}
