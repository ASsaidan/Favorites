import android.annotation.SuppressLint
import android.content.ContentResolver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.favorites.R
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToSignUp: () -> Unit,
    navigateToMain: () -> Unit,
    contentResolver: ContentResolver
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Save your favorite places in one spot!",
                textAlign = TextAlign.Center,
                fontSize = 25.sp,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(32.dp))

            RedesignedImageComponent()

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(5.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(5.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navigateToMain()
                                } else {
                                    if (email.isEmpty()) emailError = true
                                    if (password.isEmpty()) passwordError = true
                                    showError = true
                                    errorMessage = "Authentication failed"
                                }
                            }
                    } else {
                        showError = true
                        errorMessage = "Please fill in both fields"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Blue)
            ) {
                Text(
                    "Log In",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            if (showError) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showError = false }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Not registered? ")
                TextButton(
                    onClick = {
                        navigateToSignUp()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Blue
                    )
                ) {
                    Text(
                        "Sign up here",
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}

@Composable
fun RedesignedImageComponent() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.placeshome),
            contentDescription = null,
            modifier = Modifier.size(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}
