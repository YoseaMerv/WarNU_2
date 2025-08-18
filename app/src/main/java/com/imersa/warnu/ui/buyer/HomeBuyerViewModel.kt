package com.imersa.warnu.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeBuyerViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _products = MutableLiveData<List<ProductBuyer>>()
    val products: LiveData<List<ProductBuyer>> = _products

    private val _filteredProducts = MutableLiveData<List<ProductBuyer>>()
    val filteredProducts: LiveData<List<ProductBuyer>> = _filteredProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _emptyState = MutableLiveData<Boolean>()
    val emptyState: LiveData<Boolean> = _emptyState

    fun loadProducts() {
        _loading.value = true
        repository.listenProducts(
            onResult = { list ->
                _loading.value = false
                _products.value = list
                _filteredProducts.value = list
                _emptyState.value = list.isEmpty()
            },
            onError = {
                _loading.value = false
                _products.value = emptyList()
                _filteredProducts.value = emptyList()
                _emptyState.value = true
            }
        )
    }

    fun searchProducts(query: String) {
        val currentList = _products.value ?: return
        _filteredProducts.value = if (query.isBlank()) {
            currentList
        } else {
            currentList.filter {
                it.name?.contains(query, ignoreCase = true) == true
            }
        }
    }

    fun addToCart(
        product: ProductBuyer,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addToCart(product)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
