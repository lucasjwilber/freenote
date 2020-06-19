package com.lucasjwilber.freenote

import android.content.Intent
import android.os.Build
import android.view.View
import com.lucasjwilber.freenote.activities.AllNotesActivity
import com.lucasjwilber.freenote.activities.EditListActivity
import com.lucasjwilber.freenote.activities.EditNoteActivity
import com.lucasjwilber.freenote.viewmodels.AllNotesViewModel
import kotlinx.android.synthetic.main.activity_all_notes.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class AllNotesActivityTests {

    private val activity: AllNotesActivity = Robolectric.setupActivity(AllNotesActivity::class.java)
    private val viewModel = AllNotesViewModel(RuntimeEnvironment.application)

    @Test
    fun clickingFAB_shouldShowSelectionModal() {
        activity.createNewNoteOrListButton.performClick()
        assert(activity.binding.selectTypeBackground.visibility == View.VISIBLE)
    }

    @Test
    fun clickingSelectNoteButton_shouldStartEditNoteActivity() {
        activity.binding.selectNoteButton.performClick()
        val expectedIntent: Intent = Intent(activity, EditNoteActivity::class.java)
        val actualIntent: Intent = shadowOf(RuntimeEnvironment.application).nextStartedActivity
        assert(expectedIntent.component == actualIntent.component)
    }

    @Test
    fun clickingSelectListButton_shouldStartEditListActivity() {
        activity.binding.selectListButton.performClick()
        val expectedIntent: Intent = Intent(activity, EditListActivity::class.java)
        val actualIntent: Intent = shadowOf(RuntimeEnvironment.application).nextStartedActivity
        assert(expectedIntent.component == actualIntent.component)
    }

}