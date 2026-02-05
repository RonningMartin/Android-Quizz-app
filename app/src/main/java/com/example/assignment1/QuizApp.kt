package com.example.assignment1

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateListOf

// One item in the shared gallery (name + image)
data class GalleryEntry(
    val id: Long,
    val name: String,
    val image: ImageRef
)

// How we refer to an image (either from resources or a picked URI)
sealed interface ImageRef {
    data class Resource(@DrawableRes val resId: Int) : ImageRef
    data class UriString(val uri: String) : ImageRef
}

class QuizApp : Application() {
    // Backing list of all gallery entries shared across activities
    val entries = mutableStateListOf<GalleryEntry>()

    // Simple increasing ID to guarantee uniqueness
    private var nextId: Long = 1L

    // Generate a fresh unique ID for each new entry
    private fun newId(): Long = nextId++

    override fun onCreate() {
        super.onCreate()

        // Initialise the app with three built-in dog photos
        entries.addAll(
            listOf(
                GalleryEntry(id = newId(), name = "Lars", image = ImageRef.Resource(R.drawable.dog1)),
                GalleryEntry(id = newId(), name = "Zenta", image = ImageRef.Resource(R.drawable.dog2)),
                GalleryEntry(id = newId(), name = "Nala", image = ImageRef.Resource(R.drawable.dog3)),
            )
        )
    }

    // Add a new entry created by the user from the gallery picker
    fun addEntry(name: String, uriString: String) {
        entries.add(GalleryEntry(id = newId(), name = name, image = ImageRef.UriString(uriString)))
    }

    // Remove a single entry by ID
    fun removeEntry(id: Long) {
        entries.removeAll { it.id == id }
    }

    // Sort the list alphabetically (A→Z or Z→A)
    fun sortByName(ascending: Boolean) {
        val sorted = entries.sortedBy { it.name.lowercase() }
        entries.clear()
        entries.addAll(if (ascending) sorted else sorted.asReversed())
    }
}
