package com.alphawallet.app.ui.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.alphawallet.app.R

class RenameWalletDialog(val setWalletName: (name: String) -> Unit) : DialogFragment() {
    private lateinit var renameWalletInput: EditText
    private lateinit var renameActionCancel: TextView
    private lateinit var renameActionDone: TextView
    private lateinit var errorToast: Toast

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_rename_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        renameWalletInput = requireView().findViewById(R.id.rename_wallet_input)
        renameActionCancel = requireView().findViewById(R.id.rename_cancel)
        renameActionDone = requireView().findViewById(R.id.rename_done)
        errorToast = Toast.makeText(
            requireContext(),
            requireContext().getString(R.string.rename_not_complete),
            Toast.LENGTH_SHORT
        )
    }

    private fun setupListeners() {
        renameActionCancel.setOnClickListener { cancel() }
        renameActionDone.setOnClickListener {
            val newName = getNameFromView()
            newName?.let {
                setWalletName(it)
                cancel()
            } ?: run {
                errorToast.show()
            }
        }
    }

    private fun cancel() {
        this.dismiss()
        errorToast.cancel()
    }

    private fun getNameFromView(): String? {
        val input = renameWalletInput.text.toString()
        if (input.isEmpty())
            return null
        return input
    }
}
