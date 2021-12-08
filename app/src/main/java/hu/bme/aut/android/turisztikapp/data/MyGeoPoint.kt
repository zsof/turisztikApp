package hu.bme.aut.android.turisztikapp.data

import java.io.Serializable

data class MyGeoPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Serializable