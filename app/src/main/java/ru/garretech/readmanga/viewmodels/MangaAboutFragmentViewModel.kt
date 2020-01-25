package ru.garretech.readmanga.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.garretech.readmanga.database.AppDataSource
import ru.garretech.readmanga.models.Manga

class MangaAboutFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentManga: Manga? = null
    val dataSource = AppDataSource(application)

    fun getMangaFromDatabase(url: String) = dataSource.getManga(url)
        .map {
            currentManga = it
            it
        }
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

}