package edu.rit.ks4898.narria

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun FavoritesScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    var favoriteBooks by remember { mutableStateOf<List<Book>>(emptyList()) }

    // ───────────────── Listen for favourite changes ─────────────────
    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            Firebase.firestore
                .collection("users")
                .document(uid)
                .collection("books")
                .whereEqualTo("isFavorite", true)
                .addSnapshotListener { snap, _ ->
                    favoriteBooks = snap?.documents?.mapNotNull { d ->
                        d.toObject(Book::class.java)?.copy(id = d.id)
                    } ?: emptyList()
                }
        }
    }

    // ───────────────── UI ─────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Favorite Books", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (favoriteBooks.isEmpty()) {
            EmptyState(
                message = "You haven't marked any books as favorites yet.",
                icon    = Icons.Filled.Favorite
            )
        } else {
            LazyColumn {
                items(favoriteBooks) { book ->
                    BookListItem(book) {
                        navController.navigate("bookDetail/${book.id}/${book.isFavorite}")
                    }
                }
            }
        }
    }
}