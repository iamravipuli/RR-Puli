package com.example.rrpuli

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    private lateinit var txtName: TextView
    private lateinit var edtAmount: TextInputEditText
    private lateinit var edtDate: TextInputEditText
    private lateinit var txtRoi: TextView
    private lateinit var edtRemarks: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var documentId: String = ""
    private var originalItem: BenfDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // Initialize views
        txtName = findViewById(R.id.txtName)
        edtAmount = findViewById(R.id.edtAmount)
        edtDate = findViewById(R.id.edtDate)
        txtRoi = findViewById(R.id.txtRoi)
        edtRemarks = findViewById(R.id.edtRemarks)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        // Get passed data
        originalItem = intent.getParcelableExtra("item")
        documentId = originalItem?.id ?: ""

        if (originalItem == null || documentId.isEmpty()) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Populate fields with current values
        populateFields(originalItem!!)

        // Date picker
        edtDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val currentDate = edtDate.text.toString()
            if (currentDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val selectedDate = sdf.parse(currentDate)
                    if (selectedDate != null) {
                        cal.time = selectedDate
                    }
                } catch (e: Exception) {
                    // Ignore parse error, use current date
                }
            }

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
            updateTransaction()
        }

        // Cancel button
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun populateFields(item: BenfDetails) {
        txtName.text = item.name
        edtAmount.setText(item.amount.toString())
        edtDate.setText(item.date)
        txtRoi.text = item.iRate
        edtRemarks.setText(item.remarks)
    }

   private fun updateTransaction() {
    val amountStr = edtAmount.text.toString().trim()
    val date = edtDate.text.toString().trim()
    val remarks = edtRemarks.text.toString().trim()

    // Validate modifiable fields
    if (amountStr.isEmpty()) {
        Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show()
        return
    }
    if (date.isEmpty()) {
        Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show()
        return
    }

    val amount = amountStr.toLongOrNull()
    if (amount == null || amount <= 0) {
        Toast.makeText(this, "Amount must be > 0", Toast.LENGTH_SHORT).show()
        return
    }

    // Sign in anonymously (required for Firestore write)
    auth.signInAnonymously().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // ✅ Correct Firestore update syntax: use mapOf with "key" to value
            val updateMap = mapOf(
                "amount" to amount,
                "date" to date,
                "remarks" to remarks
            )

            db.collection("transactions")
                .document(documentId)
                .update(updateMap)
                .addOnSuccessListener {
                    // Show success dialog
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Success!")
                        .setMessage("Transaction updated successfully.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .setCancelable(false)
                        .create()
                    dialog.show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Update failed: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
        }
    }
}
}
