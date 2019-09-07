package ru.garretech.readmanga.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

import ru.garretech.readmanga.models.Favorites
import ru.garretech.readmanga.models.History
import ru.garretech.readmanga.models.Manga
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import android.icu.lang.UCharacter.GraphemeClusterBreak.V



@Database(entities = [Manga::class, Favorites::class, History::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDAO(): MangaDAO
    abstract fun favoritesDAO(): FavoritesDAO
    abstract fun historyDAO() : HistoryDAO

    companion object {

        private val DATABASE_NAME = "manga_database"
        private var INSTANCE: AppDatabase? = null


        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {

                    INSTANCE = Room.databaseBuilder<AppDatabase>(context, AppDatabase::class.java, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build()
                }
            }
            return INSTANCE
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `history` (`chapters` TEXT, `manga_url` TEXT NOT NULL, PRIMARY KEY(`manga_url`))")
            }
        }

    }
}
