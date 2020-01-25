package ru.garretech.readmanga.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.garretech.readmanga.database.AppDataSource
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.tools.HistoryProvider

class MangaReaderActivityViewModel(application: Application) : AndroidViewModel(application) {

    var currentManga: Manga? = null
    lateinit var historyProvider: HistoryProvider
    var dataSource = AppDataSource(application)


    fun prepareHistory(url: String) =
        getMangaFromDatabase(url).flatMap {
            currentManga = it
            getHistory()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun getMangaFromDatabase(url: String) = dataSource.getManga(url)
        .map {
            currentManga = it
            it
        }
        .subscribeOn(Schedulers.io())

    fun getHistory() =
        dataSource.getHistory(currentManga!!)
            .map {
                historyProvider = HistoryProvider(it)
            }
            .subscribeOn(Schedulers.io())

    fun addToHistory() =
        dataSource.saveHistory(historyProvider.history)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


}