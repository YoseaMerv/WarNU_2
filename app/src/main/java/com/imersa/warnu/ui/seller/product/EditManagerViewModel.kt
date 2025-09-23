package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.data.repository.ProductSellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditManageViewModel @Inject constructor(
    private val repository: ProductSellerRepository
) : ViewModel() {

    private val _sellerId = MutableLiveData<String>()

    val products: LiveData<List<Product>> = _sellerId.switchMap { sellerId ->
        repository.getProductsBySeller(sellerId)
    }

    private val _loading = MutableLiveData(false)
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
}

