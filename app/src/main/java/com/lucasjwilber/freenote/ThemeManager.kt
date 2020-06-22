package com.lucasjwilber.freenote

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

/*
new theme checklist:
* add the colors in res/values/colors
* add the style XML in res/values/styles and in the v21 file
* add an option for it in res/menu/all_notes_menu
* add the option to the if-block in AllNotesActivity's onOptionsItemSelected()
* create the necessary background layouts in res/drawable
* create the 'select type' images in res/drawable
* add new blocks to the when() flows in each method below
*/


object ThemeManager {
    private var app: Application? = null
    private var prefs: SharedPreferences? = null
    private var currentTheme = R.style.CafeTheme

    fun init(application: Application) {
        this.app = application
        prefs = application.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (prefs != null) {
            currentTheme = prefs!!.getInt("theme", R.style.CafeTheme)
        }
    }

    fun getTheme(): Int {
        return currentTheme
    }

    fun changeTheme(menuId: Int) {
        // translate the menu option id to the theme's id
        // the theme id is used everywhere else
        currentTheme = when (menuId) {
            R.id.menu_theme_cafe -> R.style.CafeTheme
            R.id.menu_theme_city -> R.style.CityTheme
            R.id.menu_theme_rose -> R.style.RoseTheme
            R.id.menu_theme_lavender -> R.style.LavenderTheme
            R.id.menu_theme_arctic -> R.style.ArcticTheme
            R.id.menu_theme_honey -> R.style.HoneyTheme
            else -> R.style.CafeTheme
        }
        prefs!!.edit().putInt("theme", currentTheme).apply()
    }

    fun getToastBackground(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.toast_background_cafe
            R.style.CityTheme -> R.drawable.toast_background_city
            R.style.RoseTheme -> R.drawable.toast_background_rose
            R.style.LavenderTheme -> R.drawable.toast_background_lavender
            R.style.ArcticTheme -> R.drawable.toast_background_arctic
            R.style.HoneyTheme -> R.drawable.toast_background_honey
            else -> R.drawable.toast_background_cafe
        }
    }

    fun getSegmentBulletDrawable(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.bullet_point_cafe
            R.style.CityTheme -> R.drawable.bullet_point_city
            R.style.RoseTheme -> R.drawable.bullet_point_rose
            R.style.LavenderTheme -> R.drawable.bullet_point_lavender
            R.style.ArcticTheme -> R.drawable.bullet_point_arctic
            R.style.HoneyTheme -> R.drawable.bullet_point_honey
            else -> R.drawable.bullet_point_cafe
        }
    }

    fun getModalBackground(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.rounded_square_bordered_cafe
            R.style.CityTheme -> R.drawable.rounded_square_bordered_city
            R.style.RoseTheme -> R.drawable.rounded_square_bordered_rose
            R.style.LavenderTheme -> R.drawable.rounded_square_bordered_lavender
            R.style.ArcticTheme -> R.drawable.rounded_square_bordered_arctic
            R.style.HoneyTheme -> R.drawable.rounded_square_bordered_honey
            else -> R.drawable.rounded_square_bordered_cafe
        }
    }

    fun getButtonColor(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.color.cafeDark
            R.style.CityTheme -> R.color.cityDarkGreen
            R.style.RoseTheme -> R.color.roseRed
            R.style.LavenderTheme -> R.color.lavenderPurple
            R.style.ArcticTheme -> R.color.arcticBlue
            R.style.HoneyTheme -> R.color.honeyGold
            else -> R.color.cafeDark
        }
    }

    fun getButtonTextColor(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.color.cafeLighter
            R.style.CityTheme -> R.color.cityLighterGray
            R.style.RoseTheme -> R.color.roseLighter
            R.style.LavenderTheme -> R.color.lavenderLighter
            R.style.ArcticTheme -> R.color.arcticLighter
            R.style.HoneyTheme -> R.color.honeyLighter
            else -> R.color.cafeLighter
        }
    }

    fun getTitleTextColor(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.color.cafeDarkest
            R.style.CityTheme -> R.color.cityDarkGray
            R.style.RoseTheme -> R.color.roseDarkRed
            R.style.LavenderTheme -> R.color.lavenderDarkPurple
            R.style.ArcticTheme -> R.color.arcticDarkBlue
            R.style.HoneyTheme -> R.color.honeyBrown
            else -> R.color.cafeDarkest
        }
    }

    fun getButtonBackground(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.rounded_square_filled_cafe
            R.style.CityTheme -> R.drawable.rounded_square_filled_city
            R.style.RoseTheme -> R.drawable.rounded_square_filled_rose
            R.style.LavenderTheme -> R.drawable.rounded_square_filled_lavender
            R.style.ArcticTheme -> R.drawable.rounded_square_filled_arctic
            R.style.HoneyTheme -> R.drawable.rounded_square_filled_honey
            else -> R.drawable.rounded_square_filled_cafe
        }
    }

    fun getRecyclerViewBackgroundColor(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.color.cafeLighter
            R.style.CityTheme -> R.color.cityLightGray
            R.style.RoseTheme -> R.color.roseLight
            R.style.LavenderTheme -> R.color.lavenderLight
            R.style.ArcticTheme -> R.color.arcticLight
            R.style.HoneyTheme -> R.color.honeyLight
            else -> R.color.white
        }
    }

    fun getNewNoteButtonImage(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.new_note_image_cafe
            R.style.CityTheme -> R.drawable.new_note_image_city
            R.style.RoseTheme -> R.drawable.new_note_image_rose
            R.style.LavenderTheme -> R.drawable.new_note_image_lavender
            R.style.ArcticTheme -> R.drawable.new_note_image_arctic
            R.style.HoneyTheme -> R.drawable.new_note_image_honey
            else -> R.drawable.new_note_image_cafe
        }
    }

    fun getNewListButtonImage(): Int {
        return when (currentTheme) {
            R.style.CafeTheme -> R.drawable.new_list_image_cafe
            R.style.CityTheme -> R.drawable.new_list_image_city
            R.style.RoseTheme -> R.drawable.new_list_image_rose
            R.style.LavenderTheme -> R.drawable.new_list_image_lavender
            R.style.ArcticTheme -> R.drawable.new_list_image_arctic
            R.style.HoneyTheme -> R.drawable.new_list_image_honey
            else -> R.drawable.new_list_image_cafe
        }
    }

}