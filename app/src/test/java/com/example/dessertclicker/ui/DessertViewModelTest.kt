package com.example.dessertclicker.ui

import com.example.dessertclicker.data.Datasource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DessertViewModelTest {
    private val viewModel = DessertViewModel()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset Main dispatcher to its original state
    }

    @Test
    fun dessertViewModel_Click_RevenueAndDessertsSoldUpdated() {
        val currentRevenue = viewModel.revenue.intValue
        val currentPrice = viewModel.uiState.value.currentDessertPrice
        val currentDessertsSold = viewModel.dessertsSold.intValue

        viewModel.onDessertClicked()

        assertEquals(currentRevenue + currentPrice, viewModel.revenue.intValue)
        assertEquals(currentDessertsSold + 1, viewModel.dessertsSold.intValue)
    }

    @Test
    fun dessertViewModel_MultipleClicksExceedProductionAmount_ImageUpdated() {
        // we repeat just before switching the production
        repeat(Datasource.dessertList[1].startProductionAmount - 1) {
            viewModel.onDessertClicked()
        }

        // we make sure we produce the first dessert for the last time
        assertEquals(Datasource.dessertList[0].price, viewModel.uiState.value.currentDessertPrice)
        assertEquals(
            Datasource.dessertList[0].imageId,
            viewModel.uiState.value.currentDessertImageId
        )

        viewModel.onDessertClicked()

        // and then we make sure that we are producing the new dessert
        assertEquals(Datasource.dessertList[1].price, viewModel.uiState.value.currentDessertPrice)
        assertEquals(
            Datasource.dessertList[1].imageId,
            viewModel.uiState.value.currentDessertImageId
        )
    }

    @Test
    fun dessertViewModel_ShareButtonClick_SendNewEmissionWithShareIntentDataDefault() = runTest {
        val collectedValues = mutableListOf<ShareIntentData>()

        val job = launch(Dispatchers.Main) {
            viewModel.startShareActivity.collect {
                collectedValues.add(it)
            }
        }

        viewModel.onShareButtonClick()

        testScheduler.advanceTimeBy(1000) // Is there a better way to do it?

        job.cancel()

        assertEquals(ShareIntentData(dessertsSold = 0, revenue = 0), collectedValues.first())
    }

    @Test
    fun dessertViewModel_ShareButtonClick_SendNewEmissionWithShareIntentData() = runTest {
        val collectedValues = mutableListOf<ShareIntentData>()

        val job = launch(Dispatchers.Main) {
            viewModel.startShareActivity.collect {
                collectedValues.add(it)
            }
        }

        viewModel.onDessertClicked()
        viewModel.onShareButtonClick()

        testScheduler.advanceTimeBy(1000) // Is there a better way to do it?

        job.cancel()

        assertEquals(ShareIntentData(dessertsSold = 1, revenue = 5), collectedValues.last())
    }
}