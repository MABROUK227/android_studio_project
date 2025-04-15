package com.example.tales.api

import com.example.tales.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ChatGptApiInterface {
    @POST("v1/chat/completions")
    suspend fun generateComptine(@Body request: ChatCompletionRequest): Response<ChatCompletionResponse>
}

data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000
)

data class Message(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

class ChatGptService {
    private val apiInterface: ChatGptApiInterface
    
    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.OPENAI_API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiInterface = retrofit.create(ChatGptApiInterface::class.java)
    }
    
    suspend fun generateComptineForChild(age: Int): Result<String> {
        return try {
            val prompt = "Génère une comptine courte et amusante pour un enfant de $age ans. La comptine doit être rythmée, facile à mémoriser et adaptée à l'âge de l'enfant."
            
            val request = ChatCompletionRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    Message(
                        role = "system",
                        content = "Tu es un créateur de comptines pour enfants. Tu dois générer des comptines courtes, amusantes et adaptées à l'âge des enfants."
                    ),
                    Message(
                        role = "user",
                        content = prompt
                    )
                )
            )
            
            val response = apiInterface.generateComptine(request)
            
            if (response.isSuccessful) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content
                if (content != null) {
                    Result.success(content)
                } else {
                    Result.failure(Exception("Réponse vide de l'API"))
                }
            } else {
                Result.failure(Exception("Erreur API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
