package ru.garretech.readmanga.database

import androidx.room.*
import ru.garretech.readmanga.models.Favorites


@Dao
interface FavoritesDAO {

    @get:Query("SELECT * FROM favorites")
    val allFavorites: List<Favorites>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorites(favorites: Favorites): Long

    @Query("SELECT * FROM favorites WHERE id = :ids")
    fun getFavoriteByIndex(ids: Long): Favorites

    @Query("SELECT * FROM favorites WHERE manga_url = :URL")
    fun getFavoriteByURL(URL: String): Favorites?

    @Delete
    fun deleteFavorites(favorites: Favorites)

    @Query("DELETE FROM favorites")
    fun clearFavorites()
}
