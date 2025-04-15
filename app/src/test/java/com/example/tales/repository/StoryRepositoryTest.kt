package com.example.tales.repository

import com.example.tales.model.Story
import com.example.tales.model.StoryPage
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CompletableFuture

@RunWith(MockitoJUnitRunner::class)
class StoryRepositoryTest {

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockCollection: CollectionReference

    @Mock
    private lateinit var mockQuery: Query

    @Mock
    private lateinit var mockQuerySnapshot: QuerySnapshot

    @Mock
    private lateinit var mockDocumentReference: DocumentReference

    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    private lateinit var storyRepository: StoryRepository

    @Before
    fun setup() {
        `when`(mockFirestore.collection("stories")).thenReturn(mockCollection)
        storyRepository = StoryRepository(mockFirestore)
    }

    @Test
    fun `getAllStories returns list of stories`() = kotlinx.coroutines.runBlocking {
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

        `when`(mockCollection.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(CompletableFuture.completedFuture(mockQuerySnapshot).asTask())
        `when`(mockQuerySnapshot.toObjects(Story::class.java)).thenReturn(mockStories)

        // Act
        val result = storyRepository.getAllStories()

        // Assert
        assert(result == mockStories)
        verify(mockCollection).orderBy("createdAt", Query.Direction.DESCENDING)
    }

    @Test
    fun `getStoryById returns story when found`() = kotlinx.coroutines.runBlocking {
        // Arrange
        val storyId = "test-id"
        val mockStory = Story(
            id = storyId,
            title = "Test Story",
            description = "Test Description",
            content = listOf(StoryPage(1, "Page 1", "image.jpg"))
        )

        `when`(mockCollection.document(storyId)).thenReturn(mockDocumentReference)
        `when`(mockDocumentReference.get()).thenReturn(CompletableFuture.completedFuture(mockDocumentSnapshot).asTask())
        `when`(mockDocumentSnapshot.toObject(Story::class.java)).thenReturn(mockStory)

        // Act
        val result = storyRepository.getStoryById(storyId)

        // Assert
        assert(result == mockStory)
        verify(mockCollection).document(storyId)
    }

    @Test
    fun `saveStory saves story with new id when id is empty`() = kotlinx.coroutines.runBlocking {
        // Arrange
        val story = Story(
            id = "",
            title = "New Story",
            description = "New Description",
            content = listOf(StoryPage(1, "Page 1", "image.jpg"))
        )
        val newId = "new-id"

        `when`(mockCollection.document()).thenReturn(mockDocumentReference)
        `when`(mockDocumentReference.id).thenReturn(newId)
        `when`(mockCollection.document(newId)).thenReturn(mockDocumentReference)
        `when`(mockDocumentReference.set(Mockito.any())).thenReturn(CompletableFuture.completedFuture(null).asTask())

        // Act
        val result = storyRepository.saveStory(story)

        // Assert
        assert(result)
        verify(mockCollection).document()
        verify(mockDocumentReference).set(Mockito.any())
    }

    @Test
    fun `deleteStory deletes story by id`() = kotlinx.coroutines.runBlocking {
        // Arrange
        val storyId = "test-id"

        `when`(mockCollection.document(storyId)).thenReturn(mockDocumentReference)
        `when`(mockDocumentReference.delete()).thenReturn(CompletableFuture.completedFuture(null).asTask())

        // Act
        val result = storyRepository.deleteStory(storyId)

        // Assert
        assert(result)
        verify(mockCollection).document(storyId)
        verify(mockDocumentReference).delete()
    }
}
