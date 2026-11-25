package com.robert.projectwallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.robert.projectwallet.R
import com.robert.projectwallet.model.TransactionItem
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(
    private var expenses: List<TransactionItem>,
    private val onEditClick: (TransactionItem) -> Unit,
    private val onDeleteClick: (TransactionItem) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvExpenseTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvExpenseCategory)
        val tvDate: TextView = view.findViewById(R.id.tvExpenseDate)
        val tvAmount: TextView = view.findViewById(R.id.tvExpenseAmount)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditExpense)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteExpense)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenses[position]
        holder.tvTitle.text = item.title
        holder.tvCategory.text = "Kategori: ${item.categoryName}"
        holder.tvDate.text = item.date

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.tvAmount.text = formatter.format(item.amount)

        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<TransactionItem>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
