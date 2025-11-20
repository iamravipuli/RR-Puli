package com.example.rrpuli

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class CalculateActivity : AppCompatActivity() {

    private lateinit var edtAmount: EditText
    private lateinit var edtRate: EditText
    private lateinit var edtDate1: EditText
    private lateinit var edtDate2: EditText
    private lateinit var txtPrincipal: TextView
    private lateinit var txtInterest: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtDuration: TextView
    private lateinit var txtResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)

        // Initialize views
        edtAmount = findViewById(R.id.edtAmount)
        edtRate = findViewById(R.id.edtRate)
        edtDate1 = findViewById(R.id.edtDate1)
        edtDate2 = findViewById(R.id.edtDate2)
        txtPrincipal = findViewById(R.id.txtPrincipal)
        txtInterest = findViewById(R.id.txtInterest)
        txtTotal = findViewById(R.id.txtTotal)
        txtDuration = findViewById(R.id.txtDuration)
        txtResult = findViewById(R.id.txtResult)

        // Date pickers
        edtDate1.setOnClickListener { showDatePicker { date -> 
            edtDate1.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)) 
        }}
        edtDate2.setOnClickListener { showDatePicker { date -> 
            edtDate2.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)) 
        }}

        // ROI input filter (max 2 before decimal, 2 after)
        edtRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isEmpty() || input == ".") return

                val parts = input.split(".")
                val before = parts[0]
                val after = if (parts.size > 1) parts[1] else ""

                var corrected = input

                // Max 2 digits before decimal
                if (before.length > 2) {
                    corrected = before.take(2)
                    if (after.isNotEmpty()) corrected += "." + after.take(2)
                }
                // Max 2 digits after decimal
                else if (after.length > 2) {
                    corrected = before + "." + after.take(2)
                }

                // Prevent leading zeros
                if (before.length > 1 && before.startsWith("0")) {
                    corrected = before.drop(1)
                    if (after.isNotEmpty()) corrected += "." + after.take(2)
                }

                if (corrected != input) {
                    edtRate.removeTextChangedListener(this)
                    edtRate.setText(corrected)
                    edtRate.setSelection(corrected.length)
                    edtRate.addTextChangedListener(this)
                }
            }
        })

        // Calculate button
        findViewById<Button>(R.id.btnCalculate).setOnClickListener {
            calculateInterest()
        }
    }

    private fun calculateInterest() {
        val amountStr = edtAmount.text.toString().trim()
        val rateStr = edtRate.text.toString().trim()

        if (amountStr.isEmpty() || rateStr.isEmpty()) {
            txtResult.text = "⚠️ Enter amount and rate"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        val roi = rateStr.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            txtResult.text = "⚠️ Amount must be > 0"
            return
        }
        if (roi == null || roi < 0) {
            txtResult.text = "⚠️ Rate cannot be negative"
            return
        }

        val date1Str = edtDate1.text.toString()
        val date2Str = edtDate2.text.toString()

        if (date1Str.isEmpty() || date2Str.isEmpty()) {
            txtResult.text = "⚠️ Select both dates"
            return
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val date1 = try { dateFormat.parse(date1Str) } catch (e: Exception) { null }
        val date2 = try { dateFormat.parse(date2Str) } catch (e: Exception) { null }

        if (date1 == null || date2 == null) {
            txtResult.text = "⚠️ Invalid date"
            return
        }

        if (date2.before(date1)) {
            txtResult.text = "⚠️ End date before start"
            return
        }

        // ✅ INCLUSIVE: +1 day
        val daysBetween = (date2.time - date1.time) / (24 * 60 * 60 * 1000) + 1

        val interest = (amount * roi * daysBetween) / (100 * 30)
        val total = amount + interest

        // Format with Indian comma style
        txtPrincipal.text = formatIndianNumber(amount)
        txtInterest.text = formatIndianNumber(interest)
        txtTotal.text = formatIndianNumber(total)

        // Duration: approximate
        val years = daysBetween / 365
        val rem = (daysBetween % 365).toInt()
        val months = rem / 30
        val days = rem % 30
        txtDuration.text = "${years}y ${months}m ${days}d"

        txtResult.text = "✓ Calculated for $daysBetween day(s)"
    }

    private fun showDatePicker(onSelected: (Date) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                val selected = Calendar.getInstance().apply {
                    set(y, m, d)
                }
                onSelected(selected.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatIndianNumber(value: Double): String {
        return try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0.00")
            formatter.format(value)
        } catch (e: Throwable) {
            String.format("%.2f", value)
        }
    }
}
