package hu.bme.aut.android.turisztikapp.data

import java.io.Serializable

data class Image(
    val id: String = "",
    val image: String? = "",
    val placeId: String = ""
) : Serializable