package ru.garretech.readmanganew.tools

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapTypeConverter {

    @TypeConverter
    fun fromString(value: String): HashMap<Int, List<Int>> {
        val mapType = object : TypeToken<HashMap<Int, List<Int>>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringMap(map: HashMap<Int, List<Int>>): String {
        val gson = Gson()
        return gson.toJson(map)
    }

}