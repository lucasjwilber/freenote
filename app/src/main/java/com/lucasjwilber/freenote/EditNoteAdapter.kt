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
    private class CurrentEditedSegment(var editText: EditText, var textView: TextView, var button: Button, var position: Int, var isStruckThrough: Boolean)
    private var currentEditedSegment: CurrentEditedSegment? = null

    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)

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

            textView.setOnClickListener {updateSegment(holder, textView) }
            textView.setOnLongClickListener { strikeThroughSegment(holder, textView) }

        } else if (getItemViewType(position) == NEW_SEGMENT) {
            val editText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)

            val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
            saveButton.setOnClickListener { createNewSegment(editText.text.toString()) }

            newSegmentEditText = editText

            if (currentNote.titleWasSet) {
                editText.requestFocus()
            }

            editText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    currentNote.titleWasSet = true
                    hideLastEditedSegment()
                }
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

        currentNote.newSegmentText = ""

    }

    private fun deleteSegment(position: Int) {
        currentNote.deletedSegments.push(DeletedSegment(position, currentNote.segments[position]))
        undoButton?.isVisible = true

        currentNote.segments.removeAt(position)

        // update currentlyEditedSegmentPosition accordingly.
        // can't use simple ++ or -- operators for some reason
        if (currentNote.currentlyEditedSegmentPosition != null) {
            if (currentNote.currentlyEditedSegmentPosition!! > position) {
                currentNote.currentlyEditedSegmentPosition =
                    currentNote.currentlyEditedSegmentPosition!! - 1
            } else {
                currentNote.currentlyEditedSegmentPosition =
                    currentNote.currentlyEditedSegmentPosition!! + 1
            }
        }
        notifyItemRemoved(position)
    }

    private fun hideLastEditedSegment() {
        if (currentEditedSegment != null) {
            val ces: CurrentEditedSegment = currentEditedSegment!!

            ces.textView.visibility = View.VISIBLE

            val updatedText: String = ces.editText.text.toString()
            ces.textView.text = updatedText
            if (currentNote.segments[ces.position].contains(STRIKE_THROUGH_INDICATOR)) {
                currentNote.segments[ces.position] = STRIKE_THROUGH_INDICATOR + updatedText
            } else {
                currentNote.segments[ces.position] = updatedText
            }

            ces.editText.visibility = View.GONE
            ces.button.visibility = View.GONE
            ces.editText.removeTextChangedListener(makeTextWatcher(TW_UPDATED_SEGMENT))
        }
    }

    private fun updateSegment(holder: MyViewHolder, textView: TextView) {
        //if the last edited segment wasn't 'saved', save the changes and update the textview
        hideLastEditedSegment()

        val editText: EditText = holder.constraintLayout.findViewById(R.id.segmentEditText)
        val updateSegmentButton: Button = holder.constraintLayout.findViewById(R.id.updateSegmentButton)

        currentEditedSegment = CurrentEditedSegment(
            editText,
            textView,
            updateSegmentButton,
            holder.adapterPosition,
            currentNote.segments[holder.adapterPosition].contains(STRIKE_THROUGH_INDICATOR))

        val ces = currentEditedSegment
        currentNote.currentlyEditedSegmentPosition = holder.adapterPosition
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
            //reapply the strike-through indicator
            if (ces.isStruckThrough) {
                currentNote.segments[ces.position] = STRIKE_THROUGH_INDICATOR + ces.editText.text.toString()
            } else {
                currentNote.segments[ces.position] = ces.editText.text.toString()
            }
            ces.textView.text = ces.editText.text.toString()
        }

        ces.textView.visibility = View.VISIBLE
        ces.editText.visibility = View.GONE
        ces.button.visibility = View.GONE

        currentEditedSegment = null
        currentNote.currentlyEditedSegmentPosition = null
    }

    private fun strikeThroughSegment(holder: MyViewHolder, textView: TextView): Boolean {
        if (currentNote.segments[holder.adapterPosition].contains(STRIKE_THROUGH_INDICATOR)) {
            currentNote.segments[holder.adapterPosition] = currentNote.segments[holder.adapterPosition].substring(3)
            textView.paintFlags = 0
        } else {
            currentNote.segments[holder.adapterPosition] = STRIKE_THROUGH_INDICATOR + currentNote.segments[holder.adapterPosition]
            textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        notifyItemChanged(holder.adapterPosition)

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
                    //make the new segment EditText and any segments that are being edited un-swipable
                    0
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteSegment(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}