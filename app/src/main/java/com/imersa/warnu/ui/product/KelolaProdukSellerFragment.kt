package com.imersa.warnu.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.imersa.warnu.R

class KelolaProdukSellerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout kelola produk
        return inflater.inflate(R.layout.fragment_product_seller, container, false)
    }
}
