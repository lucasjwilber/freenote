package com.lucasjwilber.freenote

import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.viewmodels.EditListViewModel

class ListSegmentsAdapter(private val vm: EditListViewModel) :
    RecyclerView.Adapter<ListSegmentsAdapter.MyViewHolder>() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        initSwipeListener(recyclerView)
    }

    private val SEGMENT: Int = 0
    private val NEW_SEGMENT: Int = 1
    // keep a reference to the currently edited segment to allow only one to be edited at a time
    // this is just a design decision
    private class CurrentEditedSegment(var editText: EditText, var textView: TextView, var button: Button, var textWatcher: TextWatcher)
    private var lastEditedSegment: CurrentEditedSegment? = null


    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)

    override fun getItemViewType(position: Int): Int {
        return if (position == vm.segments.size) NEW_SEGMENT else SEGMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        if (viewType == SEGMENT) {
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.segment, parent, false) as ConstraintLayout

            constraintLayout.findViewById<ImageView>(R.id.segmentBulletPoint).setImageResource(ThemeManager.getSegmentBulletDrawable())

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
                        vm.newSegmentText = s.toString()
                    }
                }
            )

            return MyViewHolder(constraintLayout)
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

            textView.setOnClickListener {
                turnLastEditedSegmentBackIntoATextView()
                updateSegment(holder, textView)
            }

            textView.setOnLongClickListener { toggleSegmentStrikeThrough(position, textView) }

        } else {
            val newSegmentEditText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)

            val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
            saveButton.setOnClickListener { addSegment(newSegmentEditText) }

            // focus the title EditText by default on new notes only, and only once, for a better UX
            if (vm.titleHasBeenSet) newSegmentEditText.requestFocus()

            newSegmentEditText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    vm.titleHasBeenSet = true
                    turnLastEditedSegmentBackIntoATextView()
                }
            }
        }
    }

    override fun getItemCount() = vm.segments.size + 1

    private fun addSegment(editText: EditText) {
        if (editText.text.isEmpty()) return

        vm.segments.add(editText.text.toString())
        editText.text.clear()

        // notifyItemInserted() is intentionally not used here, in order to keep the
        // keyboard open and the edit text focused for a better UX
        notifyDataSetChanged()
    }

    fun deleteSegment(position: Int) {
        val delSegs = vm.deletedSegments.value!!
        delSegs.push(
            EditListViewModel.DeletedSegment(
                position,
                vm.segments[position]
            )
        )
        vm.deletedSegments.value = delSegs

        vm.segments.removeAt(position)
        notifyItemRemoved(position)
    }


    private fun updateSegment(holder: MyViewHolder, textView: TextView) {
        // hide the TextView, show the EditText and button to save changes
        val editText: EditText = holder.constraintLayout.findViewById(R.id.segmentEditText)
        val updateSegmentButton: Button = holder.constraintLayout.findViewById(R.id.updateSegmentButton)

        // populate and show an EditText and hide the TextView of the segment
        editText.setText(textView.text.toString())
        editText.visibility = View.VISIBLE
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        textView.visibility = View.GONE
        updateSegmentButton.visibility = View.VISIBLE

        val textWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                vm.segments[holder.adapterPosition] =
                    if (vm.segments[holder.adapterPosition].contains(vm.STRIKE_THROUGH_INDICATOR)) {
                        vm.STRIKE_THROUGH_INDICATOR + s.toString()
                    } else {
                        s.toString()
                    }

                textView.text = s
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        editText.addTextChangedListener(textWatcher)

        lastEditedSegment = CurrentEditedSegment(
            editText,
            textView,
            updateSegmentButton,
            textWatcher
        )

        updateSegmentButton.setOnClickListener {
            turnLastEditedSegmentBackIntoATextView()
        }
    }

    private fun turnLastEditedSegmentBackIntoATextView() {
        if (lastEditedSegment != null) {
            val ces = lastEditedSegment!!
            ces.textView.visibility = View.VISIBLE
            ces.editText.visibility = View.GONE
            ces.button.visibility = View.GONE
            ces.editText.removeTextChangedListener(ces.textWatcher)
        }
    }

    private fun toggleSegmentStrikeThrough(position: Int, textView: TextView): Boolean {
        var affectedSegment = vm.segments[position]

        if (affectedSegment.contains( // remove strike-through
                vm.STRIKE_THROUGH_INDICATOR
            )) {
            affectedSegment = affectedSegment.substring(3)
            textView.paintFlags = 0

        } else { // apply strike-through
            affectedSegment = vm.STRIKE_THROUGH_INDICATOR + affectedSegment
            textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }

        vm.segments[position] = affectedSegment
        notifyItemChanged(position)

        return true
    }

    // todo: move this method to activity?
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
                return if (position == itemCount - 1)
                    0
                else
                    super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteSegment(viewHolder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}