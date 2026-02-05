package com.example.assignment1

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.random.Random
import androidx.compose.material3.ExperimentalMaterial3Api

// One quiz round: the correct entry and the three answer options
private data class Question(
    val entry: GalleryEntry,
    val options: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen() {
    val context = LocalContext.current
    // Access the shared gallery entries from the Application class
    val app = (context.applicationContext as? QuizApp)
        ?: error("QuizApp not configured as Application in AndroidManifest.xml")
    val entries = app.entries

    // Track quiz score across recompositions and configuration changes
    var correct by rememberSaveable { mutableIntStateOf(0) }
    var attempts by rememberSaveable { mutableIntStateOf(0) }

    var question by remember { mutableStateOf<Question?>(null) }
    var selected by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var showNext by remember { mutableStateOf(false) }

    // Build a new random question with one correct and two wrong names
    fun newQuestion() {
        if (entries.size < 3) {
            question = null
            feedback = "Need at least 3 entries to start. Add more in Gallery."
            return
        }

        val correctEntry = entries.random()
        val wrong = entries.filter { it.id != correctEntry.id }.shuffled().take(2).map { it.name }
        val opts = (wrong + correctEntry.name).shuffled(Random(System.nanoTime()))

        question = Question(correctEntry, opts)
        selected = null
        feedback = null
        showNext = false
    }

    // Whenever the number of entries changes, ensure we have a question ready
    LaunchedEffect(entries.size) {
        if (question == null && entries.size >= 3) newQuestion()
    }

    // Basic quiz layout: app bar, image, options and feedback
    Scaffold(
        topBar = { TopAppBar(title = { Text("Quiz") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Score: $correct / $attempts", style = MaterialTheme.typography.titleMedium)

            val q = question  // Local alias for nicer code below
            if (q == null) {
                Text(feedback ?: "Add entries in Gallery to start.")
                Button(onClick = { newQuestion() }, enabled = entries.size >= 3) {
                    Text("Start quiz")
                }
                return@Column
            }

            // Show the image for the current question
            Card {
                Box(Modifier.fillMaxWidth().padding(12.dp)) {
                    when (val img = q.entry.image) {
                        is ImageRef.Resource -> Image(
                            painter = androidx.compose.ui.res.painterResource(img.resId),
                            contentDescription = q.entry.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        is ImageRef.UriString -> AsyncImage(
                            model = img.uri,
                            contentDescription = q.entry.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }

            // Three answer options (one correct, two wrong)
            q.options.forEach { opt ->
                OutlinedButton(
                    onClick = { if (!showNext) selected = opt },
                    enabled = !showNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (selected == opt) "✓ $opt" else opt)
                }
            }

            if (!showNext) {
                Button(
                    onClick = {
                        val answer = selected ?: return@Button
                        attempts += 1
                        if (answer == q.entry.name) {
                            correct += 1
                            feedback = "Correct!"
                        } else {
                            feedback = "Wrong. Correct answer: ${q.entry.name}"
                        }
                        showNext = true
                    },
                    enabled = selected != null
                ) { Text("Submit") }
            } else {
                Text(feedback ?: "")
                Button(onClick = { newQuestion() }) { Text("Next") }
            }
        }
    }
}
