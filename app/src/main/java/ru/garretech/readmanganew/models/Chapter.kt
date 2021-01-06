package ru.garretech.readmanganew.models

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

class Chapter(name: String, val chapterNumber: Int, val volumeNumber: Int, val link: String) :
    MultiItemEntity, Serializable {
    override fun getItemType(): Int {
        return TYPE
    }

    val chapterTitleName = "$chapterNumber. $name"

    companion object {
        const val TYPE = 1
    }
}