package com.example.rrpuli

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BenfDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benf_detail)

        val item = intent.getParcelableExtra<BenfDetails>("item") ?: run {
            finish()
            return
        }

        findViewById<TextView>(R.id.txtName).text = item.name
        findViewById<TextView>(R.id.txtAmount).text = "Amount: ₹${formatIndianNumber(item.amount.toDouble())}"
        findViewById<TextView>(R.id.txtDate).text = "Date: ${item.date}"
        findViewById<TextView>(R.id.txtIRate).text = "Interest Rate: ${item.iRate}%"
        findViewById<TextView>(R.id.txtRemarks).text = "Remarks: ${item.remarks}"
    }

    private fun formatIndianNumber(value: Double): String {
        return try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            formatter.format(value.toLong())
        } catch (e: Throwable) {
            value.toLong().toString()
        }
    }
}
