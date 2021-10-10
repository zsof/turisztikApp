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

class MapFragment : Fragment(),  NavigationView.OnNavigationItemSelectedListener{

    val options = GoogleMapOptions()

    private lateinit var binding: FragmentMapBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
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
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        googleMap.uiSettings.isZoomControlsEnabled=true
        googleMap.uiSettings.isCompassEnabled=true
        googleMap.uiSettings.isMyLocationButtonEnabled=true
        googleMap.setOnMapClickListener {
            googleMap.addMarker(MarkerOptions().position(it).title("Actual position"+it.latitude))
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
        options.mapType(GoogleMap.MAP_TYPE_SATELLITE)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(true)

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
        // (activity as AppCompatActivity).setSupportActionBar(binding.fragmentMenu.appBarPosts.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ActionBarDrawerToggle(activity, binding.fragmentMenu.drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
    } // binding.fragmentMenu.appBarPosts.toolbar


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMapBinding.bind(view)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        binding.fragmentMenu.navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(
                        R.id.action_map_to_logout,
                        null
                )
            }
            R.id.menu_add_proba -> {
                findNavController().navigate(
                        R.id.action_map_to_add_new_place,
                        null
                )
            }
            R.id.menu_places ->
                findNavController().navigate(
                        R.id.action_map_to_place_list,
                        null
                )
        }

        binding.fragmentMenu.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}