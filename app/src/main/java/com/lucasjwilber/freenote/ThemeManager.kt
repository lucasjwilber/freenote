package com.lucasjwilber.freenote

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

/*
new theme checklist:
* add the colors in res/values/colors
* add the style XML in res/values/styles and in the v21 file
* create a new toast_background_[theme] drawable
* create a new rounded_square_bordered_[theme] drawable
* create a new border_bottom_segment_[theme] drawable
* add an option for it in res/menu/all_notes_menu
* add the option to the if-block in AllNotesActivity's onOptionsItemSelected()
* add new blocks to the when() flows in each method below
*/


object ThemeManager {
    private var app: Application? = null
    private var prefs: SharedPreferences? = null
    var currentTheme = R.style.CafeTheme

    fun init(application: Application) {
        app = application
        prefs = application.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (prefs != null) currentTheme = prefs!!.getInt("theme", R.style.CafeTheme)
    }

    fun changeTheme(menuId: Int) {
        val themeId = when (menuId) {
            R.id.menu_theme_cafe -> R.style.CafeTheme
            R.id.menu_theme_city -> R.style.CityTheme
            else -> R.style.CafeTheme
        }
        currentTheme = themeId
        app!!.setTheme(themeId)
        prefs!!.edit().putInt("theme", themeId).apply()
    }

    fun getToastBackground(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.toast_background_cafe
            R.style.CityTheme -> R.drawable.toast_background_city
            else -> R.drawable.toast_background_cafe
        }
    }

    fun getSegmentBackgroundDrawable(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.border_bottom_segment_cafe
            R.style.CityTheme -> R.drawable.border_bottom_segment_city
            else -> R.drawable.border_bottom_segment_cafe
        }
    }

    fun getSegmentBulletDrawable(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.bullet_point_cafe
            R.style.CityTheme -> R.drawable.bullet_point_city
            else -> R.drawable.bullet_point_cafe
        }
    }

    fun getDeleteModalBackground(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.rounded_square_bordered_cafe
            R.style.CityTheme -> R.drawable.rounded_square_bordered_city
            else -> R.drawable.rounded_square_bordered_cafe
        }
    }

    fun getCancelButtonColor(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.color.cafeDark
            R.style.CityTheme -> R.color.cityDarkGreen
            else -> R.color.cafeDark
        }
    }
}