package hu.bme.aut.android.turisztikapp.fragment

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.databinding.FragmentMapBinding
import java.util.*


class MapFragment : BaseFragment(), GoogleMap.OnMarkerClickListener {

    val options = GoogleMapOptions()

    private lateinit var binding: FragmentMapBinding
    private var myMarker: Marker? = null
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val budapest = LatLng(47.497913, 19.040236)
        googleMap.addMarker(MarkerOptions().position(budapest).title("Budapest"))
        // .icon(BitmapDescriptorFactory.fromResource(R.drawable.dollar ))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(budapest))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11F))

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.setOnMapLongClickListener {
            myMarker = googleMap.addMarker(
                MarkerOptions().position(it).title("Actual position" + it.latitude + it.longitude)
            )
            findNavController().navigate(
                R.id.action_map_to_add_new_place,
                null,
                navOptions {
                    anim {
                        enter = android.R.animator.fade_in
                        exit = android.R.animator.fade_out
                    }
                }
            )

        }
        googleMap.setOnMarkerClickListener(this)


        println(userEmail + "---------------------")

        /* val geocoder: Geocoder
         val addresses: List<Address>
         geocoder = Geocoder(context, Locale.getDefault())

         addresses = geocoder.getFromLocation(
             latitude,
             longitude,
             1
         )
         val address: String =
             addresses[0].getAddressLine(0)

         val city: String = addresses[0].getLocality()
         val state: String = addresses[0].getAdminArea()
         val country: String = addresses[0].getCountryName()
         val postalCode: String = addresses[0].getPostalCode()
         val knownName: String = addresses[0].getFeatureName()*/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMapBinding.bind(view)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        binding.toolbarMap.inflateMenu(R.menu.menu_map)
        binding.toolbarMap.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.menu_map_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(
                        R.id.action_map_to_logout,
                        null
                    )
                    true
                }
                R.id.menu_map_places -> {
                    findNavController().navigate(
                        R.id.action_map_to_place_list,
                        null
                    )
                    true
                }
                R.id.menu_map_add -> {
                    findNavController().navigate(
                        R.id.action_map_to_add_new_place,
                        null
                    )
                    true
                }

                else -> true
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            if (marker.equals(myMarker))
                findNavController().navigate(
                    R.id.action_map_to_details,
                    null
                )
            println("list fragment elv--------")
        }
        return true
    }
}
