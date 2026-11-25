package com.robert.projectwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.robert.projectwallet.adapter.IncomeAdapter
import com.robert.projectwallet.model.Category
import com.robert.projectwallet.model.TransactionItem
import com.robert.projectwallet.utils.PrefManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class IncomeActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var rvIncomes: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddIncome: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    private var incomeCategories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_income)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootIncomeLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefManager = PrefManager(this)

        rvIncomes = findViewById(R.id.rvIncomes)
        tvEmpty = findViewById(R.id.tvEmptyIncome)
        fabAddIncome = findViewById(R.id.fabAddIncome)
        toolbar = findViewById(R.id.toolbarIncome)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()

        fabAddIncome.setOnClickListener {
            showIncomeDialog(null)
        }

        loadIncomes()
    }

    private fun setupRecyclerView() {
        incomeAdapter = IncomeAdapter(
            emptyList(),
            onEditClick = { item -> showIncomeDialog(item) },
            onDeleteClick = { item -> confirmDeleteIncome(item) }
        )
        rvIncomes.layoutManager = LinearLayoutManager(this)
        rvIncomes.adapter = incomeAdapter
    }

    private fun loadIncomes() {
        val allTransactions = prefManager.getTransactions()
        val incomes = allTransactions.filter { it.type == "INCOME" }
        incomeAdapter.updateData(incomes)

        if (incomes.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvIncomes.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvIncomes.visibility = View.VISIBLE
        }
    }

    private fun showIncomeDialog(item: TransactionItem?) {
        incomeCategories = prefManager.getCategories().filter { it.type == "INCOME" }

        if (incomeCategories.isEmpty()) {
            Toast.makeText(this, "Belum ada kategori Pemasukan. Silakan buat di Master Kategori terlebih dahulu.", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_income, null)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvIncomeDialogTitle)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etIncomeTitle)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etIncomeAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerIncomeCategory)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.etIncomeDate)

        val categoryNames = incomeCategories.map { it.name }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val isEdit = item != null
        if (isEdit) {
            tvDialogTitle.text = "Ubah Pemasukan"
            etTitle.setText(item!!.title)
            etAmount.setText(item.amount.toString())
            etDate.setText(item.date)

            val categoryIndex = incomeCategories.indexOfFirst { it.id == item.categoryId }
            if (categoryIndex != -1) {
                spinnerCategory.setSelection(categoryIndex)
            }
        } else {
            tvDialogTitle.text = "Tambah Pemasukan"
            etDate.setText(currentDate)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val date = etDate.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(this, "Keterangan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0.0) {
                    Toast.makeText(this, "Jumlah pemasukan tidak valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedCategory = incomeCategories[spinnerCategory.selectedItemPosition]

                if (isEdit) {
                    val updatedItem = TransactionItem(
                        id = item!!.id,
                        title = title,
                        amount = amount,
                        categoryId = selectedCategory.id,
                        categoryName = selectedCategory.name,
                        date = if (date.isEmpty()) currentDate else date,
                        type = "INCOME"
                    )
                    prefManager.updateTransaction(updatedItem)
                    Toast.makeText(this, "Pemasukan berhasil diubah", Toast.LENGTH_SHORT).show()
                } else {
                    val newItem = TransactionItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        amount = amount,
                        categoryId = selectedCategory.id,
                        categoryName = selectedCategory.name,
                        date = if (date.isEmpty()) currentDate else date,
                        type = "INCOME"
                    )
                    prefManager.addTransaction(newItem)
                    Toast.makeText(this, "Pemasukan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                loadIncomes()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDeleteIncome(item: TransactionItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pemasukan")
            .setMessage("Apakah Anda yakin ingin menghapus pemasukan '${item.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                prefManager.deleteTransaction(item.id)
                Toast.makeText(this, "Pemasukan berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadIncomes()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
