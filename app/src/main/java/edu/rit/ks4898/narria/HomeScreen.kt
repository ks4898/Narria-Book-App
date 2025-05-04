package edu.rit.ks4898.narria

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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

    LaunchedEffect(key1 = true) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("books")
                    .get()
                    .await()

                val booksList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Book::class.java)?.copy(
                        id = doc.id,
                        bookId = doc.getString("bookId") ?: ""
                    )
                }

                books = booksList
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Books",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (books.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No books added yet. Search for books to add them.")
            }
        } else {
            LazyColumn {
                item {
                    Text(
                        text = "Reading Now",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val readingBooks = books.filter { it.readingStatus == "Reading" }
                    if (readingBooks.isEmpty()) {
                        Text("No books in this category")
                    } else {
                        LazyRow {
                            items(readingBooks) { book ->
                                BookCard(book = book) {
                                    navController.navigate("bookDetail/${book.id}/${book.isFavorite}")
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "To Read",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val toReadBooks = books.filter { it.readingStatus == "To-Read" }
                    if (toReadBooks.isEmpty()) {
                        Text("No books in this category")
                    } else {
                        LazyRow {
                            items(toReadBooks) { book ->
                                BookCard(book = book) {
                                    navController.navigate("bookDetail/${book.id}/${book.isFavorite}")
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val completedBooks = books.filter { it.readingStatus == "Completed" }
                    if (completedBooks.isEmpty()) {
                        Text("No books in this category")
                    } else {
                        LazyRow {
                            items(completedBooks) { book ->
                                BookCard(book = book) {
                                    navController.navigate("bookDetail/${book.id}/${book.isFavorite}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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