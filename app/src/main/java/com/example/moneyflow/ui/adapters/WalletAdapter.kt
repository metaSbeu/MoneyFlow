package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.data.Wallet

class WalletAdapter(
    private val onItemClick: (Wallet) -> Unit,
    private val onAddClick: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_WALLET = 0
    private val TYPE_ADD_BUTTON = 1

    private var selectedPosition = RecyclerView.NO_POSITION
    var wallets: List<Wallet> = listOf<Wallet>()
        set(value) {
            field = value
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WalletViewHolder) {
            val wallet = wallets[position]
            holder.textViewWalletName.text = wallet.name
            holder.textViewWalletBalance.text = wallet.balance.toString()

            // Получаем ID ресурса для иконки из имени, которое хранится в базе данных
            val iconResId = holder.itemView.context.resources.getIdentifier(
                wallet.icon, // имя ресурса, например "logo_sber"
                "drawable", // тип ресурса
                holder.itemView.context.packageName // имя пакета
            )

            // Устанавливаем иконку
            if (iconResId != 0) { // Проверяем, что иконка найдена
                holder.imageViewIcon.setImageResource(iconResId)
            } else {
                // Если иконка не найдена, можно установить дефолтную
                holder.imageViewIcon.setImageResource(R.drawable.ic_bank)
            }

        } else if (holder is AddButtonViewHolder) {
            holder.textViewName.text = "Создать новый счет"
            holder.imageViewIcon.setImageResource(R.drawable.ic_add_black)

            holder.itemView.setOnClickListener {
                onAddClick()
            }
        }
    }

    override fun getItemCount(): Int {
        return wallets.size + 1
    }

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById<ImageView>(R.id.imageViewWalletIcon)
        val textViewWalletName: TextView = itemView.findViewById<TextView>(R.id.textViewWalletName)
        val textViewWalletBalance: TextView = itemView.findViewById<TextView>(R.id.textViewWalletBalance)
    }

    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById<ImageView>(R.id.imageViewWalletIcon)
        val textViewName: TextView = itemView.findViewById<TextView>(R.id.textViewWalletName)
    }
}