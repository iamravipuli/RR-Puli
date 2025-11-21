package com.example.rrpuli

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

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

        // Line 2: Amount (Green) | Date (Black)
        val formattedAmount = try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            "₹${formatter.format(item.amount)}"
        } catch (e: Throwable) {
            "₹${item.amount}"
        }
        val amountDateText = "$formattedAmount | ${item.date}"
        val spannable = SpannableString(amountDateText)

        // Color amount (green)
        spannable.setSpan(
            ForegroundColorSpan(0xFF34A853.toInt()),
            0,
            formattedAmount.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Color date (black)
        spannable.setSpan(
            ForegroundColorSpan(0xFF202124.toInt()),
            formattedAmount.length + 3, // Skip " | "
            amountDateText.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        holder.txtAmountDate.text = spannable

        // Line 3: Interest (Red, calculated from date to today)
        val interestText = try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val startDate = sdf.parse(item.date) ?: throw Exception("Invalid date")
            val today = Date()

            if (today.before(startDate)) {
                "Interest: ₹0 (future date)"
            } else {
                val diffMillis = today.time - startDate.time
                val days = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1 // inclusive

                val roi = item.iRate.toDoubleOrNull() ?: 0.0
                val interest = (item.amount.toDouble() * roi * days) / (100 * 30)
                val interestRounded = Math.round(interest).toLong()
                val formattedInterest = try {
                    val formatter = android.icu.text.DecimalFormat("#,##,##0")
                    formatter.format(interestRounded)
                } catch (e: Throwable) {
                    interestRounded.toString()
                }
                "Interest: ₹$formattedInterest"
            }
        } catch (e: Exception) {
            "Interest: Error"
        }

        holder.txtInterest.text = interestText

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BenfDetailActivity::class.java).apply {
                putExtra("item", item)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}
