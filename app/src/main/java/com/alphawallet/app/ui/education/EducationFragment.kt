package com.alphawallet.app.ui.education

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.alphawallet.app.R
import com.alphawallet.app.ui.BaseFragment

class EducationFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_education, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<AppCompatImageView>(R.id.education_operator)
            .setOnClickListener { openEducationOperator() }
    }

    private fun openEducationOperator() {
        startActivity(Intent(requireContext(), EducationOperatorActivity::class.java))
    }
}