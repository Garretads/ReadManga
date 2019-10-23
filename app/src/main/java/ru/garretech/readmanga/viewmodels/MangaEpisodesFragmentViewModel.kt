package ru.garretech.readmanga.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.chad.library.adapter.base.entity.MultiItemEntity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import ru.garretech.readmanga.database.AppDataSource
import ru.garretech.readmanga.fragments.ProgressBottomSheet
import ru.garretech.readmanga.models.Chapter
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.models.Volume
import ru.garretech.readmanga.tools.HistoryProvider
import ru.garretech.readmanga.tools.SiteWorker

class MangaEpisodesFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentManga: Manga? = null

    var adapterList : List<MultiItemEntity> = ArrayList<MultiItemEntity>()
    var chapterJsonArray : JSONArray? = null

    var dataSource = AppDataSource(application)
    lateinit var historyProvider : HistoryProvider

    private var progressBottomSheet = ProgressBottomSheet()

    fun getWatchedChaptersInVolume(seriesIndex : Int) =
        historyProvider.getWatchedChaptersInVolume(seriesIndex)

    fun getWatchedVolumeIndexes() =
        historyProvider.getWatchedVolumeIndexes()

    fun getIndexOfChapterInAdapter(volumeIndex : Int, chapterNumber : Int) : Int {
        val volume = adapterList[volumeIndex] as Volume

        for ((index, element) in volume.subItems.withIndex()) {
            if (element is Chapter && element.chapterNumber == chapterNumber) {
                return index
            }
        }
        return -1
    }


    fun getChaptersList() =
        SiteWorker.formChaptersList(currentManga?.url!!, currentManga?.lastChapter!!)
            .map {
                adapterList = it["adapterList"] as List<MultiItemEntity>
                chapterJsonArray = it["chapterJsonArray"] as JSONArray

                it
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun getHistory() =
        dataSource.getHistory(currentManga!!)
            .map {
                historyProvider = HistoryProvider(it)
                historyProvider
            }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun addToHistory() =
        dataSource.saveHistory(historyProvider.history)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun getMangaFromDatabase(url : String) = dataSource.getManga(url)
        .map {
            currentManga = it
            it
        }
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}