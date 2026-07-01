package com.example.bffandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.bffandroid.navigation.AppNavGraph
import com.example.bffandroid.ui.theme.BffAndroidTheme
import com.example.bffandroid.utils.AppSession
import com.example.bffandroid.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppSession.initialize(this)
        AppSession.logSnapshot("MainActivity.onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BffAndroidTheme {
                AppNavGraph()
            }
        }
    }
/*
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        AppSession.logSnapshot("MainActivity.onStart")
        mainViewModel.onAppOpen()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        AppSession.logSnapshot("MainActivity.onStop")
        mainViewModel.onAppClose()
    }

    private companion object {
        const val TAG = "MainActivity"
    }*/
}
