package edu.rit.ks4898.narria

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Book>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Search Books",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (showError) {
            Text(
                text = "Error adding book. Please try again.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    showError = false
                },
                label = { Text("Search by title or author") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (searchQuery.isNotEmpty()) {
                        isSearching = true
                        coroutineScope.launch {
                            try {
                                val response = GoogleBooksApi.service.searchBooks(
                                    query = "intitle:$searchQuery inauthor:$searchQuery"
                                )
                                searchResults = response.items?.map { item ->
                                    Book(
                                        bookId = item.id,
                                        title = item.volumeInfo.title,
                                        author = item.volumeInfo.authors?.joinToString(", ") ?: "Unknown",
                                        description = item.volumeInfo.description ?: "No description available",
                                        coverUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                                        rating = ((item.volumeInfo.averageRating ?: 0f))
                                    )
                                } ?: emptyList()
                            } catch (e: Exception) {
                                searchResults = emptyList()
                            } finally {
                                isSearching = false
                            }
                        }
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (searchResults.isNotEmpty()) {
            LazyColumn {
                items(searchResults) { book ->
                    SearchResultItem(book = book, onAddClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .collection("books")
                                .add(book)
                                .addOnSuccessListener { showError = false }
                                .addOnFailureListener { showError = true }
                        }
                    }, onItemClick = {
                        navController.navigate("bookDetail/${book.bookId}/false")
                    })
                }
            }
        } else if (searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results found")
            }
        }
    }
}

@Composable
fun SearchResultItem(book: Book, onAddClick: () -> Unit, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCover(
                coverUrl = book.coverUrl,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                Text(text = book.author, style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRating(book.rating)
                    Spacer(Modifier.width(4.dp))
                    Text(String.format("%.1f", book.rating))
                }
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(onClick = onAddClick) {
                Text("Add")
            }
        }
    }
}