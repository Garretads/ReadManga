package ru.garretech.readmanganew.ui.reader

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import ru.garretech.readmanganew.database.AppDataSource
import ru.garretech.readmanganew.models.Manga
import ru.garretech.readmanganew.providers.HistoryProvider
import ru.garretech.readmanganew.providers.SiteContentProvider

class MangaReaderActivityViewModel(application: Application) : AndroidViewModel(application) {

    val TAG = MangaReaderActivityViewModel::class.java.simpleName

    var currentManga : Manga? = null
    lateinit var historyProvider : HistoryProvider
    var dataSource = AppDataSource(application)

    var disposableBag = CompositeDisposable()

    fun prepareHistory(url : String) =
        getMangaFromDatabase(url).flatMap {
            currentManga = it
            getHistory()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun getMangaFromDatabase(url : String) = dataSource.getManga(url)
        .map {
            currentManga = it
            it
        }
        .subscribeOn(Schedulers.io())

    fun getHistory() =
        dataSource.getHistory(currentManga!!)
            .map {
                historyProvider =
                    HistoryProvider(it)
            }
            .subscribeOn(Schedulers.io())

    fun addToHistory(callback: () -> Unit) {
        val disposable = dataSource.saveHistory(historyProvider.history)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback()
            }, {
                Log.e(TAG, "Ошибка сохранения истории", it)
            })
        disposableBag.add(disposable)
    }

    private fun getPhotosRequestSingle(url: String) : Single<JSONArray> {
        return Single.create { observer ->
            val jsonArray = SiteContentProvider.getMangaImageList(url)
            observer.onSuccess(jsonArray)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposableBag.dispose()
    }

}