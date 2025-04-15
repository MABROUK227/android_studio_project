package com.example.tales.service

import com.example.tales.model.ApiResponse
import com.example.tales.model.Story
import com.example.tales.model.StoryPage
import com.example.tales.model.StoryRequest
import com.example.tales.model.StoryPersonalization
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.mock

@RunWith(MockitoJUnitRunner::class)
class OpenAIServiceTest {

    @Mock
    private lateinit var mockOkHttpClient: OkHttpClient

    @Mock
    private lateinit var mockCall: Call

    private lateinit var openAIService: OpenAIService
    private val apiKey = "test-api-key"

    @Before
    fun setup() {
        openAIService = OpenAIService(apiKey, mockOkHttpClient)
    }

    @Test
    fun `generateStory returns success response when API call succeeds`() = runBlocking {
        // Arrange
        val storyRequest = StoryRequest(
            personalization = StoryPersonalization(
                childName = "Test Child",
                childAge = 5,
                favoriteAnimal = "Lion",
                favoriteColor = "Blue",
                favoriteActivity = "Reading"
            ),
            storyType = "Adventure"
        )

        val chatGptResponseJson = """
            {
                "id": "chatcmpl-123",
                "object": "chat.completion",
                "created": 1677652288,
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "{\n\"title\": \"Test Adventure\",\n\"description\": \"A story about Test Child and a lion\",\n\"pages\": [\n{\"pageNumber\": 1, \"text\": \"Test Child loved reading about lions.\", \"imageDescription\": \"A child reading a book about lions\"}\n]\n}"
                    },
                    "finish_reason": "stop"
                }]
            }
        """.trimIndent()

        val imageGenResponseJson = """
            {
                "created": 1677652288,
                "data": [{
                    "url": "https://example.com/image.jpg"
                }]
            }
        """.trimIndent()

        // Mock chat completion response
        val chatCompletionResponse = Response.Builder()
            .request(Request.Builder().url("https://api.openai.com/v1/chat/completions").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(chatGptResponseJson.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

        // Mock image generation response
        val imageGenResponse = Response.Builder()
            .request(Request.Builder().url("https://api.openai.com/v1/images/generations").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(imageGenResponseJson.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

        // Setup mock client to return our prepared responses
        `when`(mockOkHttpClient.newCall(any())).thenReturn(mockCall)
        
        // First call for chat completion
        `when`(mockCall.execute()).thenReturn(chatCompletionResponse)
            .thenReturn(imageGenResponse) // Second call for image generation
            .thenReturn(imageGenResponse) // Additional calls for more images if needed

        // Act
        val result = openAIService.generateStory(storyRequest)

        // Assert
        assert(result.success)
        assert(result.data is Story)
        val story = result.data as Story
        assert(story.title == "Test Adventure")
        assert(story.description == "A story about Test Child and a lion")
        assert(story.content.size == 1)
        assert(story.content[0].text == "Test Child loved reading about lions.")
    }

    @Test
    fun `generateStory returns error response when API call fails`() = runBlocking {
        // Arrange
        val storyRequest = StoryRequest(
            personalization = StoryPersonalization(
                childName = "Test Child",
                childAge = 5,
                favoriteAnimal = "Lion",
                favoriteColor = "Blue",
                favoriteActivity = "Reading"
            ),
            storyType = "Adventure"
        )

        val errorResponseJson = """
            {
                "error": {
                    "message": "Invalid API key",
                    "type": "invalid_request_error",
                    "code": "invalid_api_key"
                }
            }
        """.trimIndent()

        val errorResponse = Response.Builder()
            .request(Request.Builder().url("https://api.openai.com/v1/chat/completions").build())
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body(errorResponseJson.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

        `when`(mockOkHttpClient.newCall(any())).thenReturn(mockCall)
        `when`(mockCall.execute()).thenReturn(errorResponse)

        // Act
        val result = openAIService.generateStory(storyRequest)

        // Assert
        assert(!result.success)
        assert(result.message.contains("Erreur API: 401"))
    }
}
