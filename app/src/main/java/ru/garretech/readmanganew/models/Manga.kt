package ru.garretech.readmanganew.models

import android.graphics.Bitmap
import androidx.room.*
import ru.garretech.readmanganew.tools.ListConverter
import java.io.Serializable

@Entity
class Manga(
    var title: String?,
    @field:ColumnInfo(name = "manga_image_url")
    var mangaImageURL: String,
    @field:PrimaryKey
    var url: String
) : Serializable {

    @ColumnInfo(name = "production_year")
    var productionYear: String? = null

    @field:TypeConverters(ListConverter::class)
    var genres: List<String>? = null
    //private List<String> mainActors;
    //private List<String> actors;
    //private List<String> producers;
    @ColumnInfo(name = "production_country")
    var productionCountry: String? = null

    @ColumnInfo(name = "episodes_number")
    var chaptersNumber: String? = null

    var duration: String? = null

    @Ignore
    @Transient
    var image: Bitmap? = null

    @ColumnInfo(name = "image_cached_path")
    var imageCachedPath: String? = null

    var description: String? = null

    @ColumnInfo(name = "initial_episode")
    var lastChapter: String? = null


}