package com.example.rrpuli

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EntryActivity : AppCompatActivity() {

    private lateinit var edtName: TextInputEditText
    private lateinit var edtAmount: TextInputEditText
    private lateinit var edtDate: TextInputEditText
    private lateinit var edtRoi: TextInputEditText
    private lateinit var edtRemarks: TextInputEditText
    private lateinit var radioType: RadioGroup
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        // Initialize views
        edtName = findViewById(R.id.edtName)
        edtAmount = findViewById(R.id.edtAmount)
        edtDate = findViewById(R.id.edtDate)
        edtRoi = findViewById(R.id.edtRoi)
        edtRemarks = findViewById(R.id.edtRemarks)
        radioType = findViewById(R.id.radioType)
        btnSave = findViewById(R.id.btnSave)

        // Date picker
        edtDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    edtDate.setText(sdf.format(selected.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save button
        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val name = edtName.text.toString().trim()
        val amountStr = edtAmount.text.toString().trim()
        val date = edtDate.text.toString().trim()
        val roiStr = edtRoi.text.toString().trim()
        val remarks = edtRemarks.text.toString().trim()

        // Validate mandatory fields
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
            return
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show()
            return
        }
        if (roiStr.isEmpty()) {
            Toast.makeText(this, "Enter ROI", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toLongOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Amount must be > 0", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Validate radio button selection
        if (radioType.checkedRadioButtonId == -1) {
            // No radio button selected
            Toast.makeText(this, "Please select Credit or Debit", Toast.LENGTH_SHORT).show()
            return
        }

        // Determine type from selection
        val type = if (radioType.checkedRadioButtonId == R.id.radioCredit) "credit" else "debit"

        // Sign in anonymously and save
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val transaction = Transaction(
                    name = name,
                    amount = amount,
                    date = date,
                    roi = roiStr,
                    remarks = remarks,
                    type = type
                )

                db.collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener {
                        // Show success dialog
                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Success!")
                            .setMessage("Transaction saved successfully.")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }
                            .setCancelable(false)
                            .create()
                        dialog.show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Save failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
