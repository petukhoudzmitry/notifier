package com.tlscontact.repository

import com.tlscontact.dao.PageDao
import com.tlscontact.model.Page
import javax.inject.Inject

class PageRepository @Inject constructor(private val pageDao: PageDao) {
    suspend fun getAll(): List<Page> = pageDao.getAll()
    suspend fun loadAllByIds(pageIds: IntArray): List<Page> = pageDao.loadAllByIds(pageIds)
    suspend fun insertAll(pages: List<Page>) = pageDao.insertAll(pages)
    suspend fun delete(page: Page) = pageDao.delete(page)
    suspend fun update(page: Page) = pageDao.update(page)
}