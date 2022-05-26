package com.alphawallet.app.ui.education

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.alphawallet.app.R

class EducationOperatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_education_operator)
        findViewById<AppCompatImageView>(R.id.education_back).setOnClickListener { finish() }
    }
}