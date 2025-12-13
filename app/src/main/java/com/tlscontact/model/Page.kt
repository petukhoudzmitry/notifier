package com.tlscontact.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tlscontact.model.converter.TypeConverter

@Entity(tableName = "pages")
data class Page(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "articles")
    @field:TypeConverters(TypeConverter::class)
    val articles: List<Article>
)
