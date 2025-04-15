package com.example.tales.model

data class Story(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val coverImageUrl: String = "",
    val content: List<StoryPage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class StoryPage(
    val pageNumber: Int = 0,
    val text: String = "",
    val imageUrl: String = ""
)

data class StoryPersonalization(
    val childName: String = "",
    val childAge: Int = 0,
    val favoriteAnimal: String = "",
    val favoriteColor: String = "",
    val favoriteActivity: String = "",
    val additionalCharacters: List<String> = emptyList()
)

data class StoryRequest(
    val personalization: StoryPersonalization,
    val storyType: String = ""
)

data class ApiResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: Any? = null
)
