package com.imersa.warnu.ui.buyer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.data.repository.ProductRepository

class HomeBuyerViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _emptyState = MutableLiveData<Boolean>()
    val emptyState: LiveData<Boolean> = _emptyState

    fun loadProducts() {
        _loading.value = true
        repository.listenProducts(onResult = { list ->
            _loading.value = false
            _products.value = list
            _filteredProducts.value = list
            _emptyState.value = list.isEmpty()
        }, onError = {
            _loading.value = false
            _products.value = emptyList()
            _filteredProducts.value = emptyList()
            _emptyState.value = true
        })
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

}
