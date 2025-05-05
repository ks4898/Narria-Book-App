package edu.rit.ks4898.narria

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    darkThemeEnabled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    var username by remember { mutableStateOf<String?>(null) }
    var bookCount by remember { mutableStateOf(0) }
    var favoriteCount by remember { mutableStateOf(0) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            username = userDoc.getString("username")

            val booksSnapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("books")
                .get()
                .await()

            bookCount = booksSnapshot.size()
            favoriteCount = booksSnapshot.documents.count { it.getBoolean("isFavorite") == true }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile Icon",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = username ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))


        Text(
            text = user?.email ?: "Unknown User",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your Stats", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Books added: $bookCount")
                Text("Favorite books: $favoriteCount")

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = darkThemeEnabled.value,
                        onCheckedChange = { darkThemeEnabled.value = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Account deleted.", Toast.LENGTH_SHORT).show()
                        onLogout()
                    } else {
                        Toast.makeText(context, "Error deleting account.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Delete Account", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}