package com.example.tales.repository

import com.example.tales.model.Story
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storiesCollection = firestore.collection("stories")
    
    suspend fun getAllStories(): List<Story> = withContext(Dispatchers.IO) {
        try {
            val snapshot = storiesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            return@withContext snapshot.toObjects(Story::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
    
    suspend fun getStoryById(storyId: String): Story? = withContext(Dispatchers.IO) {
        try {
            val document = storiesCollection.document(storyId).get().await()
            return@withContext document.toObject(Story::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    
    suspend fun saveStory(story: Story): Boolean = withContext(Dispatchers.IO) {
        try {
            val storyId = story.id.ifEmpty { storiesCollection.document().id }
            val storyWithId = story.copy(id = storyId)
            
            storiesCollection.document(storyId).set(storyWithId).await()
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    
    suspend fun deleteStory(storyId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            storiesCollection.document(storyId).delete().await()
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
