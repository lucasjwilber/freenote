package com.lucasjwilber.freenote.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.lucasjwilber.freenote.PREFERENCES
import com.lucasjwilber.freenote.R
import com.lucasjwilber.freenote.THEME_CAFE
import com.lucasjwilber.freenote.THEME_CITY

class DeleteModalFragment : Fragment() {

    private var theme: Int = THEME_CAFE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val prefs = activity?.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (prefs != null) theme = prefs.getInt("theme", THEME_CAFE)

        // Inflate the layout for this fragment
        val deleteModalView = inflater.inflate(R.layout.delete_modal, container, false)

        setModalBackground(deleteModalView.findViewById(R.id.deleteModal))
        setCancelButtonColor(deleteModalView.findViewById(R.id.cancelDeleteButton))

        return deleteModalView
    }

    private fun setModalBackground(layout: ConstraintLayout) {
        layout.setBackgroundResource(
            when(theme) {
                THEME_CAFE -> R.drawable.rounded_square_bordered_cafe
                THEME_CITY -> R.drawable.rounded_square_bordered_city
                else -> R.drawable.rounded_square_bordered_cafe
            }
        )
    }

    private fun setCancelButtonColor(button: Button) {
        button.setTextColor(
            when(theme) {
                THEME_CAFE -> resources.getColor(R.color.cafeDark)
                THEME_CITY -> resources.getColor(R.color.cityDarkGreen)
                else -> resources.getColor(R.color.cafeDark)
            }
        )
    }
}