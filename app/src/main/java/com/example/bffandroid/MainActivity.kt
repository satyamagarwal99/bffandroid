package com.example.bffandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bffandroid.navigation.AppNavGraph
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.utils.AppSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppSession.initialize(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BffAndroidTheme {
                AppNavGraph()
            }
        }
    }
}
