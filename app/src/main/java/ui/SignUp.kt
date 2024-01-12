
import android.content.ContentResolver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch


@Composable
fun SignUpScreen(

    navigateToLogin: () -> Unit,
    contentResolver: ContentResolver

) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Sign Up to create your account",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = it.isEmpty() },
            label = { Text("Name") },
            isError = nameError,
            modifier = Modifier
                .padding(5.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(5.dp))
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = it.isEmpty() },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError,
            modifier = Modifier
                .padding(5.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(5.dp))
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = it.isEmpty() },
            visualTransformation = PasswordVisualTransformation(),
            label = { Text("Password") },
            isError = passwordError,
            modifier = Modifier
                .padding(5.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(5.dp))
        )
        val firestore = FirebaseFirestore.getInstance()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()


        Button(
            onClick = {
                if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    if (!isEmailValid(email)) {
                        coroutineScope.launch {
                            showEmailFormatError(snackbarHostState)
                        }
                        return@Button
                    }

                    if (isWeakPassword(password)) {
                        coroutineScope.launch {
                            showWeakPasswordError(snackbarHostState)
                        }
                        return@Button
                    }

                    val auth = FirebaseAuth.getInstance()
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = hashMapOf(
                                    "name" to name,
                                    "email" to email
                                )

                                firestore.collection("users")
                                    .add(user)
                                    .addOnSuccessListener {
                                        coroutineScope.launch{
                                            snackbarHostState.showSnackbar("User registered and document written successfully")
                                        }
                                        navigateToLogin()
                                    }
                                    .addOnFailureListener { e ->
                                        if (e is FirebaseFirestoreException) {
                                            coroutineScope.launch {
                                                showFirestoreError(snackbarHostState, e)
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                showUnknownError(snackbarHostState)
                                            }
                                        }
                                    }
                            } else {
                                if (name.isEmpty()) nameError = true
                                if (email.isEmpty()) emailError = true
                                if (password.isEmpty()) passwordError = true
                            }
                        }
                }
            },
            modifier = Modifier
                .width(100.dp)
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            Text(text = "Sign Up")
        }

        TextButton(onClick = {
            navigateToLogin()
        }) {
            Text(text = "Back to Login")
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

fun isEmailValid(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}

fun isWeakPassword(password: String): Boolean {
    return password.length < 6
}

suspend fun showEmailFormatError(snackbarHostState: SnackbarHostState) {
    snackbarHostState.showSnackbar("Invalid email format")
}

suspend fun showWeakPasswordError(snackbarHostState: SnackbarHostState) {
    snackbarHostState.showSnackbar("Weak password. Password should be at least 6 characters long")
}

suspend fun showFirestoreError(snackbarHostState: SnackbarHostState, e: FirebaseFirestoreException) {
    snackbarHostState.showSnackbar("Firestore error: ${e.message}")
}

suspend fun showUnknownError(snackbarHostState: SnackbarHostState) {
    snackbarHostState.showSnackbar("Unknown error occurred")
}