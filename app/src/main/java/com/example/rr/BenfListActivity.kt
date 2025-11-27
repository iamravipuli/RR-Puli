package com.example.rrpuli

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BenfListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BenfAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benf_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val transactionType = intent.getStringExtra("type") ?: "credit"
        val title = findViewById<TextView>(R.id.txtTitle)
        title.text = if (transactionType == "debit") "Debit List" else "Credit List"

        fetchTransactions(transactionType) { list ->
            adapter = BenfAdapter(this, list)
            recyclerView.adapter = adapter
        }
    }

 private fun fetchTransactions(type: String, onSuccess: (List<BenfDetails>) -> Unit) {
    if (auth.currentUser == null) {
        auth.signInAnonymously()
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    performFirestoreQuery(type, onSuccess)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Auth failed: ${signInTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    } else {
        performFirestoreQuery(type, onSuccess)
    }
}

// ✅ Updated helper method with sorting
private fun performFirestoreQuery(type: String, onSuccess: (List<BenfDetails>) -> Unit) {
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

                    // ✅ Sort alphabetically by name (case-insensitive)
                    val sortedList = list.sortedBy { it.name.lowercase() }

                    runOnUiThread {
                        onSuccess(sortedList)
                    }
                }
                .addOnFailureListener { exception ->
                    runOnUiThread {
                        Toast.makeText(this@BenfListActivity, "Load failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@BenfListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }.start()
}

 
}
