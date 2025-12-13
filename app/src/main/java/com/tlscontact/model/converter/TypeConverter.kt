package com.tlscontact.model.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tlscontact.model.Article

class TypeConverter {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val type = Types.newParameterizedType(List::class.java, Article::class.java)
    private val articleListAdapter = moshi.adapter<List<Article>>(type)


    @TypeConverter
    fun fromList(list: List<Article>): String {
        return articleListAdapter.toJson(list)
    }

    @TypeConverter
    fun fromJson(value: String): List<Article> {
        return articleListAdapter.fromJson(value) ?: emptyList()
    }
}