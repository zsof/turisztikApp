package hu.bme.aut.android.turisztikapp.data


import androidx.room.Entity
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

//@NoArg
data class Place(
    val id: String = "",
    val name: String = "",
    val address: String = "",

    val geoPoint: GeoPoint = GeoPoint(47.497913, 19.040236),
    val description: String = "",
    val rate: Float? = 2F,
    val image: String? = "",
    val category: Category? = Category.Museum
) : Serializable

//annotation class NoArg
