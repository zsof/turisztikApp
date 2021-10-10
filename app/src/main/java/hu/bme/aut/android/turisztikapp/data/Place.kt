package hu.bme.aut.android.turisztikapp.data

data class Place(
        val id: String = "",
        // var userId: String?,
        //   var author: String?,
        val name: String = "",
        val address: String = "",
        val description: String = "",
        val rate: Float? = 2F,
        val image: String? = ""
)
