package com.lucasjwilber.freenote

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class EditNoteAdapter(var newNote: Boolean, val context: Context, val noteType: Int) :
    RecyclerView.Adapter<EditNoteAdapter.MyViewHolder>() {


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        setUpSwipeListener(recyclerView)
    }

    private val SEGMENT: Int = 0
    private val NEW_SEGMENT: Int = 1
    private val NOTE_BODY: Int = 2
    private lateinit var newSegmentEditText: EditText
    private var currentlyEditedSegmentPosition: Int? = null
    private class LastEditedSegment(var editText: EditText, var textView: TextView, var button: Button)
    private var lastEditedSegment: LastEditedSegment? = null

    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout) {}

    override fun getItemViewType(position: Int): Int {
        return if (noteType == LIST) {
            if (position == currentNoteSegments.size)
                NEW_SEGMENT
            else
                SEGMENT
        } else {
            NOTE_BODY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {

        if (viewType == SEGMENT) {
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.segment, parent, false) as ConstraintLayout

            return MyViewHolder(constraintLayout)
        }
        else if (viewType == NEW_SEGMENT) {
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.new_segment, parent, false) as ConstraintLayout

            newSegmentEditText = constraintLayout.findViewById(R.id.newSegmentEditText)
            newSegmentEditText.addTextChangedListener(newSegmentEditTextWatcher())

            return MyViewHolder(constraintLayout)
        } else { // if (viewType == NOTE_BODY) {
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.note_body, parent, false) as ConstraintLayout
            return MyViewHolder(constraintLayout)
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (getItemViewType(position) == SEGMENT) {
            val textView: TextView = holder.constraintLayout.findViewById(R.id.segmentTextView)
            textView.text = currentNoteSegments[position]
            val editText: EditText = holder.constraintLayout.findViewById(R.id.segmentEditText)
            val updateSegmentButton: Button = holder.constraintLayout.findViewById(R.id.updateSegmentButton)

            textView.setOnClickListener {updateSegment(textView, editText, position, updateSegmentButton) }

        } else if (getItemViewType(position) == NEW_SEGMENT) {
            val editText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)

            val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
            saveButton.setOnClickListener { onNewSegmentSaveButtonClick(editText.text.toString()) }
            if (!newNote) {
                editText.requestFocus()
            }

            currentlyEditedSegmentPosition = position
            editText.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) hideLastEditedSegment()
            }
        } else { //if (getItemViewType(position) == NOTE_BODY) {
            val editText: EditText = holder.constraintLayout.findViewById(R.id.noteBodyEditText)
            editText.setText(currentNoteBody)
            editText.addTextChangedListener(noteBodyEditTextWatcher())
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = currentNoteSegments.size + 1

    private fun onNewSegmentSaveButtonClick(text: String) {
        if (text.isEmpty()) return

        currentNoteSegments.add(text)
        newSegmentEditText.text.clear()
        this.notifyItemInserted(currentNoteSegments.size)

        // flip newNote so the cursor focus will go to the new segment EditText
        newNote = false
        currentNoteHasBeenChanged = true
        currentNewSegmentText = ""
        newSegmentEditText.requestFocus()

        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun deleteSegment(position: Int) {

        currentNoteDeletedSegments.push(DeletedSegment(position, currentNoteSegments[position]))
        currentNoteSegments.removeAt(position)

        //todo: un-hide or re-color the undo button
    }

    private fun hideLastEditedSegment() {
        Log.i("ljw", "click")
        if (lastEditedSegment != null) {
            lastEditedSegment!!.textView.visibility = View.VISIBLE
            if (lastEditedSegment!!.editText.text.toString() != lastEditedSegment!!.textView.text.toString()) {
                lastEditedSegment!!.textView.text = lastEditedSegment!!.editText.text.toString()
                currentNoteSegments[currentlyEditedSegmentPosition!!] = lastEditedSegment!!.editText.text.toString()
            }
            lastEditedSegment!!.editText.visibility = View.GONE
            lastEditedSegment!!.button.visibility = View.GONE
            lastEditedSegment!!.editText.removeTextChangedListener(updatedSegmentEditTextWatcher())
        }
        currentlyEditedSegmentPosition = null
    }

    private fun updateSegment(textView: TextView, editText: EditText, position: Int, updateSegmentButton: Button) {

        hideLastEditedSegment()

        currentlyEditedSegmentPosition = position
        editText.addTextChangedListener(updatedSegmentEditTextWatcher())

        lastEditedSegment = LastEditedSegment(editText, textView, updateSegmentButton)

        editText.setText(textView.text.toString())
        editText.visibility = View.VISIBLE
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        updateSegmentButton.visibility = View.VISIBLE
        textView.visibility = View.GONE


        updateSegmentButton.setOnClickListener {
            lastEditedSegment = null

            if (textView.text.toString() != editText.text.toString()) {
                currentNoteHasBeenChanged = true
                currentNoteSegments[position] = editText.text.toString()
                textView.text = editText.text.toString()
            }
            textView.visibility = View.VISIBLE
            editText.visibility = View.GONE
            updateSegmentButton.visibility = View.GONE
            currentlyEditedSegmentPosition = null
        }
    }



    private fun setUpSwipeListener(recyclerView: RecyclerView) {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
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
                return if (position == itemCount - 1 || position == currentlyEditedSegmentPosition) {
                    //make the new segment EditText un-swipable
                    0
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteSegment(viewHolder.adapterPosition)
                notifyItemRemoved(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun newSegmentEditTextWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNewSegmentText = s.toString()
            }
            override fun afterTextChanged(s: Editable) { }
        }
    }

    private fun updatedSegmentEditTextWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNoteSegments[currentlyEditedSegmentPosition!!] = s.toString()
                currentNoteHasBeenChanged = true
            }
            override fun afterTextChanged(s: Editable) { }
        }
    }
    private fun noteBodyEditTextWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNoteHasBeenChanged = true
                currentNoteBody = s.toString()
            }
            override fun afterTextChanged(s: Editable) { }
        }
    }
}