package com.lucasjwilber.freenote.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.lucasjwilber.freenote.*

class DeleteModalFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val deleteModalView = inflater.inflate(R.layout.delete_modal, container, false)
        deleteModalView.findViewById<ConstraintLayout>(R.id.deleteModal).setBackgroundResource(ThemeManager.getDeleteModalBackground())
        deleteModalView.findViewById<Button>(R.id.cancelDeleteButton).setTextColor(resources.getColor(ThemeManager.getCancelButtonColor()))

        return deleteModalView
    }
}