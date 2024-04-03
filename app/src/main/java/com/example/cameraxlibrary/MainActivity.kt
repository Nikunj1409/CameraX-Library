package com.example.cameraxlibrary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val i = Intent(this, SelectActivity::class.java)
        startActivity(i)
        finish()
    }
}