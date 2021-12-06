package hu.bme.aut.android.turisztikapp.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.data.Category
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.FragmentMapBinding

class MapFragment : BaseFragment(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private lateinit var locationPermRequest: ActivityResultLauncher<String>
    private lateinit var location: Location

    companion object {
        const val TAG = "MapFragment"
    }

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

        map = googleMap
        handleFineLocationPermission()
        getPlaces()
        val budapest = LatLng(47.497913, 19.040236)
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(budapest))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11F))

        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true

        googleMap.setOnMapLongClickListener {
            val action =
                MapFragmentDirections.actionMapToAddNewPlace(latLng = it)
            findNavController().navigate(action)
        }

        googleMap.setOnMarkerClickListener {
            var itPlace = it.tag as Place
            var distance = getDistance(itPlace)
            toast(String.format("%.1f", distance) + " km")
            false
        }

        googleMap.setOnInfoWindowClickListener {
            val actionToDetails = MapFragmentDirections.actionMapToDetails(it.tag as Place)
            findNavController().navigate(actionToDetails)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermRequest =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    toast(getString(R.string.permission_granted))
                    Log.d(TAG, "Permission granted for location data")
                    enableMyLocation()
                } else {
                    toast(getString(R.string.permission_denied))
                    Log.d(TAG, "Permission denied for location data")
                }
            }
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
                }
                R.id.menu_map_settings -> {
                    findNavController().navigate(
                        R.id.action_map_to_settings,
                        null
                    )
                }
                R.id.menu_map_places -> {
                    findNavController().navigate(
                        R.id.action_map_to_place_list,
                        null
                    )
                }
            }
            true
        }
    }

    private fun getPlaces() {
        if (!::map.isInitialized)
            return
        Firebase.firestore.collection("places").get().addOnSuccessListener {
            for (dc in it.documents) {
                val place = dc.toObject<Place>()
                place ?: continue

                val marker =
                    when (place.category) {
                        (Category.Museum) -> {
                            map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        place.geoPoint.latitude,
                                        place.geoPoint.longitude
                                    )
                                ).title(place.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_museum))
                            )
                        }
                        (Category.Castle) -> {
                            map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        place.geoPoint.latitude,
                                        place.geoPoint.longitude
                                    )
                                ).title(place.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_castle))
                            )
                        }
                        (Category.ArtGallery) -> {
                            map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        place.geoPoint.latitude,
                                        place.geoPoint.longitude
                                    )
                                ).title(place.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_artgallery))
                            )
                        }
                        (Category.Library) -> {
                            map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        place.geoPoint.latitude,
                                        place.geoPoint.longitude
                                    )
                                ).title(place.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_library))
                            )
                        }
                        (Category.Zoo) -> {
                            map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        place.geoPoint.latitude,
                                        place.geoPoint.longitude
                                    )
                                ).title(place.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_zoo))
                            )
                        }
                        (Category.Church) -> {
                            map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        place.geoPoint.latitude,
                                        place.geoPoint.longitude
                                    )
                                ).title(place.name)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_church))
                            )
                        }
                        else -> {
                            map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        place.geoPoint.latitude,
                                        place.geoPoint.longitude
                                    )
                                ).title(place.name)
                            )
                        }
                    }
                marker?.tag = place
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) {
            return
        }
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
    }

    private fun handleFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity as AppCompatActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                showRationaleDialog(
                    explanation = R.string.explanation_permission_location,
                    onPositiveButton = this::requestFineLocationPermission
                )
            } else {
                requestFineLocationPermission()
            }
        } else {
            enableMyLocation()
        }
    }

    private fun showRationaleDialog(
        title: String = getString(R.string.attention),
        explanation: Int,
        onPositiveButton: () -> Unit,
        onNegativeButton: () -> Unit = this::onDestroy
    ) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(explanation)
            .setPositiveButton(getString(R.string.ok_permisson_dialog_map)) { dialog, _ ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton(getString(R.string.exit_permission_diaog_map)) { _, _ -> onNegativeButton() }
            .create()
        alertDialog.show()
    }

    private fun requestFineLocationPermission() {
        locationPermRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getDistance(place: Place): Float {
        location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = place.geoPoint.latitude
        location.longitude = place.geoPoint.longitude

        return location.distanceTo(map.myLocation) / 1000
    }
}