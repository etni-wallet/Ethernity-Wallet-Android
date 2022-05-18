package com.alphawallet.app.ui.widget.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.alphawallet.app.R
import com.alphawallet.app.entity.Wallet


class WalletCardAdapter(val action: () -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var wallets = emptyList<Wallet>()

    fun setWallets(wallets: List<Wallet>) {
        val list = wallets.toMutableList()
        list.add(Wallet(""))
        this.wallets = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            WalletCardViewHolder.VIEW_TYPE -> {
                val itemView: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_wallet_card, parent, false)
                return WalletCardViewHolder(itemView)
            }
            CreateOrImportWalletViewHolder.VIEW_TYPE -> {
                val itemView: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_wallet_create_card, parent, false)
                return CreateOrImportWalletViewHolder(itemView)
            }
        }

        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet_create_card, parent, false)
        return CreateOrImportWalletViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WalletCardViewHolder -> {
                holder.bind(wallets[position])
            }
            is CreateOrImportWalletViewHolder -> {
                holder.bindListener { action() }
            }
        }
    }

    override fun getItemCount(): Int = wallets.size

    override fun getItemViewType(position: Int): Int {
        return when(position){
            in wallets.indices - 1 -> WalletCardViewHolder.VIEW_TYPE
            else -> CreateOrImportWalletViewHolder.VIEW_TYPE
        }
    }
}

class WalletCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val VIEW_TYPE = 1007
    }

    private val cardName = itemView.findViewById<TextView>(R.id.card_name_tv)
    private val cardBalance = itemView.findViewById<TextView>(R.id.card_balance_tv)
    private val cardAddress = itemView.findViewById<TextView>(R.id.card_address_tv)

    fun bind(wallet: Wallet) {
        cardName.text = wallet.name.ifBlank { "MainAccount" }
        cardBalance.text = wallet.balance
        cardAddress.text = wallet.address
        setCopyPasteListener()
    }

    private fun setCopyPasteListener() {
        cardAddress.setOnClickListener {
            Toast.makeText(
                itemView.context,
                "Copied " + cardAddress.text.toString(),
                Toast.LENGTH_SHORT
            ).show()
            Log.i("CardAddress", cardAddress.text.toString());
            val clipboard =
                itemView.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("CardAddress", "c " + cardAddress.text.toString())
            clipboard?.setPrimaryClip(clip)
        }
    }
}

class CreateOrImportWalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val VIEW_TYPE = 1008
    }

    fun bindListener(action: () -> Unit) {
        itemView.setOnClickListener { action() }
    }
}