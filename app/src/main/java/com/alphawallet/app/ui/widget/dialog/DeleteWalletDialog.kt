package com.alphawallet.app.ui.widget.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
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
        setBackgroundAndResize() // Set transparent background and no title
        resizeDialog()
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

    private fun setBackgroundAndResize() {
        dialog?.let {
            it.window?.let { window ->
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.requestFeature(Window.FEATURE_NO_TITLE)
            }
        }
    }

    private fun resizeDialog() {
        this.dialog?.window?.let { window ->
            val marginTop = resources.getDimensionPixelSize(R.dimen.dp18) * 2
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.gravity = Gravity.TOP
            layoutParams.y = marginTop
            layoutParams.flags =
                layoutParams.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
            window.attributes = layoutParams
        }
    }
}
