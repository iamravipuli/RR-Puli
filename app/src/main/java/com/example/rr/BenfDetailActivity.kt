package com.example.rrpuli

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BenfDetailActivity : AppCompatActivity() {

    private lateinit var edtProjectedDate: EditText
    private lateinit var btnCalculateProjected: Button
    private lateinit var txtProjectedInterest: TextView
    private lateinit var txtTotalProjected: TextView
    private lateinit var txtProjectedDuration: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benf_detail)

        val item = intent.getParcelableExtra<BenfDetails>("item")!!

        // Header: Name (Blue)
        findViewById<TextView>(R.id.txtName).apply {
            text = item.name
            setTextColor(0xFF1A73E8.toInt()) // Blue
        }

        // Amount: Green
        val formattedAmount = try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            "₹${formatter.format(item.amount)}"
        } catch (e: Throwable) {
            "₹${item.amount}"
        }
        findViewById<TextView>(R.id.txtAmount).apply {
            text = formattedAmount
            setTextColor(0xFF34A853.toInt()) // Green
        }

        // Date: Black
        findViewById<TextView>(R.id.txtDate).apply {
            text = item.date
            setTextColor(0xFF202124.toInt()) // Black/dark gray
        }

        // Interest Rate: Red
        findViewById<TextView>(R.id.txtIRate).apply {
            text = "${item.iRate}%"
            setTextColor(0xFFEA4335.toInt()) // Red
        }

        // Today's date for interest header
        val today = java.util.Date()
        val todayFormatted = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(today)
        findViewById<TextView>(R.id.txtInterestHeader).apply {
            text = "INTEREST TO DATE (as of $todayFormatted)"
            setTextColor(0xFF34A853.toInt()) // Green header
        }

        // Calculate interest & duration
        val (interestValue, durationText) = calculateInterestAndDuration(item, today)

        // Accrued Interest: Red
        val interestFormatted = try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            "₹${formatter.format(interestValue)}"
        } catch (e: Throwable) {
            "₹$interestValue"
        }
        findViewById<TextView>(R.id.txtCalculatedInterest).apply {
            text = interestFormatted
            setTextColor(0xFFEA4335.toInt()) // Red
        }

        // Total Amount: Blue
        val totalValue = item.amount + interestValue
        val totalFormatted = try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            "₹${formatter.format(totalValue)}"
        } catch (e: Throwable) {
            "₹$totalValue"
        }
        findViewById<TextView>(R.id.txtTotalAmount).apply {
            text = totalFormatted
            setTextColor(0xFF1A73E8.toInt()) // Blue
        }

        // Duration: Gray
        findViewById<TextView>(R.id.txtDuration).apply {
            text = durationText
            setTextColor(0xFF5F6368.toInt()) // Gray
        }

        // Remarks: Gray
        findViewById<TextView>(R.id.txtRemarks).apply {
            text = item.remarks.ifEmpty { "No remarks" }
            setTextColor(0xFF5F6368.toInt()) // Gray
        }

        // Initialize projected views
        edtProjectedDate = findViewById(R.id.edtProjectedDate)
        btnCalculateProjected = findViewById(R.id.btnCalculateProjected)
        txtProjectedInterest = findViewById(R.id.txtProjectedInterest)
        txtTotalProjected = findViewById(R.id.txtTotalProjected)
        txtProjectedDuration = findViewById(R.id.txtProjectedDuration)

        // Date picker for projected date
        edtProjectedDate.setOnClickListener {
            showDatePicker { selectedDate ->
                val formatted = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US).format(selectedDate)
                edtProjectedDate.setText(formatted)
            }
        }

        // Calculate projected interest
        btnCalculateProjected.setOnClickListener {
            val projectedDateStr = edtProjectedDate.text.toString()
            if (projectedDateStr.isEmpty()) {
                Toast.makeText(this, "Select a future date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                val creditDate = sdf.parse(item.date) ?: throw Exception("Invalid date")
                val projectedDate = sdf.parse(projectedDateStr) ?: throw Exception("Invalid projected date")

                if (projectedDate.before(creditDate)) {
                    Toast.makeText(this, "Future date must be after start date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val days = ((projectedDate.time - creditDate.time) / (24 * 60 * 60 * 1000)).toInt() + 1
                val roi = item.iRate.toDoubleOrNull() ?: 0.0
                val interest = (item.amount.toDouble() * roi * days) / (100.0 * 30.0)
                val interestRounded = if (interest.isFinite()) Math.round(interest).toLong() else 0L

                val interestFormatted = try {
                    val formatter = android.icu.text.DecimalFormat("#,##,##0")
                    formatter.format(interestRounded)
                } catch (e: Throwable) {
                    interestRounded.toString()
                }

                val totalFormatted = try {
                    val formatter = android.icu.text.DecimalFormat("#,##,##0")
                    formatter.format(item.amount + interestRounded)
                } catch (e: Throwable) {
                    (item.amount + interestRounded).toString()
                }

                val years = days / 365
                val rem = days % 365
                val months = rem / 30
                val d = rem % 30
                val durationText = "$years years, $months months, $d days"

                // Update UI
                txtProjectedInterest.text = "₹$interestFormatted"
                txtTotalProjected.text = "₹$totalFormatted"
                txtProjectedDuration.text = durationText

                // Clarity note
                val note = "Interest calculated from ${item.date} to $projectedDateStr"
                findViewById<TextView>(R.id.txtProjectionNote).text = note

            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateInterestAndDuration(
        item: BenfDetails,
        today: java.util.Date
    ): Pair<Long, String> {
        return try {
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
            val startDate = sdf.parse(item.date) ?: throw Exception("Invalid date")

            if (today.before(startDate)) {
                return Pair(0L, "0 years, 0 months, 0 days")
            }

            val diffMillis = today.time - startDate.time
            val totalDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt() + 1

            val years = totalDays / 365
            val remainingAfterYears = totalDays % 365
            val months = remainingAfterYears / 30
            val days = remainingAfterYears % 30

            val durationText = "$years years, $months months, $days days"

            val roi = item.iRate.toDoubleOrNull() ?: 0.0
            val interest = (item.amount.toDouble() * roi * totalDays) / (100.0 * 30.0)
            val interestRounded = if (interest.isFinite()) Math.round(interest).toLong() else 0L

            Pair(interestRounded, durationText)
        } catch (e: Exception) {
            Pair(0L, "Error")
        }
    }

    private fun showDatePicker(onSelected: (java.util.Date) -> Unit) {
        val cal = java.util.Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = java.util.Calendar.getInstance().apply {
                    set(year, month, day)
                }
                onSelected(selected.time)
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }
}
