package com.imersa.warnu.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeBuyerViewModel : ViewModel() {

    private val _produkList = MutableLiveData<List<ProdukBuyer>>()
    val produkList: LiveData<List<ProdukBuyer>> get() = _produkList

    private val dummyProduk = listOf(
        ProdukBuyer(1, "Kopi Arabica", 15000, "https://via.placeholder.com/150"),
        ProdukBuyer(2, "Kopi Robusta", 12000, "https://via.placeholder.com/150"),
        ProdukBuyer(3, "Kopi Luwak", 55000, "https://via.placeholder.com/150"),
        ProdukBuyer(4, "Kopi Tubruk", 10000, "https://via.placeholder.com/150"),
        ProdukBuyer(5, "Kopi Gayo", 25000, "https://via.placeholder.com/150")
    )

    fun loadProduk() {
        viewModelScope.launch {
            delay(500) // simulasi loading
            _produkList.value = dummyProduk
        }
    }

    fun searchProduk(keyword: String) {
        viewModelScope.launch {
            delay(200)
            _produkList.value = if (keyword.isBlank()) {
                dummyProduk
            } else {
                dummyProduk.filter { it.nama.contains(keyword, ignoreCase = true) }
            }
        }
    }
}

data class ProdukBuyer(
    val id: Int,
    val nama: String,
    val harga: Int,
    val gambarUrl: String
)
