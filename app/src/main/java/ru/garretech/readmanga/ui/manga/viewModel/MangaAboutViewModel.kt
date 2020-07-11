package ru.garretech.readmanga.ui.manga.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.garretech.readmanga.database.AppDataSource
import ru.garretech.readmanga.models.Manga

class MangaAboutViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = MangaAboutViewModel::class.java.simpleName

    var currentManga = MutableLiveData<Manga>()
    private val dataSource = AppDataSource(application)

    fun getMangaFromDatabase(url: String, callback: (Manga) -> Unit ) =
        dataSource.getManga(url)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            currentManga.value = it
            callback(it)
        }, {
            Log.e(TAG, "Ошибка при получении манги из ДБ", it)
        })

}