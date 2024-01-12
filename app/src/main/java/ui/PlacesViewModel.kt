
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class PlacesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _places = MutableLiveData<List<Place>>()

    val places: LiveData<List<Place>> = _places.also {
        getPlaces()
    }


    private fun getPlaces() {
        db.collection("places").get()
            .addOnSuccessListener {
                _places.value = it.toObjects(Place::class.java)
            }
            .addOnFailureListener{
                Log.e(TAG, "Error getting documents: $it")
            }
    }

    fun refresh() {
        getPlaces()
    }
}
