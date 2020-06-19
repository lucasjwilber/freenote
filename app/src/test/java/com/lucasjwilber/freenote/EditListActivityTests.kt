package com.lucasjwilber.freenote

import android.graphics.Paint
import android.os.Build
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.lucasjwilber.freenote.activities.EditListActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class EditListActivityTests {

    private val activity: EditListActivity = Robolectric.setupActivity(EditListActivity::class.java)
    private fun getNewSegmentLayout(): ConstraintLayout {
        return activity.binding.noteSegmentsRV.findViewHolderForAdapterPosition(activity.viewModel.segments.size)?.itemView?.findViewById(R.id.newSegment)!!
    }
    private fun getSegmentLayoutAtPosition(position: Int): ConstraintLayout {
        return activity.binding.noteSegmentsRV.findViewHolderForAdapterPosition(position)?.itemView?.findViewById(R.id.segment)!!
    }

    @Test
    fun canAddNewSegmentsToViewModelVarFromTheAdapter() {
        val segmentsSizeBeforeClick = activity.viewModel.segments.size
        getNewSegmentLayout().findViewById<EditText>(R.id.newSegmentEditText)?.setText("yoooo")
        getNewSegmentLayout().findViewById<Button>(R.id.newSegmentSaveButton)?.performClick()

        assert(activity.viewModel.segments.size == segmentsSizeBeforeClick + 1)
        assert(activity.viewModel.segments[0] == "yoooo")
        // the EditText should be cleared when a segment is added
        assert(getNewSegmentLayout().findViewById<EditText>(R.id.newSegmentEditText)?.text.toString().isEmpty())
    }

    @Test
    fun cannotAddEmptySegments() {
        val segmentsSizeBeforeClick = activity.viewModel.segments.size
        getNewSegmentLayout().findViewById<EditText>(R.id.newSegmentEditText)?.setText("")
        getNewSegmentLayout().findViewById<Button>(R.id.newSegmentSaveButton)?.performClick()

        assert(activity.viewModel.segments.size == segmentsSizeBeforeClick)
    }

    @Test
    fun longClickingASegmentTogglesStrikeThrough() {
        // add a segment for the test
        getNewSegmentLayout().findViewById<EditText>(R.id.newSegmentEditText)?.setText("you'll never get through ME")
        getNewSegmentLayout().findViewById<Button>(R.id.newSegmentSaveButton)?.performClick()

        val segmentTextView = getSegmentLayoutAtPosition(0).findViewById<TextView>(R.id.segmentTextView)
        // assert no strikethrough by default
        assert(segmentTextView.paintFlags == 0)
        assert(!activity.viewModel.segments[0].contains(activity.viewModel.STRIKE_THROUGH_INDICATOR))

        // long click adds strike through to textview and STRIKE_THROUGH_INDICATOR to the segment element in the viewmodel
        segmentTextView.performLongClick()
        assert(segmentTextView.paintFlags == Paint.STRIKE_THRU_TEXT_FLAG)
        assert(activity.viewModel.segments[0].contains(activity.viewModel.STRIKE_THROUGH_INDICATOR))
        println(activity.viewModel.segments.toString())

        // another longclick removes the strikethrough and the indicator from the element
        segmentTextView.performLongClick()
        assert(segmentTextView.paintFlags == 0)
        assert(!activity.viewModel.segments[0].contains(activity.viewModel.STRIKE_THROUGH_INDICATOR))
    }

}