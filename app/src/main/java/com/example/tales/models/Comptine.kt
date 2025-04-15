package com.example.tales.models

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Comptine(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val audioUrl: String = "",
    val imageUrl: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
