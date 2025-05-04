package edu.rit.ks4898.narria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
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

                books = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Book::class.java)?.copy(
                        id = doc.id,
                        bookId = doc.getString("bookId") ?: ""
                    )
                }
            } finally { isLoading = false }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Books", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            books.isEmpty() -> EmptyState(
                message = "Your library is empty.\nAdd a book from the Search tab!",
                icon = Icons.Filled.Book
            )

            else -> LazyColumn {
                item { CategorySection("Reading Now", books.filter { it.readingStatus == "Reading" }, navController) }
                item { CategorySection("To Read", books.filter { it.readingStatus == "To-Read" }, navController) }
                item { CategorySection("Completed", books.filter { it.readingStatus == "Completed" }, navController) }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CategorySection(
    title: String,
    books: List<Book>,
    navController: NavHostController
) {
    var bookList by remember { mutableStateOf(books) }

    Text(title, style = MaterialTheme.typography.titleLarge)

    if (bookList.isEmpty()) {
        InlineCardEmptyState(
            message = "No books in this category",
            icon = Icons.Filled.Bookmarks
        )
    } else {
        LazyRow {
            itemsIndexed(bookList, key = { _, book -> book.id }) { index, book ->
                var dismissed by remember { mutableStateOf(false) }

                if (!dismissed) {
                    SwipeToDismiss(
                        state = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                                    dismissed = true
                                    removeBook(book)
                                    // Remove book from local list
                                    bookList = bookList.toMutableList().also { it.remove(book) }
                                    true
                                } else false
                            }
                        ),
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        },
                        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart)
                    ) {
                        BookCard(
                            book = book,
                            onClick = {
                                navController.navigate("bookDetail/${book.id}/${book.isFavorite}")
                            }
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
}

@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp),
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