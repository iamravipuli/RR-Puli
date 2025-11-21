package com.example.rrpuli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.View
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Credit Reports → passes "credit" type
        findViewById<View>(R.id.tileCredit).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java).putExtra("type", "credit"))
        }

        // Debit Reports → passes "debit" type
        findViewById<View>(R.id.tileDebit).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java).putExtra("type", "debit"))
        }

        // Interest Calculator (PULI-style)
        findViewById<View>(R.id.tileCalculate).setOnClickListener {
            startActivity(Intent(this, CalculateActivity::class.java))
        }

            
            findViewById<View>(R.id.tileNewTransaction).setOnClickListener {
            startActivity(Intent(this, EntryActivity::class.java))
            }
    }
}
