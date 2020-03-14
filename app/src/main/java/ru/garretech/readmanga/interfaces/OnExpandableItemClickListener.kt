package ru.garretech.readmanga.interfaces

import ru.garretech.readmanga.models.Chapter

interface OnExpandableItemClickListener {

    fun onChapterClick(item: Chapter)
}