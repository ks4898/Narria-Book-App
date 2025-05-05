package edu.rit.ks4898.narria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import edu.rit.ks4898.narria.ui.theme.NarriaTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val initialDarkTheme = isSystemInDarkTheme()
            val initialLoginState = FirebaseAuth.getInstance().currentUser != null

            val darkThemeEnabled = remember { mutableStateOf(initialDarkTheme) }
            val isLoggedIn = remember { mutableStateOf(initialLoginState) }

            NarriaTheme(darkTheme = darkThemeEnabled.value) {
                AppNavigation(
                    isLoggedIn = isLoggedIn.value,
                    onLoginSuccess = { isLoggedIn.value = true },
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        isLoggedIn.value = false
                    },
                    darkThemeEnabled = darkThemeEnabled
                )
            }
        }
    }
}