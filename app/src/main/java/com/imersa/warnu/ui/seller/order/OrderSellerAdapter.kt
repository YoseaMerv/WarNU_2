package com.imersa.warnu.ui.seller.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.databinding.ItemOrdersBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderSellerAdapter(private var orders: MutableList<Order>) :
    RecyclerView.Adapter<OrderSellerAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(private val binding: ItemOrdersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.tvBuyerName.text = order.customerName ?: "Pembeli"

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvOrderDate.text = order.createdAt?.toDate()?.let { sdf.format(it) } ?: "Tanpa tanggal"

            binding.tvOrderStatus.text = order.paymentStatus?.uppercase(Locale.ROOT) ?: "UNKNOWN"

            // 🔹 Filter item yang memang milik seller ini
            val sellerId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            val sellerItems = order.items?.filter { it.sellerId == sellerId }

            binding.tvOrderItems.text = sellerItems?.joinToString("\n") {
                "• ${it.name} x${it.quantity} - Rp ${it.price}"
            } ?: "Tidak ada item."

            val totalPriceForSeller = sellerItems?.sumOf { (it.price ?: 0.0) * it.quantity }
            binding.tvTotalPrice.text = "Total: Rp ${totalPriceForSeller ?: 0.0}"
        }
    }
}

