package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.data.Category
import com.example.moneyflow.utils.IconResolver

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit,
    private val onAddClick: () -> Unit,
    private val showAddButton: Boolean = true,
    internal var isIncome: Boolean,
    private val onFirstCategorySelected: ((Category) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val typeCategory = 0
    private val typeAddButton = 1

    private var selectedPosition = RecyclerView.NO_POSITION
    var categories = listOf<Category>()
        private set

    fun updateCategories(newCategories: List<Category>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = categories.size
            override fun getNewListSize() = newCategories.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return categories[oldItemPosition].id == newCategories[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return categories[oldItemPosition] == newCategories[newItemPosition]
            }
        })

        categories = newCategories
        diffResult.dispatchUpdatesTo(this)

        selectedPosition = if (newCategories.isNotEmpty()) 0 else RecyclerView.NO_POSITION
        if (selectedPosition != RecyclerView.NO_POSITION) {
            onFirstCategorySelected?.invoke(newCategories[selectedPosition])
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < categories.size) typeCategory else typeAddButton
    }

    override fun getItemCount(): Int = categories.size + if (showAddButton) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == typeCategory) {
            CategoryViewHolder(inflater.inflate(R.layout.category_item, parent, false))
        } else {
            AddButtonViewHolder(inflater.inflate(R.layout.category_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> bindCategoryViewHolder(holder, position)
            is AddButtonViewHolder -> bindAddButtonViewHolder(holder)
        }
    }

    private fun bindCategoryViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val context = holder.itemView.context

        holder.textViewName.text = category.name

        val iconResId = IconResolver.resolve(category.icon)

        holder.icon.setImageResource(iconResId)

        val backgroundRes = if (position == selectedPosition) {
            R.drawable.circle_indicator_blue
        } else {
            R.drawable.circle_indicator_gray
        }
        holder.icon.background = ContextCompat.getDrawable(context, backgroundRes)

        holder.itemView.setOnClickListener {
            updateSelection(position)
            onItemClick(category)
        }
    }

    private fun bindAddButtonViewHolder(holder: AddButtonViewHolder) {
        val context = holder.itemView.context

        holder.textViewName.text = context.getString(R.string.add)
        holder.icon.setImageResource(R.drawable.ic_add_white)
        holder.icon.background =
            ContextCompat.getDrawable(context, R.drawable.circle_indicator_gray)

        holder.itemView.setOnClickListener { onAddClick() }
    }

    private fun updateSelection(newPosition: Int) {
        val prev = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(prev)
        notifyItemChanged(selectedPosition)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewCategory)
        val icon: ImageView = itemView.findViewById(R.id.imageViewCategoryIcon)
    }

    inner class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewCategory)
        val icon: ImageView = itemView.findViewById(R.id.imageViewCategoryIcon)
    }
}