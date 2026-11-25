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
import com.robert.projectwallet.adapter.ExpenseAdapter
import com.robert.projectwallet.model.Category
import com.robert.projectwallet.model.TransactionItem
import com.robert.projectwallet.utils.PrefManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ExpenseActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var rvExpenses: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddExpense: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    private var expenseCategories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expense)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootExpenseLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefManager = PrefManager(this)

        rvExpenses = findViewById(R.id.rvExpenses)
        tvEmpty = findViewById(R.id.tvEmptyExpense)
        fabAddExpense = findViewById(R.id.fabAddExpense)
        toolbar = findViewById(R.id.toolbarExpense)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()

        fabAddExpense.setOnClickListener {
            showExpenseDialog(null)
        }

        loadExpenses()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            emptyList(),
            onEditClick = { item -> showExpenseDialog(item) },
            onDeleteClick = { item -> confirmDeleteExpense(item) }
        )
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = expenseAdapter
    }

    private fun loadExpenses() {
        val allTransactions = prefManager.getTransactions()
        val expenses = allTransactions.filter { it.type == "EXPENSE" }
        expenseAdapter.updateData(expenses)

        if (expenses.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvExpenses.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvExpenses.visibility = View.VISIBLE
        }
    }

    private fun showExpenseDialog(item: TransactionItem?) {
        expenseCategories = prefManager.getCategories().filter { it.type == "EXPENSE" }

        if (expenseCategories.isEmpty()) {
            Toast.makeText(this, "Belum ada kategori Pengeluaran. Silakan buat di Master Kategori terlebih dahulu.", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_expense, null)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvExpenseDialogTitle)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etExpenseTitle)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etExpenseAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerExpenseCategory)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.etExpenseDate)

        val categoryNames = expenseCategories.map { it.name }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val isEdit = item != null
        if (isEdit) {
            tvDialogTitle.text = "Ubah Pengeluaran"
            etTitle.setText(item!!.title)
            etAmount.setText(item.amount.toString())
            etDate.setText(item.date)

            val categoryIndex = expenseCategories.indexOfFirst { it.id == item.categoryId }
            if (categoryIndex != -1) {
                spinnerCategory.setSelection(categoryIndex)
            }
        } else {
            tvDialogTitle.text = "Tambah Pengeluaran"
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
                    Toast.makeText(this, "Jumlah pengeluaran tidak valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val selectedCategory = expenseCategories[spinnerCategory.selectedItemPosition]

                if (isEdit) {
                    val updatedItem = TransactionItem(
                        id = item!!.id,
                        title = title,
                        amount = amount,
                        categoryId = selectedCategory.id,
                        categoryName = selectedCategory.name,
                        date = if (date.isEmpty()) currentDate else date,
                        type = "EXPENSE"
                    )
                    prefManager.updateTransaction(updatedItem)
                    Toast.makeText(this, "Pengeluaran berhasil diubah", Toast.LENGTH_SHORT).show()
                } else {
                    val newItem = TransactionItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        amount = amount,
                        categoryId = selectedCategory.id,
                        categoryName = selectedCategory.name,
                        date = if (date.isEmpty()) currentDate else date,
                        type = "EXPENSE"
                    )
                    prefManager.addTransaction(newItem)
                    Toast.makeText(this, "Pengeluaran berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                loadExpenses()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDeleteExpense(item: TransactionItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pengeluaran")
            .setMessage("Apakah Anda yakin ingin menghapus pengeluaran '${item.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                prefManager.deleteTransaction(item.id)
                Toast.makeText(this, "Pengeluaran berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadExpenses()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
