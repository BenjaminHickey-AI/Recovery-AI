package com.recovery.recovery_ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {}
    }
}
