package edu.rit.ks4898.narria

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    darkThemeEnabled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            email = user.email ?: ""

            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .await()

                username = userDoc.getString("username") ?: ""
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Profile Picture",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = "Username: $username",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
                fontSize = 22.sp
            )

            Text(
                text = "Email: $email",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Dark Mode",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp
            )

            Switch(
                checked = darkThemeEnabled.value,
                onCheckedChange = { darkThemeEnabled.value = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}