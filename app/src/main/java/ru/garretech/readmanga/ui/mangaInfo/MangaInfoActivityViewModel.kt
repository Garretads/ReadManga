package ru.garretech.readmanga.ui.mangaInfo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.garretech.readmanga.database.AppDataSource
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.providers.SiteContentProvider

class MangaInfoActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = MangaInfoActivityViewModel::class.java.simpleName

    private val dataSource = AppDataSource(application)
    var currentManga: Manga? = null
    var isFavorite: Boolean = false

    private var disposableBag = CompositeDisposable()

    fun getMangaInfo(url: String, callback: (Manga) -> Unit) =
        getMangaFromDatabase(url)
            .flatMap {
                if (it.description == null)
                    getMangaFromAPISingle(url)
                else
                    Single.just(it)
            }.subscribe({
                callback(it)
            }, {
                getMangaFromAPI(url) {
                    addManga(it)
                    callback(it)
                }
            }).also { disposableBag.add(it) }

    fun getMangaFromAPISingle(url: String) = SiteContentProvider.getMangaInfo(url)
        .map {
            currentManga = it
            it
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    private fun getMangaFromAPI(url: String, callback: (Manga) -> Unit) =
        getMangaFromAPISingle(url)
            .subscribe({
                callback(it)
            }, {
                Log.e(TAG, "Ошибка получения информации о манге", it)
            }).also { disposableBag.add(it) }

    private fun getMangaFromDatabase(url: String) =
        dataSource.getManga(url)
            .map {
                currentManga = it
                it
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun addManga(manga: Manga, callback: (() -> Unit)? = null) =
        Completable.fromCallable {
            dataSource.addMovie(manga)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke()
            }, {
                Log.e(TAG, "Ошибка сохранения манги в БД", it)
            })

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

    override fun onCleared() {
        super.onCleared()
        disposableBag.dispose()
    }

}