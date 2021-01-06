package ru.garretech.readmanganew.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import ru.garretech.readmanganew.models.Manga

@Dao
interface MangaDAO {


    @get:Query("SELECT * FROM manga")
    val allCachedMovies: List<Manga>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMovie(movie: Manga): Long

    @Query("SELECT * FROM Manga WHERE URL = :URL")
    fun getManga(URL: String): Manga?

}
