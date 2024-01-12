import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material3.Icon
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream


suspend fun uploadImage(contentResolver: ContentResolver, uri: Uri): String? {
    val storageReference =
        FirebaseStorage.getInstance().reference.child("images/${uri.lastPathSegment}")

    val tempFile = File.createTempFile("image", ".jpg")
    val inputStream = contentResolver.openInputStream(uri)
    val outputStream = FileOutputStream(tempFile)
    inputStream?.use { input ->
        outputStream.use { output ->
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()
        }
    }

    if (!tempFile.exists()) {
        Log.e("TAG", "Temporary file does not exist")
        return null
    }

    val convertedUri = Uri.fromFile(tempFile)

    return try {
        val uploadTask = storageReference.putFile(convertedUri)
        uploadTask.addOnFailureListener {
            Log.e("TAG", "Error uploading image: ${it.message}")
        }

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw Exception("Upload failed")
            }
            storageReference.downloadUrl
        }

        val url = urlTask.await()
        if (tempFile.exists()) {
            tempFile.delete()
        }
        url?.toString()
    } finally {

    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewPlaceScreen(
    onAddPlace: (Place) -> Unit,
    navController: NavController,
    contentResolver: ContentResolver
) {
    val storage = Firebase.storage
    var imageUri: Uri? by remember { mutableStateOf(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Add New Place", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") }
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") }
            )

            Row {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Star $i",
                        tint = if (i <= rating) Color.Yellow else Color.LightGray,
                        modifier = Modifier.clickable {
                            rating = i.toFloat()
                        }
                    )
                }
            }

            val pickImage =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val uri = result.data?.data
                        imageUri = uri
                    }
                }

            Button(onClick = {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                pickImage.launch(intent)
            }) {
                Text("Select Image")
            }

            val coroutineScope = rememberCoroutineScope()

            Button(onClick = {
                coroutineScope.launch {
                    val imageUrl = imageUri?.let { uploadImage(contentResolver, it) }
                    if (imageUrl != null) {
                        val place = Place(name, description, address, rating, imageUrl)
                        onAddPlace(place)
                        addPlaceToFirestore(place, imageUri)
                        navController.popBackStack()
                    } else {

                    }
                }
            }) {
                Text("Save")
            }
        }
    }
}

fun addPlaceToFirestore(place: Place, imageUri: Uri?) {
    val db = FirebaseFirestore.getInstance()
    val imageUrl = imageUri?.toString() // Assuming imageUrl is a string

    val newPlace = hashMapOf(
        "name" to place.name,
        "description" to place.description,
        "address" to place.address,
        "rating" to place.rating,
        "imageUrl" to imageUrl // Save the image URL here
    )

    db.collection("places")
        .add(newPlace)
        .addOnSuccessListener { documentReference ->
            println("DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            println("Error adding document: $e")
        }
}


data class Place(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    var rating: Float = 0f,
    val imageUrl: String? = null
) {

    constructor() : this(
        "",
        "",
        "",
        0f,
        null
    )

}