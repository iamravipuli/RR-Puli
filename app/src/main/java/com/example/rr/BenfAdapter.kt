package com.example.rrpuli

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BenfAdapter(
    private val context: Context,
    private val items: List<BenfDetails>
) : RecyclerView.Adapter<BenfAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtAmountDate: TextView = view.findViewById(R.id.txtAmountDate)
        val txtInterest: TextView = view.findViewById(R.id.txtInterest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_benf, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Line 1: Name (Blue)
        holder.txtName.text = item.name

        // Line 2: Amount (Green) | Date (Black) - Plain text
        val formattedAmount = try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            "₹${formatter.format(item.amount)}"
        } catch (e: Throwable) {
            "₹${item.amount}"
        }
        holder.txtAmountDate.text = "$formattedAmount | ${item.date}"

        // Line 3: Interest (Red) with Indian comma formatting
        val interestText = try {
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
            val startDate = sdf.parse(item.date) ?: throw Exception("Invalid date")
            val today = java.util.Date()

            if (today.before(startDate)) {
                "Interest: ₹0 (future date)"
            } else {
                val diffMillis = today.time - startDate.time
                val days = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1 // inclusive

                val roi = item.iRate.toDoubleOrNull() ?: 0.0
                val interest = (item.amount.toDouble() * roi * days) / (100.0 * 30.0)

                if (interest.isFinite()) {
                    val interestRounded = Math.round(interest).toLong()
                    val formattedInterest = try {
                        val formatter = android.icu.text.DecimalFormat("#,##,##0")
                        formatter.format(interestRounded)
                    } catch (e: Throwable) {
                        interestRounded.toString()
                    }
                    "Interest: ₹$formattedInterest"
                } else {
                    "Interest: ₹0"
                }
            }
        } catch (e: Exception) {
            "Interest: Error"
        }

        holder.txtInterest.text = interestText

        // Navigate to detail screen
        holder.itemView.setOnClickListener {
            val intent = Intent(context, BenfDetailActivity::class.java).apply {
                putExtra("item", item)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}
