package ru.garretech.readmanga

/**
 * Created by garred on 14.03.17.
 */

object Settings {
    const val APP_PREFERENCES = "mysettings"
    var max_loaded_in_screen = 15
    const val BLOCK_ID = "adf-304149/991383"
    const val VERSION_CODE = "version_code"



    fun max_loaded_in_screen(): Int {
        return max_loaded_in_screen
    }

    fun block_id(): String {
        return BLOCK_ID
    }
}
