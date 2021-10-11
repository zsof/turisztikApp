package hu.bme.aut.android.turisztikapp.fragment

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.databinding.FragmentMapBinding

class MapFragment : BaseFragment() {

    val options = GoogleMapOptions()

    private lateinit var binding: FragmentMapBinding
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(budapest))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11F))
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.setOnMapLongClickListener {
            googleMap.addMarker(
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
        println(userEmail + "---------------------")
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
        binding.toolbar.inflateMenu(R.menu.menu_map)
        binding.toolbar.setOnMenuItemClickListener {

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
}
