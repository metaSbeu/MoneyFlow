package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.ui.adapters.BankIconAdapter.BankIconViewHolder

class BankIconAdapter(
    private val icons: List<Int>,
    private val onIconClick: (Int) -> Unit
): RecyclerView.Adapter<BankIconViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BankIconViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)
        return BankIconViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BankIconViewHolder,
        position: Int
    ) {

        val iconResId = icons[position]

        holder.icon.setImageResource(iconResId)

        if (position == selectedPosition) {
            holder.icon.setBackgroundResource(R.drawable.circle_indicator_blue)
        } else {
            holder.icon.setBackgroundResource(R.drawable.circle_indicator_gray)
        }

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)

            onIconClick(iconResId)
        }
    }

    override fun getItemCount(): Int {
        return icons.size
    }


    class BankIconViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById<ImageView>(R.id.imageViewIBankIcon)
    }
}