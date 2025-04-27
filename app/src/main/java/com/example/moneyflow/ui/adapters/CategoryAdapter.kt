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
    private val onItemClick: (Category) -> Unit,
    private val onAddClick: () -> Unit,
    private val showAddButton: Boolean = true,
    private val isIncome: Boolean,
    private val onFirstCategorySelected: ((Category) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_CATEGORY = 0
    private val TYPE_ADD_BUTTON = 1

    private var selectedPosition = RecyclerView.NO_POSITION
    var categories = listOf<Category>()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                selectedPosition = 0
                onFirstCategorySelected?.invoke(value[0])
            } else {
                selectedPosition = RecyclerView.NO_POSITION
            }
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return if (position < categories.size) TYPE_CATEGORY else TYPE_ADD_BUTTON
    }

    override fun getItemCount(): Int = categories.size + if (showAddButton) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_CATEGORY) {
            val view = inflater.inflate(R.layout.category_item, parent, false)
            CategoryViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.category_item, parent, false)
            AddButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryViewHolder) {
            val category = categories[position]
            holder.textViewName.text = category.name

            holder.icon.setImageResource(category.iconResId)

            if (position == selectedPosition) {
                holder.icon.setBackgroundResource(R.drawable.circle_indicator_blue)
            } else {
                holder.icon.setBackgroundResource(R.drawable.circle_indicator_gray)
            }

            holder.itemView.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onItemClick(category)
            }
        } else if (holder is AddButtonViewHolder) {
            holder.textViewName.text = "Создать"
            holder.icon.setImageResource(R.drawable.ic_add_white) // иконка "+"
            holder.icon.setBackgroundResource(R.drawable.circle_indicator_gray)

            holder.itemView.setOnClickListener {
                onAddClick()
            }
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewCategory)
        val icon: ImageView = itemView.findViewById(R.id.imageViewCategoryIcon)
    }

    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewCategory)
        val icon: ImageView = itemView.findViewById(R.id.imageViewCategoryIcon)
    }
}
