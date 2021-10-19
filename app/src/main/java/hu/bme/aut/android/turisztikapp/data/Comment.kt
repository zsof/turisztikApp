package hu.bme.aut.android.turisztikapp.data

import java.io.Serializable

data class Comment(
    val id: String = "",
    val placeId: String = "",
    // var userId: String?,
    //   var author: String?,
    val comment: String = "",
    //  val rate: Float? = 2F
) : Serializable
