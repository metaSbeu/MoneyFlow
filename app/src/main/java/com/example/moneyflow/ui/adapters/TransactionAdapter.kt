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
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.nanoseconds

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
): RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    var transactions = listOf<TransactionWithCategory>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
//        val transactionWithCategory = transactions[position]

        val (transaction, category) = transactions[position]
        val context = holder.itemView.context

        // Загрузка иконки категории по имени
        val iconResId = context.resources.getIdentifier(
            category.icon,
            "drawable",
            context.packageName
        )

        holder.imageViewIcon.setImageResource(iconResId)

        holder.textViewCategory.text = category.name
        holder.textViewAmount.text = transaction.sum.toString()
        val note = transaction.note
        holder.textViewComment.text = if (note.isNullOrBlank()) {
            holder.itemView.context.getString(R.string.comment_s, "<пусто>") // Default string from resources
        } else {
            holder.itemView.context.getString(R.string.comment_s, note)
        }


        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Пример формата
        val formattedDate = dateFormat.format(transaction.createdAt) // Преобразование временной метки в строку

        holder.textViewDate.text = formattedDate

        val colorResId = when(transaction.isIncome) {
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
        val imageViewIcon = itemView.findViewById<ImageView>(R.id.imageViewCategoryIcon)
        val textViewCategory = itemView.findViewById<TextView>(R.id.textViewCategoryName)
        val textViewAmount = itemView.findViewById<TextView>(R.id.textViewSum)
        val textViewComment = itemView.findViewById<TextView>(R.id.textViewComment)
        val textViewDate = itemView.findViewById<TextView>(R.id.textViewDate)
    }
}