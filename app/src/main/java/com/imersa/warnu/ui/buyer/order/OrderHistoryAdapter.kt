package com.imersa.warnu.ui.buyer.order

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.text.DecimalFormat

class OrderHistoryAdapter : ListAdapter<Order, OrderHistoryAdapter.OrderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tv_total_amount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            // Tampilkan Order ID
            tvOrderId.text = "Order ID: ${order.orderId}"

            // Format tanggal
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            tvDate.text = order.createdAt?.toDate()?.let { sdf.format(it) } ?: "No date"

            // Format total harga
            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0
            tvTotalAmount.text = formatter.format(order.totalAmount ?: 0.0)

            // Status ambil dari orderStatus biar konsisten
            val status = order.orderStatus ?: "Pending"
            tvStatus.text = status

            // Warna background status (samakan dengan seller)
            val statusBackground = when (status) {
                "Pending" -> R.drawable.status_pending_background
                "Processing" -> R.drawable.status_processing_background
                "Shipped" -> R.drawable.status_shipped_background
                "Completed" -> R.drawable.status_completed_background
                "Cancelled" -> R.drawable.status_cancelled_background
                else -> R.drawable.status_pending_background
            }
            tvStatus.background = ContextCompat.getDrawable(itemView.context, statusBackground)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}