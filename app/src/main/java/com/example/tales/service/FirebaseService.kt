package com.example.tales.service

import android.content.Context
import android.util.Log
import com.example.tales.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FirebaseService {
    companion object {
        private const val TAG = "FirebaseService"
        
        fun initialize(context: Context) {
            try {
                // Initialiser Firebase
                FirebaseApp.initializeApp(context)
                
                // Configurer Firestore
                val firestore = FirebaseFirestore.getInstance()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                firestore.firestoreSettings = settings
                
                Log.d(TAG, "Firebase initialisé avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'initialisation de Firebase: ${e.message}")
            }
        }
    }
}
