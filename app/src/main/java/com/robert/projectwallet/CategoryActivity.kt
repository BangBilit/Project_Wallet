package com.robert.projectwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
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
import com.robert.projectwallet.adapter.CategoryAdapter
import com.robert.projectwallet.model.Category
import com.robert.projectwallet.utils.PrefManager
import java.util.UUID

class CategoryActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var rvCategories: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddCategory: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category)

        // Handle window insets so toolbar doesn't overlap with status bar (Dynamic Island / cutout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootCategoryLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefManager = PrefManager(this)

        rvCategories = findViewById(R.id.rvCategories)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabAddCategory = findViewById(R.id.fabAddCategory)
        toolbar = findViewById(R.id.toolbarCategory)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()

        fabAddCategory.setOnClickListener {
            showCategoryDialog(null)
        }

        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            emptyList(),
            onEditClick = { category -> showCategoryDialog(category) },
            onDeleteClick = { category -> confirmDeleteCategory(category) }
        )
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.adapter = categoryAdapter
    }

    private fun loadCategories() {
        val categories = prefManager.getCategories()
        categoryAdapter.updateData(categories)
        if (categories.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvCategories.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvCategories.visibility = View.VISIBLE
        }
    }

    private fun showCategoryDialog(category: Category?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category, null)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etCategoryName = dialogView.findViewById<TextInputEditText>(R.id.etCategoryName)
        val rgCategoryType = dialogView.findViewById<RadioGroup>(R.id.rgCategoryType)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)

        val isEdit = category != null
        if (isEdit) {
            tvDialogTitle.text = "Ubah Kategori"
            etCategoryName.setText(category!!.name)
            if (category.type == "INCOME") {
                rbIncome.isChecked = true
            } else {
                dialogView.findViewById<RadioButton>(R.id.rbExpense).isChecked = true
            }
        } else {
            tvDialogTitle.text = "Tambah Kategori"
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etCategoryName.text.toString().trim()
                val type = if (rgCategoryType.checkedRadioButtonId == R.id.rbIncome) "INCOME" else "EXPENSE"

                if (name.isEmpty()) {
                    Toast.makeText(this, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (isEdit) {
                    val updatedCategory = Category(category.id, name, type)
                    prefManager.updateCategory(updatedCategory)
                    Toast.makeText(this, "Kategori berhasil diubah", Toast.LENGTH_SHORT).show()
                } else {
                    val newCategory = Category(UUID.randomUUID().toString(), name, type)
                    prefManager.addCategory(newCategory)
                    Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                loadCategories()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDeleteCategory(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori '${category.name}'?")
            .setPositiveButton("Hapus") { _, _ ->
                prefManager.deleteCategory(category.id)
                Toast.makeText(this, "Kategori berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadCategories()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
