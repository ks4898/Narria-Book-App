package edu.rit.ks4898.narria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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

    // ─────────────────── Load user’s books ───────────────────
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
            } finally { isLoading = false }
        }
    }

    // ─────────────────────── UI ───────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Books", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

            books.isEmpty() -> EmptyState(
                message = "Your library is empty.\nAdd a book from the Search tab!",
                icon    = Icons.Filled.Book
            )

            else -> LazyColumn {
                item { CategorySection("Reading Now", books.filter { it.readingStatus == "Reading" }, navController) }
                item { CategorySection("To Read",      books.filter { it.readingStatus == "To-Read" },  navController) }
                item { CategorySection("Completed",     books.filter { it.readingStatus == "Completed" },navController) }
            }
        }
    }
}

// ---------- Small helper composable for each carousel ----------
@Composable
private fun CategorySection(
    title: String,
    books: List<Book>,
    navController: NavHostController
) {
    Text(title, style = MaterialTheme.typography.titleLarge)

    if (books.isEmpty()) {
        InlineCardEmptyState(
            message = "No books in this category",
            icon = Icons.Default.Bookmarks
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


// ---------- BookCard (unchanged) ----------
@Composable
fun BookCard(book: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
            BookCover(
                coverUrl = book.coverUrl,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(book.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))

            Row {
                StarRating(book.rating)
            }
            if (book.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favorite",
                    tint = Color.Yellow,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(4.dp)
                )
            }
        }
    }
}