package edu.rit.ks4898.narria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ——————————————————————————————————————————— Load user’s books
    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("books")
                    .get()
                    .await()

                books = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Book::class.java)?.copy(
                        id = doc.id,
                        bookId = doc.getString("bookId") ?: ""
                    )
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ——————————————————————————————————————————— UI
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Books", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            books.isEmpty() -> {
                EmptyState(
                    message = "Your library is empty.\nAdd a book from the Search tab!",
                    icon   = Icons.Default.Search
                )
            }
            else -> {
                // Existing list/row sections  ……………………………………………………………
                LazyColumn {
                    item {
                        Section(
                            title = "Reading Now",
                            books = books.filter { it.readingStatus == "Reading" },
                            navController = navController
                        )
                    }
                    item {
                        Section(
                            title = "To Read",
                            books = books.filter { it.readingStatus == "To-Read" },
                            navController = navController
                        )
                    }
                    item {
                        Section(
                            title = "Completed",
                            books = books.filter { it.readingStatus == "Completed" },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

// ---------- helper composable for the three carousel sections ----------
@Composable
private fun Section(
    title: String,
    books: List<Book>,
    navController: NavHostController
) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    if (books.isEmpty()) {
        Text(
            "No books in this category",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        LazyRow {
            items(books) { book ->
                BookCard(book) {
                    navController.navigate("bookDetail/${book.id}/${book.isFavorite}")
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
fun BookCard(book: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            BookCover(
                coverUrl = book.coverUrl,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                StarRating(book.rating)
            }

            if (book.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(4.dp)
                )
            }
        }
    }
}