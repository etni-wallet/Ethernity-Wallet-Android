package com.alphawallet.app.ui.widget.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alphawallet.app.R
import com.alphawallet.app.entity.Wallet

class WalletCardAdapter() : RecyclerView.Adapter<WalletCardViewHolder>() {

    private var wallets = emptyList<Wallet>()

    fun setWallets(wallets : List<Wallet>){
        this.wallets = wallets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletCardViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet_card, parent, false)
        return WalletCardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WalletCardViewHolder, position: Int) {
        holder.bind(wallets[position])
    }

    override fun getItemCount(): Int = wallets.size
}

class WalletCardViewHolder(view : View) : RecyclerView.ViewHolder(view) {

    private val cardName = itemView.findViewById<TextView>(R.id.card_name_tv)
    private val cardBalance = itemView.findViewById<TextView>(R.id.card_balance_tv)
    private val cardAddress = itemView.findViewById<TextView>(R.id.card_address_tv)

    fun bind(wallet : Wallet) {
        cardName.text = wallet.name.ifBlank { "MainAccount" }
        cardBalance.text = wallet.balance
        cardAddress.text = wallet.address
    }
}