package com.example.coroutinelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.coroutinelab.ui.CoroutineLabApp
import com.example.coroutinelab.ui.theme.CoroutineLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoroutineLabTheme {
                CoroutineLabApp()
            }
        }
    }
}

