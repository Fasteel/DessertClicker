package com.example.dessertclicker.ui

import androidx.compose.runtime.asIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dessertclicker.data.Datasource
import com.example.dessertclicker.model.Dessert
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// QUESTION: Where should it live?
data class ShareIntentData(
    val dessertsSold: Int,
    val revenue: Int
)

class DessertViewModel : ViewModel() {
    // QUESTION: When to put things into the UI state flow vs public state variable?
    private var _revenue = mutableIntStateOf(0)
    val revenue = _revenue.asIntState()

    private var _dessertsSold = mutableIntStateOf(0)
    val dessertsSold = _dessertsSold.asIntState()

    val startShareActivity = MutableSharedFlow<ShareIntentData>()

    private val _uiState = MutableStateFlow(
        DessertUiState(
            currentDessertPrice = Datasource.dessertList[0].price,
            currentDessertImageId = Datasource.dessertList[0].imageId
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onDessertClicked() {
        // Update the revenue
        _revenue.intValue += uiState.value.currentDessertPrice
        _dessertsSold.intValue++

        // Show the next dessert
        val dessertToShow = determineDessertToShow(Datasource.dessertList, dessertsSold.intValue)
        _uiState.update { uiState ->
            uiState.copy(
                currentDessertImageId = dessertToShow.imageId,
                currentDessertPrice = dessertToShow.price
            )
        }
    }

    /**
     * Determine which dessert to show.
     */
    private fun determineDessertToShow(
        desserts: List<Dessert>,
        dessertsSold: Int
    ): Dessert {
        var dessertToShow = desserts.first()
        for (dessert in desserts) {
            if (dessertsSold >= dessert.startProductionAmount) {
                dessertToShow = dessert
            } else {
                // The list of desserts is sorted by startProductionAmount. As you sell more desserts,
                // you'll start producing more expensive desserts as determined by startProductionAmount
                // We know to break as soon as we see a dessert who's "startProductionAmount" is greater
                // than the amount sold.
                break
            }
        }

        return dessertToShow
    }

    fun onShareButtonClick() {
        viewModelScope.launch {
            startShareActivity.emit(
                ShareIntentData(
                    dessertsSold = dessertsSold.intValue,
                    revenue = revenue.intValue
                )
            )
        }
    }

}
