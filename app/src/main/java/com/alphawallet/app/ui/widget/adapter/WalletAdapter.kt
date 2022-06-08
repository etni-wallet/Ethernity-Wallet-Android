package com.alphawallet.app.ui.widget.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.alphawallet.app.R
import com.alphawallet.app.entity.Wallet
import com.alphawallet.app.widget.CopyTextView
import com.google.android.material.textview.MaterialTextView


class WalletAdapter(
    private val addAction: () -> Unit,
    private val menuAction: () -> Unit,
    private val sendAction: () -> Unit,
    private val receiveAction: () -> Unit,
    private val backupAction: () -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var wallets = emptyList<Wallet>()

    fun setWallets(wallets: List<Wallet>) {
        val list = wallets.toMutableList()
        list.add(Wallet("")) //add CreateOrImportViewHolder at the end
        this.wallets = emptyList()
        this.wallets = list
        notifyDataSetChanged()
    }

    fun getWallet(position: Int): Wallet? {
        if (position == wallets.size - 1)
            return null
        return wallets[position]
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
                holder.bind(wallets[position], menuAction, sendAction, receiveAction, backupAction)
            }
            is CreateOrImportWalletViewHolder -> {
                holder.bind { addAction() }
            }
        }
    }

    override fun getItemCount(): Int = wallets.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            in 0..wallets.size - 2 -> WalletCardViewHolder.VIEW_TYPE
            else -> CreateOrImportWalletViewHolder.VIEW_TYPE
        }
    }

    fun setBackupWarning(wallet: Wallet) {
        wallet.backupRequired = true
        notifyItemChanged(wallets.indexOf(wallet))
    }
}

class WalletCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        val VIEW_TYPE = 1007
    }

    private val walletName = itemView.findViewById<TextView>(R.id.wallet_name_tv)
    private val walletBalance = itemView.findViewById<TextView>(R.id.wallet_balance_tv)
    private val copyTextAddress = itemView.findViewById<TextView>(R.id.wallet_address_tv)
    private val walletActionMenu =
        itemView.findViewById<AppCompatImageView>(R.id.wallet_action_menu)
    private val walletActionSend = itemView.findViewById<TextView>(R.id.wallet_action_send)
    private val walletActionReceive = itemView.findViewById<TextView>(R.id.wallet_action_receive)
    private val backupBanner = itemView.findViewById<ConstraintLayout>(R.id.backup_banner)

    fun bind(
        wallet: Wallet,
        menuAction: () -> Unit,
        sendAction: () -> Unit,
        receiveAction: () -> Unit,
        backupAction: () -> Unit,
    ) {
        walletName.text = wallet.name.ifBlank { "MainAccount" }
        walletBalance.text = wallet.balance
        copyTextAddress.text = wallet.address
        copyTextAddress.setOnClickListener { copyText() }
        walletActionMenu.setOnClickListener { menuAction() }
        walletActionSend.setOnClickListener { sendAction() }
        walletActionReceive.setOnClickListener { receiveAction() }
        initBackupBanner(wallet, backupAction)
    }

    private fun initBackupBanner(wallet: Wallet, backupAction: () -> Unit) {
        if (wallet.backupRequired)
            backupBanner.visibility = View.VISIBLE
        else
            backupBanner.visibility = View.GONE
        itemView.findViewById<TextView>(R.id.backup_now).setOnClickListener {
            backupAction()
        }
        itemView.findViewById<AppCompatImageView>(R.id.close_backup).setOnClickListener {
            backupBanner.visibility = View.GONE
        }
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