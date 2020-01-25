package ru.garretech.readmanga.models

import androidx.room.*

@Entity(
    tableName = "favorites",
    indices = [Index("manga_url")],
    foreignKeys = [ForeignKey(
        entity = Manga::class,
        parentColumns = ["url"],
        childColumns = ["manga_url"]
    )]
)
class Favorites {

    // Хранить ссылки на фильмы. Только и всего
    /*
    * Получить все ссылки (Полный список избранного. Или по частям, если с пагинацией)
    * Занести в избранное
    *
    * */

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "manga_url")
    lateinit var mangaURL: String
}
