package hu.bme.aut.android.turisztikapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.adapter.PlaceListAdapter
import hu.bme.aut.android.turisztikapp.databinding.FragmentPlaceListBinding
import hu.bme.aut.android.turisztikapp.databinding.NavHeaderMainBinding

class PlaceListFragment : BaseFragment(),
    OnNavigationItemSelectedListener {
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
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlaceListBinding.bind(view)

        placeListAdapter = PlaceListAdapter()
        binding.placeList.layoutManager = LinearLayoutManager(context)
        binding.placeList.adapter = placeListAdapter

        val dividerItemDecoration = DividerItemDecoration(
            binding.placeList.context,
            (binding.placeList.layoutManager as LinearLayoutManager).orientation
        )
        context?.getDrawable(R.drawable.recyclerview_divider)
            ?.let { dividerItemDecoration.setDrawable(it) }
        binding.placeList.addItemDecoration(dividerItemDecoration)

        initPostsListener()
        setToolbar()
    }

    private fun setToolbar() {
        navHostFragment =
            (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navView.setNavigationItemSelectedListener(this)
        val header = binding.navView.getHeaderView(0)
        val headerBinding = NavHeaderMainBinding.bind(header)
        headerBinding.nameTextNavHeader.text = "Üdv, $userName!"
        Glide.with(this)
            .load(FirebaseAuth.getInstance().currentUser?.photoUrl)
            .placeholder(R.drawable.ic_profile)
            .into(
                headerBinding.imageViewNavHeader
            )

        binding.toolbar.inflateMenu(R.menu.search_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    val searchView = it.actionView as SearchView
                    it.expandActionView()
                    searchView.queryHint = getString(R.string.search)
                    searchView.isIconified = false

                    searchView.setOnQueryTextListener(
                        object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                placeListAdapter.filter.filter(newText)
                                return true
                            }
                        }
                    )
                }
                R.id.refresh -> {
                    val actionToList =
                        PlaceListFragmentDirections.actionPlacelistToPlaceList()
                    findNavController().navigate(actionToList)
                }
            }
            true
        }
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
                        DocumentChange.Type.ADDED -> placeListAdapter.addPlace(dc.document.toObject())
                        DocumentChange.Type.MODIFIED -> toast(dc.document.data.toString())
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
            R.id.menu_settings ->
                findNavController().navigate(
                    R.id.action_placelist_to_settings,
                    null
                )
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}