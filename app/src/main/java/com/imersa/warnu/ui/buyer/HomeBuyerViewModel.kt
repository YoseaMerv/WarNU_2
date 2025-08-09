package com.imersa.warnu.ui.buyer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeBuyerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _produkList = MutableLiveData<List<ProdukBuyer>>()
    val produkList: LiveData<List<ProdukBuyer>> get() = _produkList

    fun loadProduk() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val produkList = result.documents.mapNotNull { doc ->
                    doc.toObject(ProdukBuyer::class.java)?.copy(id = doc.id)
                }
                _produkList.value = produkList
            }
            .addOnFailureListener { e ->
                Log.e("HomeBuyerViewModel", "Gagal memuat produk", e)
                _produkList.value = emptyList()
            }
    }

    fun searchProduk(keyword: String) {
        if (keyword.isBlank()) {
            loadProduk()
            return
        }

        firestore.collection("products")
            .orderBy("name") // pastikan field ini di-index
            .startAt(keyword)
            .endAt(keyword + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                val produkList = result.documents.mapNotNull { doc ->
                    doc.toObject(ProdukBuyer::class.java)?.copy(id = doc.id)
                }
                _produkList.value = produkList
            }
            .addOnFailureListener { e ->
                Log.e("HomeBuyerViewModel", "Gagal mencari produk", e)
                _produkList.value = emptyList()
            }
    }
}
