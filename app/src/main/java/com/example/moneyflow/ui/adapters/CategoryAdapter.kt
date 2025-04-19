package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.data.Category

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    var categories = mutableListOf<Category>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.textViewName.text = category.name
        holder.icon.setImageResource(R.drawable.ic_home) // или category.iconResId

        // Изменяем фон в зависимости от выбранности
        if (position == selectedPosition) {
            holder.icon.setBackgroundResource(R.drawable.circle_indicator_blue) // фоновый drawable для выбранного
        } else {
            holder.icon.setBackgroundResource(R.drawable.circle_indicator_gray) // для невыбранного
        }

        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)

            onItemClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewCategory)
        val icon: ImageView = itemView.findViewById(R.id.imageViewCategoryIcon)
    }
}
