package hu.bme.aut.android.turisztikapp.data

import java.io.Serializable
import java.util.*

data class Comment(
    val id: String = "",
    val placeId: String = "",
    var userId: String? = null,
    var userName: String? = "",
    val comment: String = "",
    val date: Date = Date()
) : Serializable
