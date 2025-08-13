package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditManageViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _sellerId = MutableLiveData<String>()

    val products: LiveData<List<Product>> = _sellerId.switchMap { sellerId ->
        repository.getProductsBySeller(sellerId)
    }

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchProducts(sellerId: String) {
        _sellerId.value = sellerId
    }

    fun deleteProduct(productId: String, onResult: (Boolean) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            repository.deleteProduct(productId) { success ->
                _loading.postValue(false)
                if (success) onResult(true)
                else {
                    _errorMessage.postValue("Gagal menghapus produk atau file")
                    onResult(false)
                }
            }
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }
}

