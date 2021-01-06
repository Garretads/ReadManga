package ru.garretech.readmanganew.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.garretech.readmanganew.database.AppDataSource
import ru.garretech.readmanganew.fragments.ProgressBottomSheet
import ru.garretech.readmanganew.models.Manga
import ru.garretech.readmanganew.providers.SiteContentProvider
import java.util.*
import java.util.concurrent.ExecutionException

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    var observable: Observable<List<Manga>>? = null
    var dataSource = AppDataSource(application)
    var mSiteWorker = SiteContentProvider()
    var requestQuery: SiteContentProvider.RequestQuery? = null
    var progressBottomSheet = ProgressBottomSheet()

    var title: String = ""

    fun getGenresList() =
        SiteContentProvider.genresList.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    @Throws(InterruptedException::class, ExecutionException::class, NullPointerException::class)
    fun getRequestQueryCompletable(
        requestType: Int,
        path: String,
        params: HashMap<String, String>
    ): Completable {
        return Completable.fromCallable {
            requestQuery = mSiteWorker.RequestQuery(getApplication(), requestType, path, params)
            observable = requestQuery!!.nextQuery
            null
        }.subscribeOn(Schedulers.io())
    }

    @Throws(InterruptedException::class, ExecutionException::class, NullPointerException::class)
    fun getRequestQueryCompletable(requestType: Int, path: String) =
        Completable.fromCallable {
            requestQuery = mSiteWorker.RequestQuery(getApplication(), requestType, path)
            observable = requestQuery!!.nextQuery
            null
        }.subscribeOn(Schedulers.io())

    @Throws(InterruptedException::class, ExecutionException::class, NullPointerException::class)
    fun getRequestQueryCompletable(requestType: Int) =
        Completable.fromCallable {
            requestQuery = mSiteWorker.RequestQuery(getApplication(), requestType)
            observable = requestQuery!!.nextQuery
            null
        }


    val favoritesObservable =
        Completable.fromCallable {
            observable = dataSource.listOfFavorites
            null
        }.subscribeOn(Schedulers.io())

    val historyObservable =
        Completable.fromCallable {
            observable = dataSource.listOfHistoryObservable
            null
        }.subscribeOn(Schedulers.io())


    val nextQueryObservable =
        Completable.fromCallable {
            observable = requestQuery!!.nextQuery
            null
        }.subscribeOn(Schedulers.io())

    val onRefreshObservableNetwork =
        Completable.fromCallable {

            if (requestQuery != null)
                requestQuery!!.resetOffset()
            else
                requestQuery =
                    mSiteWorker.RequestQuery(getApplication(), SiteContentProvider.EDITOR_CHOICE_QUERY)

            observable = requestQuery!!.nextQuery

            null
        }.subscribeOn(Schedulers.io())

    fun clearHistory() =
        dataSource.clearHistory().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun clearFavorites() =
        dataSource.clearFavorites().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

}