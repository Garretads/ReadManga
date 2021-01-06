package ru.garretech.readmanganew.ui.mangaInfo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.garretech.readmanganew.database.AppDataSource
import ru.garretech.readmanganew.models.Manga

class MangaAboutViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = MangaAboutViewModel::class.java.simpleName

    var currentManga: Manga? = null
    private val dataSource = AppDataSource(application)

    fun getMangaFromDatabase(url: String, callback: (Manga) -> Unit ) =
        dataSource.getManga(url)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            currentManga = it
            callback(it)
        }, {
            Log.e(TAG, "Ошибка при получении манги из ДБ", it)
        })

}