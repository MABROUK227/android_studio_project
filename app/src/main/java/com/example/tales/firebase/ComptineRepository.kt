package com.example.tales.firebase

import com.example.tales.models.Comptine
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ComptineRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val comptinesCollection = firestore.collection("comptines")
    
    suspend fun getAllComptines(): List<Comptine> {
        return try {
            comptinesCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Comptine::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getComptineById(id: String): Comptine? {
        return try {
            comptinesCollection
                .document(id)
                .get()
                .await()
                .toObject(Comptine::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun addComptine(comptine: Comptine): Boolean {
        return try {
            val docRef = comptinesCollection.document()
            val comptineWithId = comptine.copy(id = docRef.id)
            docRef.set(comptineWithId).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateComptine(comptine: Comptine): Boolean {
        return try {
            comptinesCollection.document(comptine.id).set(comptine).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteComptine(id: String): Boolean {
        return try {
            comptinesCollection.document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
