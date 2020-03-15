package ru.garretech.readmanga

object Settings {
    const val APP_PREFERENCES = "mysettings"
    var max_loaded_in_screen = 15
    const val BLOCK_ID = "adf-304149/991383"
    const val BLOCK_ID1 = "adf-304149/1036281"
    const val MYTARGET_ID = 618375
    const val VERSION_CODE = "version_code"

    const val READMANGA_URL = "https://readmanga.me"
    const val READMANGA_NAME = "readmanga.me"

    const val MINTMANGA_URL = "https://readmanga.me"
    const val MINTMANGA_NAME = "readmanga.me"


    fun max_loaded_in_screen(): Int {
        return max_loaded_in_screen
    }

    fun block_id(): String {
        return BLOCK_ID
    }
}
