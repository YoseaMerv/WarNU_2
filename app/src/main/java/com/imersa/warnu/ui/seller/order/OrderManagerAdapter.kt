package com.imersa.warnu.ui.seller.order

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
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

// 1. Tambahkan listener pada constructor adapter
class OrderManagerAdapter(
    private val onItemClick: (Order) -> Unit
) : ListAdapter<Order, OrderManagerAdapter.OrderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orders, parent, false)
        // 2. Kirim listener ke ViewHolder
        return OrderViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 3. Tambahkan listener pada constructor ViewHolder
    class OrderViewHolder(itemView: View, private val onItemClick: (Order) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvTotalAmount: TextView = itemView.findViewById(R.id.tv_total_amount)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            tvCustomerName.text = order.customerName
            tvOrderId.text = order.orderId

            val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
            formatter.maximumFractionDigits = 0
            formatter.minimumFractionDigits = 0

            tvTotalAmount.text = formatter.format(order.totalAmount ?: 0.0)

            tvStatus.text = order.paymentStatus?.replaceFirstChar { it.uppercase() }
            val statusBackground = when (order.paymentStatus) {
                "settlement" -> R.drawable.status_settlement_background
                "pending" -> R.drawable.status_pending_background
                else -> R.drawable.status_cancelled_background
            }
            tvStatus.background = ContextCompat.getDrawable(itemView.context, statusBackground)

            // 4. Set OnClickListener pada item view
            itemView.setOnClickListener {
                onItemClick(order)
            }
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
