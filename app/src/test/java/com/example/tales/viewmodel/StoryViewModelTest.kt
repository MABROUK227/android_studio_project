package com.example.tales.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.tales.model.ApiResponse
import com.example.tales.model.Story
import com.example.tales.model.StoryPage
import com.example.tales.model.StoryPersonalization
import com.example.tales.model.StoryRequest
import com.example.tales.repository.StoryRepository
import com.example.tales.service.OpenAIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var openAIService: OpenAIService

    @Mock
    private lateinit var storiesObserver: Observer<List<Story>>

    @Mock
    private lateinit var currentStoryObserver: Observer<Story?>

    @Mock
    private lateinit var loadingObserver: Observer<Boolean>

    @Mock
    private lateinit var errorObserver: Observer<String?>

    private lateinit var viewModel: StoryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StoryViewModel(storyRepository, openAIService)
        viewModel.stories.observeForever(storiesObserver)
        viewModel.currentStory.observeForever(currentStoryObserver)
        viewModel.isLoading.observeForever(loadingObserver)
        viewModel.error.observeForever(errorObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        viewModel.stories.removeObserver(storiesObserver)
        viewModel.currentStory.removeObserver(currentStoryObserver)
        viewModel.isLoading.removeObserver(loadingObserver)
        viewModel.error.removeObserver(errorObserver)
    }

    @Test
    fun `loadStories updates stories LiveData on success`() = testDispatcher.runBlockingTest {
        // Arrange
        val mockStories = listOf(
            Story(
                id = "1",
                title = "Test Story 1",
                description = "Test Description 1",
                content = listOf(StoryPage(1, "Page 1", "image1.jpg"))
            ),
            Story(
                id = "2",
                title = "Test Story 2",
                description = "Test Description 2",
                content = listOf(StoryPage(1, "Page 1", "image2.jpg"))
            )
        )
        `when`(storyRepository.getAllStories()).thenReturn(mockStories)

        // Act
        viewModel.loadStories()

        // Assert
        verify(loadingObserver).onChanged(true)
        verify(storyRepository).getAllStories()
        verify(storiesObserver).onChanged(mockStories)
        verify(loadingObserver).onChanged(false)
    }

    @Test
    fun `getStoryById updates currentStory LiveData on success`() = testDispatcher.runBlockingTest {
        // Arrange
        val storyId = "test-id"
        val mockStory = Story(
            id = storyId,
            title = "Test Story",
            description = "Test Description",
            content = listOf(StoryPage(1, "Page 1", "image.jpg"))
        )
        `when`(storyRepository.getStoryById(storyId)).thenReturn(mockStory)

        // Act
        viewModel.getStoryById(storyId)

        // Assert
        verify(loadingObserver).onChanged(true)
        verify(storyRepository).getStoryById(storyId)
        verify(currentStoryObserver).onChanged(mockStory)
        verify(loadingObserver).onChanged(false)
    }

    @Test
    fun `createStory updates currentStory LiveData on success`() = testDispatcher.runBlockingTest {
        // Arrange
        val personalization = StoryPersonalization(
            childName = "Test Child",
            childAge = 5,
            favoriteAnimal = "Lion",
            favoriteColor = "Blue",
            favoriteActivity = "Reading"
        )
        val storyType = "Adventure"
        val storyRequest = StoryRequest(personalization, storyType)
        
        val mockStory = Story(
            id = "new-id",
            title = "Test Adventure",
            description = "A story about Test Child",
            content = listOf(StoryPage(1, "Page 1", "image.jpg"))
        )
        
        val apiResponse = ApiResponse(true, "Success", mockStory)
        
        `when`(openAIService.generateStory(storyRequest)).thenReturn(apiResponse)
        `when`(storyRepository.saveStory(mockStory)).thenReturn(true)

        // Act
        viewModel.createStory(personalization, storyType)

        // Assert
        verify(loadingObserver).onChanged(true)
        verify(openAIService).generateStory(storyRequest)
        verify(storyRepository).saveStory(mockStory)
        verify(currentStoryObserver).onChanged(mockStory)
        verify(loadingObserver).onChanged(false)
    }

    @Test
    fun `createStory updates error LiveData on failure`() = testDispatcher.runBlockingTest {
        // Arrange
        val personalization = StoryPersonalization(
            childName = "Test Child",
            childAge = 5,
            favoriteAnimal = "Lion",
            favoriteColor = "Blue",
            favoriteActivity = "Reading"
        )
        val storyType = "Adventure"
        val storyRequest = StoryRequest(personalization, storyType)
        
        val errorMessage = "Failed to generate story"
        val apiResponse = ApiResponse(false, errorMessage, null)
        
        `when`(openAIService.generateStory(storyRequest)).thenReturn(apiResponse)

        // Act
        viewModel.createStory(personalization, storyType)

        // Assert
        verify(loadingObserver).onChanged(true)
        verify(openAIService).generateStory(storyRequest)
        verify(errorObserver).onChanged(errorMessage)
        verify(loadingObserver).onChanged(false)
    }
}
