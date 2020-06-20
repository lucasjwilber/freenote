package com.lucasjwilber.freenote

import android.os.Build
import com.lucasjwilber.freenote.views.EditListActivity
import com.lucasjwilber.freenote.viewmodels.EditListViewModel
import kotlinx.android.synthetic.main.activity_all_notes.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class EditListActivityTests_newList {

    private val activity: EditListActivity = Robolectric.setupActivity(EditListActivity::class.java)
    private val viewModel = EditListViewModel(RuntimeEnvironment.application)

    @Test
    fun newList_titleEditTextIsFocusedOnStart() {
        assert(activity.binding.noteTitleEditText.hasFocus())
    }

    @Test
    fun newList_toolbarTitleIsCreateList() {
        assert(activity.toolbar.title.toString() == "Create List")
    }

}