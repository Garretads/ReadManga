package ru.garretech.readmanga.tools

import ru.garretech.readmanga.models.History


class HistoryProvider(val history: History) {

    init {
        if (history.chapters == null)
            history.chapters = HashMap()
    }


    fun getWatchedChaptersInVolume(volumeIndex: Int) : List<Int> {
        return if (history.chapters!!.containsKey(volumeIndex)) {
            history.chapters!![volumeIndex]!!
        } else {
            emptyList()
        }
    }

    fun getWatchedVolumeNumbers() : List<Int> {
        return if (history.chapters != null)
            history.chapters!!.keys.toList()
        else
            emptyList()
    }

    fun addChapter(volumeIndex : Int, chapterIndex : Int) {
        if (history.chapters!!.containsKey(volumeIndex)) {
            val idArray = history.chapters!![volumeIndex]!!
            val newArray = ArrayList<Int>()
            newArray.addAll(idArray)

            if (!newArray.contains(chapterIndex))
                newArray.add(chapterIndex)

            history.chapters!![volumeIndex] = newArray.toList()
        }
        else {
            val idArray = ArrayList<Int>()
            idArray.add(chapterIndex)
            history.chapters!![volumeIndex] = idArray
        }
    }

    fun getLastWatchedChapter() : HashMap<Int,Int>? {
        return if (history.chapters != null) {

            val lastVolume = history.chapters!!.keys.last()
            val lastChapter = history.chapters!![lastVolume]!!.last()

            HashMap<Int,Int>().also { it[lastVolume] = lastChapter }
        } else {
            null
        }
    }

}