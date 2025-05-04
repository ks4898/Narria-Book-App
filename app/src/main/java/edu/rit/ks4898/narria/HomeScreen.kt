package edu.rit.ks4898.narria

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val booksState = remember { mutableStateListOf<Book>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("books")
                    .get()
                    .await()

                booksState.clear()
                booksState.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Book::class.java)?.copy(
                        id = doc.id,
                        bookId = doc.getString("bookId") ?: ""
                    )
                })
            } finally { isLoading = false }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("My Books", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                booksState.isEmpty() -> EmptyState(
                    message = "Your library is empty.\nAdd a book from the Search tab!",
                    icon = Icons.Filled.Book
                )

                else -> LazyColumn {
                    item { CategorySection("Reading Now", booksState.filter { it.readingStatus == "Reading" }, navController, booksState) }
                    item { CategorySection("To Read", booksState.filter { it.readingStatus == "To-Read" }, navController, booksState) }
                    item { CategorySection("Completed", booksState.filter { it.readingStatus == "Completed" }, navController, booksState) }
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    books: List<Book>,
    navController: NavHostController,
    allBooks: MutableList<Book>
) {
    Text(title, style = MaterialTheme.typography.titleLarge)

    if (books.isEmpty()) {
        InlineCardEmptyState(
            message = "No books in this category",
            icon = Icons.Filled.Bookmarks
        )
    } else {
        LazyRow {
            items(books, key = { it.id }) { book ->
                var dismissed by remember { mutableStateOf(false) }

                AnimatedVisibility(
                    visible = !dismissed,
                    exit = fadeOut()
                ) {
                    BookCardWithDelete(
                        book = book,
                        onClick = { navController.navigate("bookDetail/${book.id}/${book.isFavorite}") },
                        onDeleteConfirmed = {
                            dismissed = true
                            removeBook(book)
                            allBooks.remove(book)
                        }
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCardWithDelete(
    book: Book,
    onClick: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to remove this book?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConfirmed()
                    showConfirmDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showConfirmDialog = true }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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

            Spacer(Modifier.height(8.dp))

            Text(book.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))

            StarRating(rating = book.rating, starSize = 16.dp)

            if (book.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = Color.Red,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

private fun removeBook(book: Book) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("books")
        .document(book.id)
        .delete()
}