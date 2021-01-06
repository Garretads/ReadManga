package ru.garretech.readmanganew.interfaces

import ru.garretech.readmanganew.models.Chapter

interface OnExpandableItemClickListener {

    fun onChapterClick(item: Chapter)
}