package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.example.moneyflow.data.Wallet

class WalletAdapter(
    private val onItemClick: (Wallet) -> Unit,
    private val onAddClick: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private val TYPE_WALLET = 0
        internal val TYPE_ADD_BUTTON = 1
    }

    private var selectedPosition = RecyclerView.NO_POSITION
    var wallets: List<Wallet> = listOf<Wallet>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var isAllSelected = false

    fun selectAll() {
        isAllSelected = true
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun selectWallet(wallet: Wallet) {
        val position = wallets.indexOfFirst { it.id == wallet.id }
        if (position != -1) {
            isAllSelected = false
            val prevSelected = selectedPosition
            selectedPosition = position
            if (prevSelected != RecyclerView.NO_POSITION) {
                notifyItemChanged(prevSelected)
            }
            notifyItemChanged(selectedPosition)
        }
    }

    fun deselectAll() {
        isAllSelected = false
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < wallets.size) TYPE_WALLET else TYPE_ADD_BUTTON
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_WALLET) {
            val view = inflater.inflate(R.layout.wallet_item, parent, false)
            WalletViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.wallet_item, parent, false)
            AddButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (holder is WalletViewHolder) {
            val context = holder.itemView.context
            val wallet = wallets[position]
            val formatted = wallet.balance.formatWithSpaces()
            holder.textViewWalletNameAndBalance.text =
                holder.itemView.context.getString(
                    R.string.wallet_main_info,
                    wallet.name,
                    formatted
                )

            val iconResId = context.resources.getIdentifier(
                wallet.icon,
                "drawable",
                context.packageName
            )

            if (iconResId != 0) {
                holder.imageViewIcon.setImageResource(iconResId)
            } else {
                holder.imageViewIcon.setImageResource(R.drawable.ic_bank)
            }

            // Установка выделенного фона
            holder.cardRoot.setBackgroundResource(
                when {
                    isAllSelected -> R.drawable.bg_wallet_selected
                    position == selectedPosition -> R.drawable.bg_wallet_selected
                    else -> R.drawable.bg_wallet_normal
                }
            )

            holder.itemView.setOnClickListener {
                isAllSelected = false
                val prevSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(prevSelected)
                notifyItemChanged(selectedPosition)
                onItemClick(wallet)
            }

        } else if (holder is AddButtonViewHolder) {
            holder.textViewName.text = "Создать новый счет"
            holder.imageViewIcon.setImageResource(R.drawable.ic_add_black)

            holder.itemView.setBackgroundResource(R.drawable.bg_wallet_normal)

            holder.itemView.setOnClickListener {
                onAddClick()
            }
        }
    }

    override fun getItemCount(): Int {
        return wallets.size + 1
    }

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardRoot: CardView = itemView.findViewById(R.id.cardRoot)
        val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewWalletIcon)
        val textViewWalletNameAndBalance: TextView = itemView.findViewById(R.id.textViewWalletName)
    }

    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById<ImageView>(R.id.imageViewWalletIcon)
        val textViewName: TextView = itemView.findViewById<TextView>(R.id.textViewWalletName)
    }
}
