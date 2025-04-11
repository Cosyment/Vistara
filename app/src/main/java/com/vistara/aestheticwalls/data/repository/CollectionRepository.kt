package com.vistara.aestheticwalls.data.repository

import com.vistara.aestheticwalls.data.model.Collection
import com.vistara.aestheticwalls.data.model.Wallpaper

interface CollectionRepository {
    suspend fun getCollection(id: String): Collection
    suspend fun getCollectionWallpapers(collectionId: String): List<Wallpaper>
    suspend fun getCollections(page: Int = 1, pageSize: Int = 20): List<Collection>
    suspend fun getFeaturedCollections(page: Int = 1, pageSize: Int = 20): List<Collection>
    suspend fun searchCollections(query: String, page: Int = 1, pageSize: Int = 20): List<Collection>
} 