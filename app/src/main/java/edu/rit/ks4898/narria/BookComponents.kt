package edu.rit.ks4898.narria

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BookCover(coverUrl: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = coverUrl.ifEmpty { "https://via.placeholder.com/150" },
        contentDescription = "Book Cover",
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(4.dp))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(bookId: String, initialIsFavorite: Boolean, navController: NavHostController) {
    var book by remember { mutableStateOf<Book?>(null) }
    var isFavorite by remember { mutableStateOf(initialIsFavorite) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    LaunchedEffect(bookId) {
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("books")
                .document(bookId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("BookDetail", "Listen failed", error)
                        return@addSnapshotListener
                    }

                    val bookData = snapshot?.toObject(Book::class.java)?.copy(id = snapshot.id)
                    book = bookData

                    if (snapshot != null && snapshot.contains("isFavorite")) {
                        isFavorite = snapshot.getBoolean("isFavorite") == true
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userId?.let { uid ->
                                val newFavorite = !isFavorite
                                isFavorite = newFavorite

                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .collection("books")
                                    .document(bookId)
                                    .update("isFavorite", newFavorite)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }

                    IconButton(
                        onClick = {
                            book?.let {
                                shareBook(context, it.copy(description = it.description.ifEmpty { "No description available" }))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            book?.let { currentBook ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    BookCover(
                        coverUrl = currentBook.coverUrl,
                        modifier = Modifier
                            .height(200.dp)
                            .width(130.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = currentBook.title,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp)) // ← added space

                        Text(
                            text = currentBook.author,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp)) // ← added space before rating

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRating(currentBook.rating, starSize = 32.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                    }

                }

                Text("Description", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
                Text(currentBook.description.ifEmpty { "No description available" }, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Reading Status", style = MaterialTheme.typography.titleLarge)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ReadingStatusButton("To-Read", currentBook.readingStatus == "To-Read") {
                        updateReadingStatus(bookId, "To-Read")
                    }
                    ReadingStatusButton("Reading", currentBook.readingStatus == "Reading") {
                        updateReadingStatus(bookId, "Reading")
                    }
                    ReadingStatusButton("Completed", currentBook.readingStatus == "Completed") {
                        updateReadingStatus(bookId, "Completed")
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ReadingStatusButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(text)
    }
}

private fun updateReadingStatus(bookId: String, status: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("books")
            .document(bookId)
            .update("readingStatus", status)
    }
}

fun shareBook(context: Context, book: Book) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Check out this book: ${book.title}")
        putExtra(Intent.EXTRA_TEXT, "I'm reading ${book.title}. It's rated ${String.format("%.0f", book.rating)}/5!")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}

@Composable
fun BookListItem(book: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(coverUrl = book.coverUrl, modifier = Modifier.height(100.dp).width(70.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = book.author, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row {
                    StarRating(book.rating, starSize = 16.dp)
                }
                Text(text = "Status: ${book.readingStatus}", style = MaterialTheme.typography.bodySmall)
            }

            if (book.isFavorite) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Favorite", tint = Color.Red, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
fun StarRating(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    starColor: Color = MaterialTheme.colorScheme.secondary,
    emptyStarColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val filled = rating.toInt().coerceIn(0, 5)
        repeat(5) { index ->
            Icon(
                imageVector = if (index < filled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < filled) starColor else emptyStarColor,
                modifier = Modifier.size(starSize)
            )
        }
    }
}
