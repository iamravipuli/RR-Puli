package com.example.rrpuli

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BenfListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BenfAdapter
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benf_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get transaction type from intent
        val transactionType = intent.getStringExtra("type") ?: "credit"

        // ✅ Update title based on type
        val title = findViewById<TextView>(R.id.txtTitle)
        title.text = if (transactionType == "debit") "Debit List" else "Credit List"

        // Fetch data from Firestore
        fetchTransactions(transactionType) { list ->
            adapter = BenfAdapter(this, list)
            recyclerView.adapter = adapter
        }
    }

  private fun fetchTransactions(type: String, onSuccess: (List<BenfDetails>) -> Unit) {
    Thread {
        try {
            db.collection("transactions")
                .whereEqualTo("type", type.lowercase())  // Force lowercase match
                .get()
                .addOnSuccessListener { result ->
                    println("Firestore: Found ${result.size()} documents for type: $type")  // ✅ Debug log
                    
                    val list = mutableListOf<BenfDetails>()
                    for (document in result) {
                        println("Firestore: Processing doc ID: ${document.id}")  // ✅ Debug log
                        
                        val item = BenfDetails(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            amount = document.getLong("amount") ?: 0,
                            date = document.getString("date") ?: "",
                            iRate = document.getString("roi") ?: "0.00",
                            remarks = document.getString("remarks") ?: ""
                        )
                        list.add(item)
                        println("Firestore: Added item: ${item.name}")  // ✅ Debug log
                    }
                    
                    runOnUiThread {
                        if (list.isEmpty()) {
                            Toast.makeText(this, "No $type records found", Toast.LENGTH_LONG).show()
                        }
                        onSuccess(list)
                    }
                }
                .addOnFailureListener { exception ->
                    runOnUiThread {
                        Toast.makeText(this, "Load failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
}
}
