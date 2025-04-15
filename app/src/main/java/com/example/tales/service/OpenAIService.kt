package com.example.tales.service

import com.example.tales.model.ApiResponse
import com.example.tales.model.Story
import com.example.tales.model.StoryPage
import com.example.tales.model.StoryRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAIService(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val baseUrl = "https://api.openai.com/v1"
    
    suspend fun generateStory(storyRequest: StoryRequest): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val prompt = createStoryPrompt(storyRequest)
            val response = callChatGPT(prompt)
            
            if (response.success && response.data != null) {
                val storyJson = response.data.toString()
                val story = parseStoryJson(storyJson, storyRequest)
                
                // Générer les images pour chaque page
                val storyWithImages = generateImagesForStory(story)
                
                return@withContext ApiResponse(true, "Histoire générée avec succès", storyWithImages)
            } else {
                return@withContext ApiResponse(false, "Erreur lors de la génération de l'histoire: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ApiResponse(false, "Erreur lors de la génération de l'histoire: ${e.message}")
        }
    }
    
    private fun createStoryPrompt(storyRequest: StoryRequest): String {
        val personalization = storyRequest.personalization
        return """
            Crée une histoire pour enfant adaptée à l'âge ${personalization.childAge} ans.
            L'enfant s'appelle ${personalization.childName}.
            Son animal préféré est ${personalization.favoriteAnimal}.
            Sa couleur préférée est ${personalization.favoriteColor}.
            Son activité préférée est ${personalization.favoriteActivity}.
            ${if (personalization.additionalCharacters.isNotEmpty()) "Inclure ces personnages: ${personalization.additionalCharacters.joinToString(", ")}." else ""}
            
            Type d'histoire: ${storyRequest.storyType}
            
            Format de réponse: JSON avec la structure suivante:
            {
                "title": "Titre de l'histoire",
                "description": "Brève description de l'histoire",
                "pages": [
                    {
                        "pageNumber": 1,
                        "text": "Texte de la page 1",
                        "imageDescription": "Description détaillée pour générer l'image de la page 1"
                    },
                    ...
                ]
            }
            
            L'histoire doit avoir entre 5 et 8 pages.
            Chaque page doit contenir environ 2-3 phrases adaptées à l'âge de l'enfant.
            Pour chaque page, inclure une description détaillée de l'image qui devrait l'accompagner.
            Assure-toi que l'histoire est adaptée aux enfants, positive et éducative.
        """.trimIndent()
    }
    
    private suspend fun callChatGPT(prompt: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("model", "gpt-4")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "Tu es un auteur d'histoires pour enfants. Tu dois créer des histoires adaptées à l'âge indiqué, avec un langage simple et des thèmes positifs.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
            }
            
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            
            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseBody)
                val content = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                
                // Extraire le JSON de la réponse
                val jsonPattern = "\\{.*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
                val jsonMatch = jsonPattern.find(content)
                val jsonContent = jsonMatch?.value ?: content
                
                return@withContext ApiResponse(true, "Réponse générée avec succès", jsonContent)
            } else {
                return@withContext ApiResponse(false, "Erreur API: ${response.code} - $responseBody")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ApiResponse(false, "Erreur lors de l'appel à ChatGPT: ${e.message}")
        }
    }
    
    private suspend fun generateImage(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("model", "dall-e-3")
                put("prompt", "Illustration pour une histoire pour enfants: $prompt. Style coloré, adapté aux enfants, sécuritaire et éducatif.")
                put("n", 1)
                put("size", "1024x1024")
            }
            
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            
            val request = Request.Builder()
                .url("$baseUrl/images/generations")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseBody)
                return@withContext jsonResponse
                    .getJSONArray("data")
                    .getJSONObject(0)
                    .getString("url")
            } else {
                throw Exception("Erreur API: ${response.code} - $responseBody")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    private suspend fun generateImagesForStory(story: Story): Story = withContext(Dispatchers.IO) {
        try {
            // Générer l'image de couverture
            val coverPrompt = "Illustration de couverture pour une histoire pour enfants intitulée '${story.title}'. ${story.description}"
            val coverImageUrl = generateImage(coverPrompt)
            
            // Générer les images pour chaque page
            val updatedPages = story.content.map { page ->
                val imageDescription = page.imageUrl // Temporairement stocké dans imageUrl
                val imageUrl = generateImage(imageDescription)
                page.copy(imageUrl = imageUrl)
            }
            
            return@withContext story.copy(coverImageUrl = coverImageUrl, content = updatedPages)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext story
        }
    }
    
    private fun parseStoryJson(jsonString: String, storyRequest: StoryRequest): Story {
        try {
            val jsonObject = JSONObject(jsonString)
            
            val title = jsonObject.getString("title")
            val description = jsonObject.getString("description")
            
            val pagesArray = jsonObject.getJSONArray("pages")
            val pages = mutableListOf<StoryPage>()
            
            for (i in 0 until pagesArray.length()) {
                val pageObject = pagesArray.getJSONObject(i)
                val pageNumber = pageObject.getInt("pageNumber")
                val text = pageObject.getString("text")
                val imageDescription = pageObject.getString("imageDescription")
                
                pages.add(StoryPage(pageNumber, text, imageDescription))
            }
            
            return Story(
                title = title,
                description = description,
                content = pages,
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
