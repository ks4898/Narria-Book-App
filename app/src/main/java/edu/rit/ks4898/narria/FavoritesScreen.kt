package edu.rit.ks4898.narria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun FavoritesScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var favoriteBooks by remember { mutableStateOf<List<Book>>(emptyList()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(userId) {
        if (userId != null) {
            Firebase.firestore
                .collection("users")
                .document(userId)
                .collection("books")
                .whereEqualTo("isFavorite", true)
                .addSnapshotListener { snapshot, _ ->
                    val booksList = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Book::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    favoriteBooks = booksList
                }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Favorite Books", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        if (favoriteBooks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorite books yet. Mark books as favorite to see them here.")
            }
        } else {
            LazyColumn {
                items(favoriteBooks) { book ->
                    BookListItem(book = book) {
                        navController.navigate("bookDetail/${book.id}/${book.isFavorite}")
                    }
                }
            }
        }
    }
}