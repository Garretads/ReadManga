package ru.garretech.readmanga.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.garretech.readmanga.database.AppDataSource
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.tools.SiteWorker

class MangaInfoActivityViewModel(application: Application) : AndroidViewModel(application) {

    val dataSource = AppDataSource(application)
    var currentManga : Manga? = null
    var isFavorite : Boolean = false


    fun getMangaInfo(url: String) = SiteWorker.getMangaInfo(url)
        .map {
            currentManga = it
            it
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun getMangaFromDatabase(url : String) = dataSource.getManga(url)
        .map {
            currentManga = it
            it
        }
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun addManga(manga: Manga) =
            Completable.fromCallable {
                dataSource.addMovie(manga)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    val isInFavorite =
            Single.create<Boolean> {
                it.onSuccess(dataSource.isFavorite(currentManga?.url!!))
            }.subscribeOn(Schedulers.io())
            .map {
                isFavorite = it
                isFavorite
            }


    val addFavorites =
        Completable.fromCallable { dataSource.addFavorites(currentManga!!) }
            .subscribeOn(Schedulers.io())

    val deleteFavorites =
        Completable.fromCallable { dataSource.deleteFavorites(currentManga!!) }
            .subscribeOn(Schedulers.io())

}