package com.tlscontact.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tlscontact.model.Page

@Dao
interface PageDao {
    @Query("SELECT * FROM pages")
    suspend fun getAll(): List<Page>

    @Query("SELECT * FROM pages WHERE uid IN (:pageIds)")
    suspend fun loadAllByIds(pageIds: IntArray): List<Page>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pages: List<Page>)

    @Delete
    suspend fun delete(page: Page)

    @Update
    suspend fun update(page: Page)
}