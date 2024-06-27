package com.seiko.composeclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.seiko.composeclock.ui.theme.ComposeClockTheme
import com.seiko.composeclock.ui.theme.backgroundColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeClockTheme {
                Surface(Modifier.fillMaxSize(), color = backgroundColor) {
                    Box(Modifier, Alignment.Center) {
                        val clockState = rememberClockState()
                        ComposeRoundClock(clockState = clockState)
                    }
                }
            }
        }
    }
}
