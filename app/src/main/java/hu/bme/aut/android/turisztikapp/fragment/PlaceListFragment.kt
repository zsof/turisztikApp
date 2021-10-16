package hu.bme.aut.android.turisztikapp.fragment

import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.adapter.PlaceListAdapter
import hu.bme.aut.android.turisztikapp.data.Category
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.FragmentPlaceListBinding


class PlaceListFragment : BaseFragment(),
    OnNavigationItemSelectedListener/*, PlaceListAdapter.OnItemCLickListener */ {

    private lateinit var binding: FragmentPlaceListBinding
    private lateinit var placeListAdapter: PlaceListAdapter
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_list, container, false)

        // (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)   //ne legyen pötty


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlaceListBinding.bind(view)

        placeListAdapter = PlaceListAdapter()
        binding.placeList.layoutManager = LinearLayoutManager(context).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        binding.placeList.adapter = placeListAdapter


        val dividerItemDecoration = DividerItemDecoration(  //recyclerview row-ok közötti elválasztó
            binding.placeList.context,
            (binding.placeList.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.setDrawable(context?.getDrawable(R.drawable.recyclerview_divider)!!)
        binding.placeList.addItemDecoration(dividerItemDecoration)

        navHostFragment =
            (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navView.setNavigationItemSelectedListener(this)

        initPostsListener()

    }


    private fun initPostsListener() {
        val db = Firebase.firestore
        db.collection("places")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    toast(e.toString())
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> placeListAdapter.addPlace(dc.document.toObject<Place>())
                        DocumentChange.Type.MODIFIED -> toast(dc.document.data.toString())  //TODO
                        DocumentChange.Type.REMOVED -> placeListAdapter.removePlace(dc.document.toObject())
                    }
                }
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(
                    R.id.action_placelis_to_logout,
                    null
                )
            }
            R.id.menu_add_proba -> {
                findNavController().navigate(
                    R.id.action_placelist_to_add_new_place,
                    null
                )
            }
            R.id.menu_places ->
                findNavController().navigate(
                    R.id.action_placelist_to_place_list,
                    null
                )
            R.id.menu_map ->
                findNavController().navigate(
                    R.id.action_placelist_to_map,
                    null
                )
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    /* override fun onBackPressed() {
         if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
             binding.drawerLayout.closeDrawer(GravityCompat.START)
         } else {
             super.onBackPressed()
         }
     }*/
}

