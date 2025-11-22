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

        val transactionType = intent.getStringExtra("type") ?: "credit"
        val title = findViewById<TextView>(R.id.txtTitle)
        title.text = if (transactionType == "debit") "Debit List" else "Credit List"

        // ✅ Fetch and set adapter
        fetchTransactions(transactionType) { list ->
            // ✅ Debug: Print size
            println("DEBUG: Loaded ${list.size} items for type: $transactionType")
            
            if (list.isEmpty()) {
                Toast.makeText(this, "No $transactionType records found", Toast.LENGTH_LONG).show()
            }
            
            // ✅ Create new adapter and set it
            adapter = BenfAdapter(this, list)
            recyclerView.adapter = adapter  // ✅ Set adapter AFTER creation
        }
    }

    private fun fetchTransactions(type: String, onSuccess: (List<BenfDetails>) -> Unit) {
        Thread {
            try {
                db.collection("transactions")
                    .whereEqualTo("type", type.lowercase())
                    .get()
                    .addOnSuccessListener { result ->
                        val list = mutableListOf<BenfDetails>()
                        for (document in result) {
                            val item = BenfDetails(
                                id = document.id,
                                name = document.getString("name") ?: "",
                                amount = document.getLong("amount") ?: 0,
                                date = document.getString("date") ?: "",
                                iRate = document.getString("roi") ?: "0.00",
                                remarks = document.getString("remarks") ?: ""
                            )
                            list.add(item)
                        }
                        
                        runOnUiThread {
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
