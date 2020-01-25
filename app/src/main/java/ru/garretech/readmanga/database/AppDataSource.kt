package ru.garretech.readmanga.database

import android.content.Context
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.garretech.readmanga.models.Favorites
import ru.garretech.readmanga.models.History
import ru.garretech.readmanga.models.Manga
import java.util.*
import kotlin.collections.HashMap

class AppDataSource(context: Context) {
    private val appDatabase: AppDatabase by lazy { AppDatabase.getInstance(context)!! }
    private val mangaDAO: MangaDAO by lazy {
        appDatabase.movieDAO()
    }
    private val favoritesDAO: FavoritesDAO by lazy {
        appDatabase.favoritesDAO()
    }

    private val historyDAO: HistoryDAO by lazy {
        appDatabase.historyDAO()
    }

    val allMovies: List<Manga>
        get() = mangaDAO.allCachedMovies

    val listOfFavorites: Observable<List<Manga>>
        get() {
            val list = ArrayList<Manga>()

            val favorites = favoritesDAO.allFavorites

            for (favorite in favorites) {
                val movie = mangaDAO.getManga(favorite.mangaURL)
                list.add(movie!!)
            }
            return Observable.fromArray(list)
        }

    val listOfHistoryObservable: Observable<List<Manga>>
        get() {
            val historyList = historyDAO.allHistory
            val movieList = ArrayList<Manga>()

            for (history in historyList) {
                val movie = mangaDAO.getManga(history.mangaURL)
                if (movie != null)
                    movieList.add(movie)
            }
            return Observable.fromArray(movieList)
        }

    init {

    }

    fun addMovie(manga: Manga) {
        mangaDAO.addMovie(manga)
    }

    fun getManga(URL: String) =
        Single.create<Manga> {
            val manga = mangaDAO.getManga(URL)

            if (manga != null)
                it.onSuccess(manga)
            else
                it.onError(NullPointerException())
        }

    fun isInDatabase(url: String) =
        Single.create<Boolean> {
            val manga = mangaDAO.getManga(url)

            if (manga != null)
                it.onSuccess(true)
            else
                it.onSuccess(false)
        }

    fun isFavorite(URL: String): Boolean {
        val favorites = favoritesDAO.getFavoriteByURL(URL)
        return favorites != null
    }

    fun addFavorites(manga: Manga) {
        mangaDAO.addMovie(manga)
        Log.d("Database", "Manga " + manga.url + " added")
        val favorite = Favorites()
        favorite.mangaURL = manga.url
        favoritesDAO.addFavorites(favorite)
    }


    fun deleteFavorites(manga: Manga) {
        val favorites = favoritesDAO.getFavoriteByURL(manga.url)
        if (favorites != null)
            favoritesDAO.deleteFavorites(favorites)
    }

    fun getHistory(manga: Manga) =
        Single.create<History> {
            val history = historyDAO.getHistoryByURL(manga.url)

            if (history == null)
                it.onSuccess(History((manga.url)))
            else
                it.onSuccess(history)

        }

    fun saveHistory(history: History) =
        Completable.fromCallable {
            historyDAO.saveHistory(history)
        }

    fun saveHistory(manga: Manga) {
        var history = historyDAO.getHistoryByURL(manga.url)

        if (history == null) {
            history = History(manga.url).also { it.chapters = HashMap() }
            historyDAO.saveHistory(history)
        }

    }

    fun updateHistory(history: History) =
        Completable.fromCallable {
            historyDAO.updateHistory(history)
        }


    fun clearHistory() =
        Completable.fromCallable {
            historyDAO.clearHistory()
        }

    fun clearFavorites() =
        Completable.fromCallable {
            favoritesDAO.clearFavorites()
        }

}
