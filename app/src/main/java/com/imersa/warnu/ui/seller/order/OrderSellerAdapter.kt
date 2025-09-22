package com.imersa.warnu.ui.seller.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.databinding.ItemOrdersBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderSellerAdapter : ListAdapter<Order, OrderSellerAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class OrderViewHolder(private val binding: ItemOrdersBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(order.totalAmount)
            val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))

            binding.tvOrderId.text = "Order #${order.orderId?.take(8)}" // Ambil 8 karakter pertama
            binding.tvCustomerName.text = order.customerName ?: "Nama Pelanggan Tidak Tersedia"
            binding.tvOrderTotal.text = formattedPrice
            binding.tvOrderStatus.text = order.paymentStatus?.replaceFirstChar { it.uppercase() }
            binding.tvOrderDate.text = order.createdAt?.toDate()?.let { sdf.format(it) } ?: "Tanggal tidak tersedia"

            // Logika untuk mengubah warna status (opsional tapi bagus)
            // Anda perlu membuat drawable (misal: status_pending_background.xml)
            // binding.tvOrderStatus.setBackgroundResource(
            //     when(order.paymentStatus) {
            //         "pending" -> R.drawable.status_pending_background
            //         "settlement" -> R.drawable.status_success_background
            //         else -> R.drawable.status_failed_background
            //     }
            // )
        }
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}