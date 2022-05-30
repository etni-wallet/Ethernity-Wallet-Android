package com.alphawallet.app.ui.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.alphawallet.app.R

class DeleteWalletDialog(val action: () -> Unit) : DialogFragment() {

    private lateinit var deleteActionCancel: TextView
    private lateinit var deleteActionDone: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_delete_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        deleteActionCancel = requireView().findViewById(R.id.delete_cancel)
        deleteActionDone = requireView().findViewById(R.id.delete_confirmed)
    }

    private fun setupListeners() {
        deleteActionCancel.setOnClickListener { this.dismiss() }
        deleteActionDone.setOnClickListener {
            action()
            this.dismiss()
        }
    }
}
