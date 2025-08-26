package com.imersa.warnu.ui.seller.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.data.repository.ProductSellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeSellerViewModel @Inject constructor(
    private val repository: ProductSellerRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchProducts(sellerId: String) {
        _isLoading.value = true
        repository.getProductsBySeller(sellerId).observeForever { list ->
            _products.value = list
            _isLoading.value = false
        }
    }
}
