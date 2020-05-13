package com.lucasjwilber.freenote

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var newSegmentEditText: EditText
    private var currentlyEditedSegmentPosition: Int? = null
    private class LastEditedSegment(var editText: EditText, var textView: TextView, var button: Button, var position: Int)
    private var lastEditedSegment: LastEditedSegment? = null

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
            saveButton.setOnClickListener { onNewSegmentSaveButtonClick(editText.text.toString()) }
            if (!currentNote.isNew) {
                editText.requestFocus()
            }

            currentlyEditedSegmentPosition = position
            editText.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) hideLastEditedSegment()
            }
        } else { //if (getItemViewType(position) == NOTE_BODY) {
            val editText: EditText = holder.constraintLayout.findViewById(R.id.noteBodyEditText)
            editText.setText(currentNote.body)
            editText.addTextChangedListener(noteBodyEditTextWatcher())
        }
    }

    override fun getItemCount() = currentNote.segments.size + 1

    private fun onNewSegmentSaveButtonClick(text: String) {
        if (text.isEmpty()) return

        currentNote.segments.add(text)
        newSegmentEditText.text.clear()
        notifyDataSetChanged()

        currentNote.hasBeenChanged = true
        currentNote.newSegmentText = ""
        newSegmentEditText.requestFocus()

//        val imm: InputMethodManager =
//            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    private fun deleteSegment(position: Int) {
        currentNote.deletedSegments.push(DeletedSegment(position, currentNote.segments[position]))
        currentNote.segments.removeAt(position)
        undoButton?.isVisible = true
        notifyDataSetChanged()
    }

    private fun hideLastEditedSegment() {
        if (lastEditedSegment != null) {
            lastEditedSegment!!.textView.visibility = View.VISIBLE
            if (lastEditedSegment!!.editText.text.toString() != lastEditedSegment!!.textView.text.toString()) {
                lastEditedSegment!!.textView.text = lastEditedSegment!!.editText.text.toString()
                if (currentlyEditedSegmentPosition != null) {
                    currentNote.segments[lastEditedSegment!!.position] = lastEditedSegment!!.editText.text.toString()
                }
            }
            lastEditedSegment!!.editText.visibility = View.GONE
            lastEditedSegment!!.button.visibility = View.GONE
            lastEditedSegment!!.editText.removeTextChangedListener(updatedSegmentEditTextWatcher())
        }
        currentlyEditedSegmentPosition = null
    }

    private fun updateSegment(textView: TextView, editText: EditText, position: Int, updateSegmentButton: Button) {
        val isStruckThrough = currentNote.segments[position].contains(STRIKE_THROUGH_INDICATOR)
        hideLastEditedSegment()

        currentlyEditedSegmentPosition = position
        editText.addTextChangedListener(updatedSegmentEditTextWatcher())

        lastEditedSegment = LastEditedSegment(editText, textView, updateSegmentButton, position)

        editText.setText(textView.text.toString())
        editText.visibility = View.VISIBLE
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        updateSegmentButton.visibility = View.VISIBLE
        textView.visibility = View.GONE

        updateSegmentButton.setOnClickListener {
            lastEditedSegment = null

            if (textView.text.toString() != editText.text.toString()) {
                currentNote.hasBeenChanged = true
                    currentNote.segments[position] = editText.text.toString()
                textView.text = editText.text.toString()
            }

            //reapply the strike-through indicator
            if (isStruckThrough) {
                currentNote.segments[position] = STRIKE_THROUGH_INDICATOR + editText.text.toString()
            }

            textView.visibility = View.VISIBLE
            editText.visibility = View.GONE
            updateSegmentButton.visibility = View.GONE
            currentlyEditedSegmentPosition = null
        }
    }

    private fun strikeThroughSegment(position: Int, text: String, textView: TextView): Boolean {
        if (text.contains(STRIKE_THROUGH_INDICATOR)) {
            currentNote.segments[position] = currentNote.segments[position].substring(3)
            textView.paintFlags = 0
        } else {
            currentNote.segments[position] = STRIKE_THROUGH_INDICATOR + currentNote.segments[position]
            textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
//        notifyItemChanged(position)
        notifyDataSetChanged()
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
                return if (position == itemCount - 1 || position == currentlyEditedSegmentPosition) {
                    //make the new segment EditText un-swipable
                    0
                } else {
                    super.getSwipeDirs(recyclerView, viewHolder)
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteSegment(viewHolder.adapterPosition)
//                notifyItemRemoved(viewHolder.adapterPosition)
                notifyDataSetChanged()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun newSegmentEditTextWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNote.newSegmentText = s.toString()
            }
            override fun afterTextChanged(s: Editable) { }
        }
    }

    private fun updatedSegmentEditTextWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNote.segments[currentlyEditedSegmentPosition!!] = s.toString()
                currentNote.hasBeenChanged = true
            }
            override fun afterTextChanged(s: Editable) { }
        }
    }
    private fun noteBodyEditTextWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                currentNote.hasBeenChanged = true
                currentNote.body = s.toString()
            }
            override fun afterTextChanged(s: Editable) { }
        }
    }
}