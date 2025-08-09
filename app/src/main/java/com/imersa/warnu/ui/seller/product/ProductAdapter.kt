package com.imersa.warnu.ui.seller.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.imersa.warnu.R

class ProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onEditClick: ((Product) -> Unit)? = null,
    private val onDeleteClick: ((Product) -> Unit)? = null,
    private val layoutResId: Int
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Karena id beda, kita declare semua nullable dulu
        val image: ImageView? = view.findViewById(R.id.ivProductImage)
            ?: view.findViewById(R.id.ivProduk)
        val name: TextView? = view.findViewById(R.id.tvProductName)
            ?: view.findViewById(R.id.tvNamaProduk)
        val harga: TextView? = view.findViewById(R.id.tvProductPrice)
            ?: view.findViewById(R.id.tvHargaStok)
        val stok: TextView? = view.findViewById(R.id.tvStokProduk)
        val btnEdit: ImageButton? = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton? = view.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutResId, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Nama produk
        holder.name?.text = product.name ?: ""

        // Harga dan stok untuk dashboard (pisah)
        if (layoutResId == R.layout.item_product_dashboard) {
            holder.harga?.text = "Rp ${(product.price ?: 0.0).toInt()}"
            holder.stok?.text = "Stok: ${product.stock ?: 0}"
        } else { // untuk edit_product yang gabung harga+stok
            holder.harga?.text = "Rp ${(product.price ?: 0.0).toInt()} - Stok: ${product.stock ?: 0}"
        }

        // Image load
        val imageUrl = product.imageUrl ?: ""
        if (imageUrl.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.image ?: return)
        } else {
            holder.image?.setImageResource(R.drawable.placeholder_image)
        }

        holder.itemView.setOnClickListener { onItemClick(product) }
        holder.btnEdit?.setOnClickListener { onEditClick?.invoke(product) }
        holder.btnDelete?.setOnClickListener { onDeleteClick?.invoke(product) }
    }

    override fun getItemCount() = products.size
}

