package com.lucasjwilber.freenote.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lucasjwilber.freenote.R
import com.lucasjwilber.freenote.ThemeManager

class SelectTypeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val selectTypeView = inflater.inflate(R.layout.select_type, container, false)
        selectTypeView.findViewById<ImageView>(R.id.selectNoteButton).setImageResource(ThemeManager.getNewNoteButtonImage())
        selectTypeView.findViewById<ImageView>(R.id.selectListButton).setImageResource(ThemeManager.getNewListButtonImage())
        selectTypeView.findViewById<LinearLayout>(R.id.selectTypeNoteSection).setBackgroundResource(ThemeManager.getModalBackground())
        selectTypeView.findViewById<LinearLayout>(R.id.selectTypeListSection).setBackgroundResource(ThemeManager.getModalBackground())
        selectTypeView.findViewById<TextView>(R.id.selectNoteTV).setTextColor(resources.getColor(ThemeManager.getTitleTextColor()))
        selectTypeView.findViewById<TextView>(R.id.selectListTV).setTextColor(resources.getColor(ThemeManager.getTitleTextColor()))

        return selectTypeView
    }
}