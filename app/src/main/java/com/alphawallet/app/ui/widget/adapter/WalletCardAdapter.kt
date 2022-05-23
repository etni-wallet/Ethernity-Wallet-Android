package com.alphawallet.app.ui.widget.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.alphawallet.app.R
import com.alphawallet.app.entity.Wallet
import com.alphawallet.app.widget.CopyTextView


class WalletCardAdapter(
    val menuAction: () -> Unit,
    val sendAction: () -> Unit,
    val receiveAction: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var wallets = emptyList<Wallet>()

    fun setWallets(wallets: List<Wallet>) {
        val list = wallets.toMutableList()
        list.add(Wallet(""))
        this.wallets = list
        notifyDataSetChanged()
    }

    fun getWallet(position: Int) = wallets[position]

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
                holder.bind(wallets[position], menuAction, sendAction, receiveAction)
            }
            is CreateOrImportWalletViewHolder -> {
                holder.bind { menuAction() }
            }
        }
    }

    override fun getItemCount(): Int = wallets.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
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
    private val copyTextAddress = itemView.findViewById<TextView>(R.id.card_address_tv)
    private val cardActionMenu = itemView.findViewById<AppCompatImageView>(R.id.card_action_menu)
    private val sendButton = itemView.findViewById<TextView>(R.id.card_action_send)
    private val receiveButton = itemView.findViewById<TextView>(R.id.card_action_receive)

    fun bind(wallet: Wallet,
             menuAction: () -> Unit,
             sendAction: () -> Unit,
             receiveAction: () -> Unit,
    ) {
        cardName.text = wallet.name.ifBlank { "MainAccount" }
        cardBalance.text = wallet.balance
        copyTextAddress.text = wallet.address
        cardActionMenu.setOnClickListener { menuAction() }
        copyTextAddress.setOnClickListener { copyText() }
        sendButton.setOnClickListener { sendAction() }
        receiveButton.setOnClickListener { receiveAction() }
    }

    private fun copyText() {
        val clipboard = itemView.context
            .getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData
            .newPlainText(CopyTextView.KEY_ADDRESS, copyTextAddress.text.toString())
        clipboard?.setPrimaryClip(clip)

        showToast()
    }

    private fun showToast() {
        Toast.makeText(itemView.context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
            .show()
    }
}

class CreateOrImportWalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val VIEW_TYPE = 1008
    }

    fun bind(action: () -> Unit) {
        itemView.setOnClickListener { action() }
    }
}