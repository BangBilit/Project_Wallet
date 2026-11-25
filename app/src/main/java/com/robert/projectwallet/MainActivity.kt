package com.robert.projectwallet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnCategory = findViewById<Button>(R.id.btnCategory)
        val btnIncome = findViewById<Button>(R.id.btnIncome)
        val btnExpense = findViewById<Button>(R.id.btnExpense)

        btnCategory.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        btnIncome.setOnClickListener {
            Toast.makeText(this, "Fitur Pemasukan (No. 3) akan segera dikerjakan berikutnya!", Toast.LENGTH_SHORT).show()
        }

        btnExpense.setOnClickListener {
            Toast.makeText(this, "Fitur Pengeluaran (No. 4) akan segera dikerjakan berikutnya!", Toast.LENGTH_SHORT).show()
        }
    }
}
