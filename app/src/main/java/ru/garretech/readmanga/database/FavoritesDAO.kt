package ru.garretech.readmanga.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
