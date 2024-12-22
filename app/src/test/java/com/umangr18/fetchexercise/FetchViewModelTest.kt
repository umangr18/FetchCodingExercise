package com.umangr18.fetchexercise

import FetchApiService
import FetchViewModel
import ListItem
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class FetchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var viewModel: FetchViewModel
    private val mockApiService = mock(FetchApiService::class.java)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FetchViewModel(mockApiService)
    }

    @Test
    fun `test initial state is loading`() {
        assertEquals(FetchScreenContract.UiState.ContentLoading, viewModel.screenState.value)
    }

    @Test
    fun `test successful network response`() = runTest {
        val mockData = listOf(
            ListItem(2, 1, "Item 2"),
            ListItem(1, 2, "Item 1"),
            ListItem(3, 3, "Item 3")
        )

        `when`(mockApiService.getListItems()).thenReturn(mockData)

        viewModel.fetchItems()
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedState = FetchScreenContract.FetchScreenState(
            uiData = listOf(
                ListItem(2, 1, "Item 2"),
                ListItem(1, 2, "Item 1"),
                ListItem(3, 3, "Item 3")
            )
        )
        assertEquals(FetchScreenContract.UiState.Success(expectedState), viewModel.screenState.value)
    }


    @Test
    fun `test network failure response`() = runTest {
        `when`(mockApiService.getListItems()).thenThrow(RuntimeException("Network failure"))

        viewModel.fetchItems()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(FetchScreenContract.UiState.Error<String>(), viewModel.screenState.value)
    }

    @Test
    fun `test empty response`() = runTest {
        `when`(mockApiService.getListItems()).thenReturn(emptyList())

        viewModel.fetchItems()
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedState = FetchScreenContract.FetchScreenState(uiData = emptyList())
        assertEquals(FetchScreenContract.UiState.Success(expectedState), viewModel.screenState.value)
    }

    @Test
    fun `test data filtering`() = runTest {
        val mockData = listOf(
            ListItem(2, 1, "Item 2"),
            ListItem(1, 2, ""),
            ListItem(3, 3, null)
        )

        `when`(mockApiService.getListItems()).thenReturn(mockData)

        viewModel.fetchItems()
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedState = FetchScreenContract.FetchScreenState(
            uiData = listOf(ListItem(2, 1, "Item 2"))
        )
        assertEquals(FetchScreenContract.UiState.Success(expectedState), viewModel.screenState.value)
    }
}
