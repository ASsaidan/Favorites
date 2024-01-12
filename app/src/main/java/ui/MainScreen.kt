package ui

import Place
import PlacesViewModel
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigateToAddPlace: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    placesViewModel: PlacesViewModel = viewModel(),
    contentResolver: ContentResolver
) {
    val places by placesViewModel.places.observeAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToAddPlace) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
            placesViewModel.refresh()

        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(places) { place ->
                PlaceCard(
                    contentResolver = contentResolver,
                    place = place,
                    onClick = { onPlaceClick(place) })
                Divider()

            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCard(
    contentResolver: ContentResolver,
    place: Place,
    onClick: () -> Unit
) {
    var currentRating by remember { mutableStateOf(place.rating) }


    Card(
        onClick = onClick,

    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .width(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.headlineSmall
            )


            Text(
                text = place.description,
                textAlign = TextAlign.Justify
            )

            Text(text = "Address: ${place.address}")


            RatingBar(
                rating = currentRating,
                onRatingChange = {
                    currentRating = it
                    place.rating = it
                }
            )


            NetworkImage(url = place.imageUrl ?: "")


        }
    }
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit
) {

    Row {
        for (i in 1..5) {
            val isStarFilled = remember { i <= rating }
            IconButton(onClick = { onRatingChange(i.toFloat()) }) {
                Icon(
                    imageVector = if (isStarFilled) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.Star
                    },
                    contentDescription = "Rate $i",
                    tint = if (isStarFilled) Color.Yellow else Color.LightGray,
                )
            }
        }
    }
}


@Composable
fun NetworkImage(url: String) {
    val image: MutableState<Bitmap?> = remember { mutableStateOf(null) }
    val context = LocalContext.current
    val glide = remember(context) { Glide.with(context) }

    DisposableEffect(url) {
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                image.value = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                image.value = null
            }
        }
        glide
            .asBitmap()
            .load(url)
            .into(target)

        onDispose {
            glide.clear(target)
            image.value = null
        }
    }

    image.value?.let {
        Image(bitmap = it.asImageBitmap(), contentDescription = null)
    }
}





















