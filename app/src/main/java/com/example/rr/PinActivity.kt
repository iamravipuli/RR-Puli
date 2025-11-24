package com.example.rrpuli

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PinActivity : AppCompatActivity() {

    private lateinit var edtPin: EditText
    private var dynamicPin: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        edtPin = findViewById(R.id.edtPin)

        
        auth.signInAnonymously()
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    // Now fetch the PIN from Firestore
                    fetchPinFromFirestore()
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Auth failed: ${signInTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

        edtPin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pin = s?.toString() ?: ""
                if (pin.length == 4) {
                    if (pin == dynamicPin) {
                        startActivity(Intent(this@PinActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@PinActivity, "Invalid PIN", Toast.LENGTH_SHORT).show()
                        edtPin.setText("")
                    }
                }
            }
        })
    }

    private fun fetchPinFromFirestore() {
        db.collection("app_config")
            .document(" 8pIPAtsGEjNRc3w3XS61 ")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val p = document.getString("p") ?: "0"
                    val u = document.getString("u") ?: "0"
                    val l = document.getString("l") ?: "0"
                    val i = document.getString("i") ?: "0"

                  
                    val constructedPin = buildString {
                        append(if (p.length >= 1) p[0] else '0')
                        append(if (u.length >= 2) u[1] else '0')
                        append(if (l.length >= 3) l[2] else '0')
                        append(if (i.length >= 4) i[3] else '0')
                    }

                    dynamicPin = constructedPin
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "PIN config not found", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                runOnUiThread {
                    Toast.makeText(this, "Load PIN failed: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
