package ru.garretech.readmanganew.tools

import androidx.room.TypeConverter
import java.util.*

class ListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        val newString = list.toString().substring(1, list.toString().length - 1)
        return newString
    }

    @TypeConverter
    fun toList(listAsString: String): List<String> {
        val newList =
            Arrays.asList(*listAsString.split("\\s*,\\s*".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray())
        return newList
    }

}
