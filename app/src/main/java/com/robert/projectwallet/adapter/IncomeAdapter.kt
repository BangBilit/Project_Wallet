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

class IncomeAdapter(
    private var incomes: List<TransactionItem>,
    private val onEditClick: (TransactionItem) -> Unit,
    private val onDeleteClick: (TransactionItem) -> Unit
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvIncomeTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvIncomeCategory)
        val tvDate: TextView = view.findViewById(R.id.tvIncomeDate)
        val tvAmount: TextView = view.findViewById(R.id.tvIncomeAmount)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditIncome)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteIncome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val item = incomes[position]
        holder.tvTitle.text = item.title
        holder.tvCategory.text = "Kategori: ${item.categoryName}"
        holder.tvDate.text = item.date

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.tvAmount.text = formatter.format(item.amount)

        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = incomes.size

    fun updateData(newIncomes: List<TransactionItem>) {
        incomes = newIncomes
        notifyDataSetChanged()
    }
}
