package hu.bme.aut.android.turisztikapp.data

import java.io.Serializable

data class Place(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val geoPoint: MyGeoPoint = MyGeoPoint(),
    val description: String = "",
    val rate: Float? = 2F,
    val image: String? = "",
    val category: Category? = Category.Museum
) : Serializable