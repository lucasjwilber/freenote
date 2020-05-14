package com.lucasjwilber.freenote

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class EditNoteAdapter(val context: Context) :
    RecyclerView.Adapter<EditNoteAdapter.MyViewHolder>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        setUpSwipeListener(recyclerView)
    }

    private val SEGMENT: Int = 0
    private val NEW_SEGMENT: Int = 1
    private val NOTE_BODY: Int = 2
//    private lateinit var newSegmentEditText: EditText
    private class CurrentEditedSegment(var editText: EditText, var textView: TextView, var button: Button, var position: Int, var isStruckThrough: Boolean)
    private var currentEditedSegment: CurrentEditedSegment? = null

    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout) {}

    override fun getItemViewType(position: Int): Int {
        return if (currentNote.type == LIST) {
            if (position == currentNote.segments.size)
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
            newSegmentEditText!!.addTextChangedListener(makeTextWatcher(TW_NEW_SEGMENT))

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

            if (currentNote.segments[position].contains(STRIKE_THROUGH_INDICATOR)) {
                textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                textView.text = currentNote.segments[position].substring(3)
            } else {
                textView.text = currentNote.segments[position]
                textView.paintFlags = 0
            }

            val editText: EditText = holder.constraintLayout.findViewById(R.id.segmentEditText)
            val updateSegmentButton: Button = holder.constraintLayout.findViewById(R.id.updateSegmentButton)

            textView.setOnClickListener {updateSegment(textView, editText, position, updateSegmentButton) }
            textView.setOnLongClickListener { strikeThroughSegment(position, currentNote.segments[position], textView) }

        } else if (getItemViewType(position) == NEW_SEGMENT) {
            val editText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)

            val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
            saveButton.setOnClickListener { createNewSegment(editText.text.toString()) }

            newSegmentEditText = editText
            editText.requestFocus()
            currentNote.currentlyEditedSegmentPosition = position
            editText.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) hideLastEditedSegment()
            }
        } else { //if (getItemViewType(position) == NOTE_BODY) {
            val editText: EditText = holder.constraintLayout.findViewById(R.id.noteBodyEditText)
            editText.setText(currentNote.body)
            editText.addTextChangedListener(makeTextWatcher(TW_NOTE_BODY))
        }
    }

    override fun getItemCount() = currentNote.segments.size + 1

    private fun createNewSegment(text: String) {
        if (text.isEmpty()) return

        currentNote.segments.add(text)
        newSegmentEditText!!.text.clear()

        // don't use notifyItemInserted() here, in order to keep the keyboard open and the edit text focused
        notifyDataSetChanged()

        currentNote.hasBeenChanged = true
        currentNote.newSegmentText = ""

    }

    private fun deleteSegment(position: Int) {
        currentNote.deletedSegments.push(DeletedSegment(position, currentNote.segments[position]))
        currentNote.segments.removeAt(position)
        undoButton?.isVisible = true
        notifyDataSetChanged()
    }

    private fun hideLastEditedSegment() {
        if (currentEditedSegment != null) {
            val ces: CurrentEditedSegment = currentEditedSegment!!

            ces.textView.visibility = View.VISIBLE
            val updatedText: String = ces.editText.text.toString()
            if (updatedText != ces.textView.text.toString()) {
                ces.textView.text = updatedText
                currentNote.segments[ces.position] = updatedText
            }
            ces.editText.visibility = View.GONE
            ces.button.visibility = View.GONE
            ces.editText.removeTextChangedListener(makeTextWatcher(TW_UPDATED_SEGMENT))
        }
    }

    private fun updateSegment(textView: TextView, editText: EditText, position: Int, updateSegmentButton: Button) {
        //if the last edited segment wasn't 'saved', save the changes and update the textview
        hideLastEditedSegment()

        currentEditedSegment = CurrentEditedSegment(
            editText,
            textView,
            updateSegmentButton,
            position,
            currentNote.segments[position].contains(STRIKE_THROUGH_INDICATOR))

        val ces = currentEditedSegment
        currentNote.currentlyEditedSegmentPosition = position
        ces!!.editText.addTextChangedListener(makeTextWatcher(TW_UPDATED_SEGMENT))
        editText.setText(textView.text.toString())
        editText.visibility = View.VISIBLE
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        updateSegmentButton.visibility = View.VISIBLE
        textView.visibility = View.GONE

        updateSegmentButton.setOnClickListener {
            saveSegmentEdits()
        }
    }

    private fun saveSegmentEdits() {
        val ces: CurrentEditedSegment = currentEditedSegment!!
        if (ces.textView.text.toString() != ces.editText.text.toString()) {
            currentNote.hasBeenChanged = true
            currentNote.segments[ces.position] = ces.editText.text.toString()
            ces.textView.text = ces.editText.text.toString()
        }

        //reapply the strike-through indicator
        if (ces.isStruckThrough) {
            currentNote.segments[ces.position] = STRIKE_THROUGH_INDICATOR + ces.editText.text.toString()
        }

        ces.textView.visibility = View.VISIBLE
        ces.editText.visibility = View.GONE
        ces.button.visibility = View.GONE

        currentEditedSegment = null
        currentNote.currentlyEditedSegmentPosition = null
    }

    private fun strikeThroughSegment(position: Int, text: String, textView: TextView): Boolean {
        if (text.contains(STRIKE_THROUGH_INDICATOR)) {
            currentNote.segments[position] = currentNote.segments[position].substring(3)
            textView.paintFlags = 0
        } else {
            currentNote.segments[position] = STRIKE_THROUGH_INDICATOR + currentNote.segments[position]
            textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        notifyItemChanged(position)
        currentNote.hasBeenChanged = true

        return true
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
                return if (position == itemCount - 1 || position == currentNote.currentlyEditedSegmentPosition) {
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
}