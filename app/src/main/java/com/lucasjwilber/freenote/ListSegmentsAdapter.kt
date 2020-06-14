package com.lucasjwilber.freenote

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*

// it's important that the LiveData is passed in here instead of its value so that updates here are reflected in the ViewModel
class ListSegmentsAdapter(var segmentsLD: MutableLiveData<List<String>>, var deletedSegmentsLD: MutableLiveData<Stack<DeletedSegment>>) :
    RecyclerView.Adapter<ListSegmentsAdapter.MyViewHolder>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        initSwipeListener(recyclerView)
    }

    private val SEGMENT: Int = 0
    private val NEW_SEGMENT: Int = 1
    private class CurrentEditedSegment(var editText: EditText, var textView: TextView, var button: Button, var position: Int, var isStruckThrough: Boolean)
    private var currentEditedSegment: CurrentEditedSegment? = null
    private var deletedSegments = Stack<DeletedSegment>()
    private var segments = ArrayList(segmentsLD.value!!)


    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)

    override fun getItemViewType(position: Int): Int {
        return if (position == segments.size) NEW_SEGMENT else SEGMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (viewType == SEGMENT) {
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.segment, parent, false) as ConstraintLayout

            return MyViewHolder(
                constraintLayout
            )
        } else { // NEW_SEGMENT
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.new_segment, parent, false) as ConstraintLayout

            newSegmentEditText = constraintLayout.findViewById(R.id.newSegmentEditText)

//            newSegmentEditText!!.addTextChangedListener(
//                makeTextWatcher(TW_NEW_SEGMENT)
//            )

            return MyViewHolder(
                constraintLayout
            )
        }
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (getItemViewType(position) == SEGMENT) {
            val textView: TextView = holder.constraintLayout.findViewById(R.id.segmentTextView)

            if (segments[position].contains(
                    STRIKE_THROUGH_INDICATOR
                )) {
                textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                textView.text = segments[position].substring(3)
            } else {
                textView.text = segments[position]
                textView.paintFlags = 0
            }

            textView.setOnClickListener { updateSegment(holder, textView) }
            textView.setOnLongClickListener { strikeThroughSegment(holder, textView) }

        } else { // if (getItemViewType(position) == NEW_SEGMENT)
            val editText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)

            val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
            saveButton.setOnClickListener { createNewSegment(editText.text.toString()) }

            newSegmentEditText = editText

//            if (currentNote.titleWasSet) {
//                editText.requestFocus()
//            }

            editText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
//                    currentNote.titleWasSet = true
                    hideLastEditedSegment()
                }
            }
        }
    }

    override fun getItemCount() = segments.size + 1

    private fun createNewSegment(text: String) {
        if (text.isEmpty()) return

        segments.add(text)
        segmentsLD.value = segments

        newSegmentEditText!!.text.clear()

        // don't use notifyItemInserted() here, in order to keep the keyboard open and the edit text focused
        notifyDataSetChanged()

//        currentNote.newSegmentText = ""

    }

    fun deleteSegment(position: Int) {
//        currentNote.deletedSegments.push(
//            DeletedSegment(
//                position,
//                segments[position]
//            )
//        )
//        undoButton?.isVisible = true

        deletedSegments.push(
            DeletedSegment(
                position,
                segments[position]
            )
        )
        deletedSegmentsLD.value = deletedSegments

        segments.removeAt(position)
        segmentsLD.value = segments

        // update currentlyEditedSegmentPosition accordingly.
        // can't use simple ++ or -- operators for some reason
//        if (currentNote.currentlyEditedSegmentPosition != null) {
//            if (currentNote.currentlyEditedSegmentPosition!! > position) {
//                currentNote.currentlyEditedSegmentPosition =
//                    currentNote.currentlyEditedSegmentPosition!! - 1
//            } else {
//                currentNote.currentlyEditedSegmentPosition =
//                    currentNote.currentlyEditedSegmentPosition!! + 1
//            }
//        }
        notifyItemRemoved(position)
    }

    private fun hideLastEditedSegment() {
        if (currentEditedSegment != null) {
            val ces: CurrentEditedSegment = currentEditedSegment!!

            ces.textView.visibility = View.VISIBLE

            val updatedText: String = ces.editText.text.toString()
            ces.textView.text = updatedText
            if (segments[ces.position].contains(
                    STRIKE_THROUGH_INDICATOR
                )) {
                segments[ces.position] = STRIKE_THROUGH_INDICATOR + updatedText
            } else {
                segments[ces.position] = updatedText
            }

            ces.editText.visibility = View.GONE
            ces.button.visibility = View.GONE
//            ces.editText.removeTextChangedListener(
//                makeTextWatcher(
//                    TW_UPDATED_SEGMENT
//                )
//            )
        }
    }

    private fun updateSegment(holder: MyViewHolder, textView: TextView) {
        //if the last edited segment wasn't 'saved', save the changes and update the textview
        hideLastEditedSegment()

        val editText: EditText = holder.constraintLayout.findViewById(R.id.segmentEditText)
        val updateSegmentButton: Button = holder.constraintLayout.findViewById(R.id.updateSegmentButton)

        currentEditedSegment =
            CurrentEditedSegment(
                editText,
                textView,
                updateSegmentButton,
                holder.adapterPosition,
                segments[holder.adapterPosition].contains(
                    STRIKE_THROUGH_INDICATOR
                )
            )

        val ces = currentEditedSegment
//        currentNote.currentlyEditedSegmentPosition = holder.adapterPosition
//        ces!!.editText.addTextChangedListener(
//            makeTextWatcher(
//                TW_UPDATED_SEGMENT
//            )
//        )
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
                segments[ces.position] = STRIKE_THROUGH_INDICATOR + ces.editText.text.toString()
            } else {
                segments[ces.position] = ces.editText.text.toString()
            }
            ces.textView.text = ces.editText.text.toString()
        }

        ces.textView.visibility = View.VISIBLE
        ces.editText.visibility = View.GONE
        ces.button.visibility = View.GONE

        currentEditedSegment = null
//        currentNote.currentlyEditedSegmentPosition = null
    }

    private fun strikeThroughSegment(holder: MyViewHolder, textView: TextView): Boolean {
        if (segments[holder.adapterPosition].contains(
                STRIKE_THROUGH_INDICATOR
            )) {
            segments[holder.adapterPosition] = segments[holder.adapterPosition].substring(3)
            textView.paintFlags = 0
        } else {
            segments[holder.adapterPosition] = STRIKE_THROUGH_INDICATOR + segments[holder.adapterPosition]
            textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        notifyItemChanged(holder.adapterPosition)

        return true
    }

    private fun initSwipeListener(recyclerView: RecyclerView) {
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
//                return if (position == itemCount - 1 || position == currentNote.currentlyEditedSegmentPosition) {
                return if (position == itemCount - 1) {
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