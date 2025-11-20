package com.example.rrpuli

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.example.rrpuli.databinding.ActivityCalculateBinding
import java.text.SimpleDateFormat
import java.util.*

class CalculateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculateBinding
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInputFilters()
        setupDatePickers()

        binding.btnCalculate.setOnClickListener {
            validateAndCalculate()
        }
    }

    private fun setupInputFilters() {
        // Amount: max 7 digits
        binding.edtAmount.filters = arrayOf(android.text.InputFilter.LengthFilter(7))

        // Rate: custom decimal filter (max 2 before, 2 after decimal)
        binding.edtRate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString() ?: ""
                if (input.isEmpty() || input == ".") return

                val parts = input.split(".")
                val before = if (parts.isNotEmpty()) parts[0] else ""
                val after = if (parts.size > 1) parts[1] else ""

                var corrected = input

                // Enforce max 2 digits before decimal
                if (before.length > 2) {
                    corrected = before.take(2)
                    if (after.isNotEmpty()) corrected += "." + after.take(2)
                }
                // Enforce max 2 digits after decimal
                else if (after.length > 2) {
                    corrected = before + "." + after.take(2)
                }

                // Prevent leading zeros (e.g., "00.5" -> "0.5")
                if (before.length > 1 && before.startsWith("0")) {
                    corrected = before.drop(1)
                    if (after.isNotEmpty()) corrected += "." + after.take(2)
                }

                if (corrected != input) {
                    binding.edtRate.removeTextChangedListener(this)
                    binding.edtRate.setText(corrected)
                    binding.edtRate.setSelection(corrected.length)
                    binding.edtRate.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupDatePickers() {
        binding.edtDate1.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    binding.edtDate1.setText(dateFormat.format(selected.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.edtDate2.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    binding.edtDate2.setText(dateFormat.format(selected.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun validateAndCalculate() {
        val amountStr = binding.edtAmount.text.toString().trim()
        val rateStr = binding.edtRate.text.toString().trim()
        val date1Str = binding.edtDate1.text.toString().trim()
        val date2Str = binding.edtDate2.text.toString().trim()

        if (amountStr.isEmpty()) {
            binding.txtResult.text = "⚠️ Enter Principal Amount"
            return
        }
        if (rateStr.isEmpty()) {
            binding.txtResult.text = "⚠️ Enter Rate"
            return
        }
        if (date1Str.isEmpty()) {
            binding.txtResult.text = "⚠️ Select Start Date"
            return
        }
        if (date2Str.isEmpty()) {
            binding.txtResult.text = "⚠️ Select End Date"
            return
        }

        hideKeyboard()

        val amount = amountStr.toDoubleOrNull()
        val roi = rateStr.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            binding.txtResult.text = "⚠️ Amount must be > 0"
            return
        }
        if (roi == null || roi < 0) {
            binding.txtResult.text = "⚠️ Rate cannot be negative"
            return
        }

        val date1 = try { dateFormat.parse(date1Str) } catch (e: Exception) { null }
        val date2 = try { dateFormat.parse(date2Str) } catch (e: Exception) { null }

        if (date1 == null || date2 == null) {
            binding.txtResult.text = "⚠️ Invalid date format"
            return
        }

        if (date2.before(date1)) {
            binding.txtResult.text = "⚠️ End date before start"
            return
        }

        // ✅ INCLUSIVE DURATION: Add 1 day so both start and end are counted
        val daysBetween = ((date2.time - date1.time) / (24 * 60 * 60 * 1000)).toInt() + 1

        val interest = (amount * roi * daysBetween) / (100 * 30)
        val total = amount + interest

        // Format with Indian comma style
        binding.txtPrincipal.text = "₹${formatIndianNumber(amount)}"
        binding.txtInterest.text = "₹${formatIndianNumber(interest)}"
        binding.txtTotal.text = "₹${formatIndianNumber(total)}"

        // Duration: approximate
        val years = daysBetween / 365
        val rem = (daysBetween % 365).toInt()
        val months = rem / 30
        val days = rem % 30
        binding.txtDuration.text = "${years}y ${months}m ${days}d"

        binding.txtResult.text = "✓ Calculated for $daysBetween day(s)"
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.btnCalculate.windowToken, 0)
    }

    private fun formatIndianNumber(value: Double): String {
        return try {
            val formatter = android.icu.text.DecimalFormat("#,##,##0")
            formatter.format(value.toLong())
        } catch (e: Throwable) {
            String.format("%.0f", value)
        }
    }
}
