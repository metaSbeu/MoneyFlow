package com.example.moneyflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WalletAdapter : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    var wallets: List<Wallet> = listOf<Wallet>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WalletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.wallet_item,
            parent,
            false
        )
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WalletViewHolder,
        position: Int
    ) {
        val wallet = wallets[position]
        holder.textViewWalletName.text = wallet.name
        holder.textViewWalletBalance.text = wallet.balance.toString()
        holder.imageViewIcon.setImageResource(R.drawable.alpha)
    }

    override fun getItemCount(): Int {
        return wallets.size
    }

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById<ImageView>(R.id.imageViewWalletIcon)
        val textViewWalletName = itemView.findViewById<TextView>(R.id.textViewWalletName)
        val textViewWalletBalance = itemView.findViewById<TextView>(R.id.textViewWalletBalance)
    }
}