package com.example.rrpuli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Credit Reports → passes "credit" type
        findViewById<Button>(R.id.btnCredit).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java).putExtra("type", "credit"))
        }

        // Debit Reports → passes "debit" type
        findViewById<Button>(R.id.btnDebit).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java).putExtra("type", "debit"))
        }

        // Interest Calculator (PULI-style)
        findViewById<Button>(R.id.btnCalculate).setOnClickListener {
            startActivity(Intent(this, CalculateActivity::class.java))
        }

        // New Transaction (Firestore entry)
        findViewById<Button>(R.id.btnNewTransaction).setOnClickListener {
            startActivity(Intent(this, EntryActivity::class.java))
        }
    }
}
