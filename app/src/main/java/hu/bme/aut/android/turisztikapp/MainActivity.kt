package hu.bme.aut.android.turisztikapp
import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

import hu.bme.aut.android.turisztikapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
/*

        navHostFragment= supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout )
        binding.navView.setupWithNavController(navController)  //menu
        binding.toolbarMain.setupWithNavController(navController, appBarConfiguration)
*/

    }


    /*override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
              navController.navigate(
                    R.id.action_placelis_to_logout,
                    null
                )
            }
            R.id.menu_add_proba -> {
                navController.navigate(
                    R.id.action_placelist_to_add_new_place,
                    null
                )
            }
            R.id.menu_places ->
                navController.navigate(
                    R.id.action_placelist_to_place_list,
                    null
                )
            R.id.menu_map ->
                navController.navigate(
                    R.id.action_placelist_to_map,
                    null
                )
        }


        return true
    }*/
    /*   override fun onBackPressed() {
           if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
               binding.drawerLayout.closeDrawer(GravityCompat.START)
           } else {
               super.onBackPressed()
           }
       }*/

}