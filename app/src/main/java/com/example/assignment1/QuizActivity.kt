package com.example.assignment1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Host the quiz UI using Jetpack Compose
        setContent {
            MaterialTheme {
                Surface {
                    // Main quiz screen where the user guesses names
                    QuizScreen()
                }
            }
        }
    }
}
