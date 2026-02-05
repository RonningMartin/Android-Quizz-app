package com.example.assignment1

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

import androidx.compose.material3.ExperimentalMaterial3Api
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    // Access the shared application state that holds all gallery entries
    val app = (context.applicationContext as? QuizApp)
        ?: error("QuizApp not configured as Application in AndroidManifest.xml")

    var showAddDialog by remember { mutableStateOf(false) }

    // Basic screen chrome: top bar, list content and FAB
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                actions = {
                    // Sort entries alphabetically in both directions
                    TextButton(onClick = { app.sortByName(true) }) { Text("A→Z") }
                    TextButton(onClick = { app.sortByName(false) }) { Text("Z→A") }
                }
            )
        },
        floatingActionButton = {
            // Open dialog for adding a new gallery entry
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("Add entry")
            }
        }
    ) { padding ->
        if (app.entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                // Hint for the user when there are no images yet
                Text("No entries yet. Add one!")
            }
        } else {
            // List all current gallery entries in a scrolling column
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Use default positional keys to avoid duplicate-ID crashes
                items(app.entries) { entry ->
                    GalleryRow(
                        entry = entry,
                        onRemove = { app.removeEntry(entry.id) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddEntryDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, uriString ->
                    app.addEntry(name, uriString)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun GalleryRow(entry: GalleryEntry, onRemove: () -> Unit) {
    // One row in the gallery list: image + name + hint text
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Tap image to remove this specific entry
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable { onRemove() }
            ) {
                when (val img = entry.image) {
                    is ImageRef.Resource -> Image(
                        painter = androidx.compose.ui.res.painterResource(img.resId),
                        contentDescription = entry.name,
                        modifier = Modifier.fillMaxSize()
                    )
                    is ImageRef.UriString -> AsyncImage(
                        model = img.uri,
                        contentDescription = entry.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(text = entry.name, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.weight(1f))

            Text(
                text = "Tap image to remove",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AddEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, uriString: String) -> Unit
) {
    val context = LocalContext.current  // Needed to launch the image picker and persist URI permission

    var name by remember { mutableStateOf("") }
    var chosenUriString by remember { mutableStateOf<String?>(null) }

    // Let the user pick an existing image from the device
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Persist read permission so Quiz screen can also load the image
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            chosenUriString = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                Button(onClick = { picker.launch(arrayOf("image/*")) }) {
                    Text(if (chosenUriString == null) "Pick image" else "Image selected ✓")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val uriStr = chosenUriString
                    if (name.isNotBlank() && uriStr != null) onAdd(name.trim(), uriStr)
                },
                enabled = name.isNotBlank() && chosenUriString != null
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
