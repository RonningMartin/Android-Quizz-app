package com.example.assignment1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Host the gallery UI using Jetpack Compose
        setContent {
            MaterialTheme {
                Surface {
                    // Main gallery screen showing all entries
                    GalleryScreen()
                }
            }
        }
    }
}
