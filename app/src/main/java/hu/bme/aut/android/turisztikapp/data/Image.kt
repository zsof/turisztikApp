package hu.bme.aut.android.turisztikapp.data

import java.io.Serializable
import java.util.*

data class Image(
    val id: String = "",
    val image: String? = "",
    val placeId: String = "",
    val date: Date = Date()
) : Serializable