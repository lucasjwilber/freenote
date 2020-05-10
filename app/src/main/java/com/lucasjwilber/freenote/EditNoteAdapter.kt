package com.lucasjwilber.freenote

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class EditNoteAdapter(private val segments: ArrayList<String>,
                      var newNote: Boolean,
                      var deletedSegments: Stack<EditNoteActivity.DeletedSegment>) :
    RecyclerView.Adapter<EditNoteAdapter.MyViewHolder>() {


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        setUpSwipeListener(recyclerView)
    }

    private val SEGMENT: Int = 0
    private val NEW_SEGMENT: Int = 1
    private lateinit var newSegmentEditText: EditText

    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout) { }

    override fun getItemViewType(position: Int): Int {
        return if (position == segments.size)
            NEW_SEGMENT
        else
            SEGMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {

        if (viewType == SEGMENT) {
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.segment, parent, false) as ConstraintLayout

            return MyViewHolder(constraintLayout)
        }
        else { //(viewType == NEW_SEGMENT)
            val constraintLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.new_segment, parent, false) as ConstraintLayout
            // set the view's size, margins, paddings and layout parameters
            newSegmentEditText = constraintLayout.findViewById(R.id.newSegmentEditText)
            return MyViewHolder(constraintLayout)
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (getItemViewType(position) == SEGMENT) {
            val textView: TextView = holder.constraintLayout.findViewById(R.id.segmentTextView)
            textView.text = segments[position]

        } else { //(getItemViewType(position) == NEW_SEGMENT)
            val editText: EditText = holder.constraintLayout.findViewById(R.id.newSegmentEditText)
            val saveButton: Button = holder.constraintLayout.findViewById(R.id.newSegmentSaveButton)
            saveButton.setOnClickListener { onNewSegmentSaveButtonClick(editText.text.toString()) }
            if (!newNote) {
                editText.requestFocus()
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = segments.size + 1

    private fun onNewSegmentSaveButtonClick(text: String) {
        if (text.isEmpty()) return

        segments.add(text)
        newSegmentEditText.text.clear()
        this.notifyItemInserted(segments.size)

        // flip newNote so the cursor focus will go to the new segment EditText
        newNote = false

        testString += "newsegmentsaved"
    }

    private fun deleteSegment(position: Int) {
        Log.i("ljw", "segments is $segments, position is $position")

        deletedSegments.push(EditNoteActivity.DeletedSegment(position, segments[position]))
        segments.removeAt(position)

        //todo: un-hide or re-color the undo button
    }


    private fun setUpSwipeListener(recyclerView: RecyclerView) {

        Log.i("ljw", "set up swipe listener")
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
                return if (position == itemCount - 1) {
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