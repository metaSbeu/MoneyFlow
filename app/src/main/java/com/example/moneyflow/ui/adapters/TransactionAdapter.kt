package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.data.Transaction
import com.example.moneyflow.data.TransactionWithCategory
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.utils.IconResolver
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    var transactions = listOf<TransactionWithCategory>()
        private set

    fun updateTransactions(newTransactions: List<TransactionWithCategory>) {
        val diffCallback = object : androidx.recyclerview.widget.DiffUtil.Callback() {
            override fun getOldListSize() = transactions.size
            override fun getNewListSize() = newTransactions.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return transactions[oldItemPosition].transaction.id == newTransactions[newItemPosition].transaction.id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return transactions[oldItemPosition] == newTransactions[newItemPosition]
            }
        }

        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)
        transactions = newTransactions
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TransactionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder, position: Int
    ) {
        val (transaction, category) = transactions[position]
        val context = holder.itemView.context

        val iconResId = IconResolver.resolve(category.icon)

        holder.imageViewIcon.setImageResource(iconResId)

        holder.textViewCategory.text = category.name

        val formattedSumWithSign = if (transaction.isIncome) {
            "+${transaction.sum.formatWithSpaces(holder.itemView.context)}"
        } else {
            "-${transaction.sum.formatWithSpaces(holder.itemView.context)}"
        }
        holder.textViewAmount.text = formattedSumWithSign

        val note = transaction.note
        holder.textViewComment.text = if (note.isNullOrBlank()) {
            holder.itemView.context.getString(R.string.comment_s, context.getString(R.string.empty))
        } else {
            holder.itemView.context.getString(R.string.comment_s, note)
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(transaction.createdAt)

        holder.textViewDate.text = formattedDate

        val colorResId = when (transaction.isIncome) {
            true -> R.color.light_green
            false -> R.color.light_red
        }
        val color = ContextCompat.getColor(holder.itemView.context, colorResId)
        holder.textViewAmount.setTextColor(color)

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById<ImageView>(R.id.imageViewCategoryIcon)
        val textViewCategory: TextView = itemView.findViewById<TextView>(R.id.textViewCategoryName)
        val textViewAmount: TextView = itemView.findViewById<TextView>(R.id.textViewSum)
        val textViewComment: TextView = itemView.findViewById<TextView>(R.id.textViewComment)
        val textViewDate: TextView = itemView.findViewById<TextView>(R.id.textViewDate)
    }
}