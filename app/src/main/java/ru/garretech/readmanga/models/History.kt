package ru.garretech.readmanga.models

import androidx.room.*
import ru.garretech.readmanga.tools.MapTypeConverter

@Entity(tableName = "history", indices = [Index("manga_url")])
class History(@PrimaryKey @field:ColumnInfo(name = "manga_url") var mangaURL: String) {

    @field:TypeConverters(MapTypeConverter::class)
    var chapters: HashMap<Int, List<Int>>? = null

}
