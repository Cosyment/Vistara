package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.Category

interface CategoryRepository {
    suspend fun getCategories(): List<Category>
    suspend fun getStaticCategories(): List<Category>
    suspend fun getLiveCategories(): List<Category>
    suspend fun getFeaturedCategories(): List<Category>
    suspend fun getCategory(id: String): Category
    suspend fun searchCategories(query: String): List<Category>
} 