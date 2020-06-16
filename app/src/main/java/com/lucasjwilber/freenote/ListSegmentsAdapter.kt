package com.lucasjwilber.freenote

import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
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
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.viewmodels.EditListViewModel
import java.util.*

class ListSegmentsAdapter(private val vm: EditListViewModel) :
    RecyclerView.Adapter<ListSegmentsAdapter.MyViewHolder>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        initSwipeListener(recyclerView)
    }

    private val SEGMENT: Int = 0
    private val NEW_SEGMENT: Int = 1
    // 
    private class CurrentEditedSegment(var editText: EditText, var textView: TextView, var button: Button, var position: Int, var isStruckThrough: Boolean)
    private var currentEditedSegment: CurrentEditedSegment? = null


    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)

    override fun getItemViewType(position: Int): Int {
        return if (position == vm.segments.size) NEW_SEGMENT else SEGMENT
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

            val newSegmentEditText: EditText = constraintLayout.findViewById(R.id.newSegmentEditText)

            // the new segment EditText's content is tracked in the ViewModel so that it will be saved in case
            // the user forgets to add it as a new segment
            newSegmentEditText.addTextChangedListener(
                object: TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        vm.newSegmentText.value = s.toString()
                    }
                }
            )

            return MyViewHolder(
                constraintLayout
            )
        }
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (getItemViewType(position) == SEGMENT) {
            val textView: TextView = holder.constraintLayout.findViewById(R.id.segmentTextView)

            if (vm.segments[position].contains(
                    vm.STRIKE_THROUGH_INDICATOR
            )) {
                textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                textView.text = vm.segments[position].substring(3)
            } else {
                textView.text = vm.segments[position]
                textView.paintFlags = 0
            }

            textView.setOnClickListener { updateSegment(holder, textView) }
            textView.setOnLongClickListener { strikeThroughSegment(holder, textView) }

        } else { // if (getItemViewType(position) == NEW_SEGMENT)
            val newSegmentEditText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)

            val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
            saveButton.setOnClickListener { createNewSegment(newSegmentEditText) }

            if (vm.titleHasBeenSet) {
                newSegmentEditText.requestFocus()
            }

            newSegmentEditText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    vm.titleHasBeenSet = true
                    hideLastEditedSegment()
                }
            }
        }
    }

    override fun getItemCount() = vm.segments.size + 1

    fun createNewSegment(editText: EditText) {
        if (editText.text.isEmpty()) return

        val segs = ArrayList(vm.segments)
        segs.add(editText.text.toString())
        vm.segments = segs

        editText.text.clear()

        // notifyItemInserted() is intentionally not used here, in order to keep the
        // keyboard open and the edit text focused for a better UX
        notifyDataSetChanged()
    }

    fun deleteSegment(position: Int) {

        var delSegs = vm.deletedSegments.value!!
        delSegs.push(
            EditListViewModel.DeletedSegment(
                position,
                vm.segments[position]
            )
        )
        vm.deletedSegments.value = delSegs

        vm.segments.removeAt(position)

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

            val segs = ArrayList(vm.segments)
            if (vm.segments[ces.position].contains(
                    vm.STRIKE_THROUGH_INDICATOR
                )) {
                segs[ces.position] = vm.STRIKE_THROUGH_INDICATOR + updatedText
            } else {
                segs[ces.position] = updatedText
            }
            vm.segments = segs

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
        // hide the TextView, show the EditText and button to save changes
        val editText: EditText = holder.constraintLayout.findViewById(R.id.segmentEditText)
        val updateSegmentButton: Button = holder.constraintLayout.findViewById(R.id.updateSegmentButton)

        editText.setText(textView.text.toString())
        editText.visibility = View.VISIBLE
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        textView.visibility = View.GONE
        updateSegmentButton.visibility = View.VISIBLE

        var text = vm.segments[holder.adapterPosition]
        if (text.contains(vm.STRIKE_THROUGH_INDICATOR)) text = text.substring(3)

        val textWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                text = s.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        editText.addTextChangedListener(textWatcher)


        updateSegmentButton.setOnClickListener {
            textView.text = text

            if (vm.segments[holder.adapterPosition].contains(
                    vm.STRIKE_THROUGH_INDICATOR
                )) {
                text = vm.STRIKE_THROUGH_INDICATOR + text
            }
            vm.segments[holder.adapterPosition] = text

            textView.visibility = View.VISIBLE
            editText.visibility = View.GONE
            updateSegmentButton.visibility = View.GONE
            editText.removeTextChangedListener(textWatcher)
        }
    }

    private fun strikeThroughSegment(holder: MyViewHolder, textView: TextView): Boolean {
        var affectedSegment = vm.segments[holder.adapterPosition]

        // remove strike-through
        if (affectedSegment.contains(
                vm.STRIKE_THROUGH_INDICATOR
            )) {
            affectedSegment = affectedSegment.substring(3)
            textView.paintFlags = 0
        // apply strike-through
        } else {
            affectedSegment = vm.STRIKE_THROUGH_INDICATOR + affectedSegment
            textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }

        vm.segments[holder.adapterPosition] = affectedSegment
        notifyItemChanged(holder.adapterPosition)

        return true
    }

    // todo: move to activity?
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
                // the new segment viewholder at the bottom should not be swipeable
                return if (position == itemCount - 1) 0 else super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteSegment(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}