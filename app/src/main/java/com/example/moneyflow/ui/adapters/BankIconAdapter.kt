package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.ui.adapters.BankIconAdapter.BankIconViewHolder
import com.example.moneyflow.utils.IconResolver

class BankIconAdapter(
    private val icons: List<String>,
    private val onIconClick: (String) -> Unit,
    preselectedIcon: String? = null
) : RecyclerView.Adapter<BankIconViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    init {
        if (preselectedIcon != null) {
            selectedPosition = icons.indexOf(preselectedIcon)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BankIconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)
        return BankIconViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BankIconViewHolder, position: Int
    ) {
        val iconName = icons[position]

        val iconResId = IconResolver.resolve(iconName)
        holder.icon.setImageResource(iconResId)

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.category_background_selected)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.category_background)
        }

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)

            onIconClick(iconName)
        }
    }

    override fun getItemCount(): Int {
        return icons.size
    }

    class BankIconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.imageViewIBankIcon)
    }
}