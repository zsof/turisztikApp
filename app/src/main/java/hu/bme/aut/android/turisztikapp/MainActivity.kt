package hu.bme.aut.android.turisztikapp


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import hu.bme.aut.android.turisztikapp.databinding.ActivityMainBinding
import hu.bme.aut.android.turisztikapp.fragment.BaseFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    /* private val firebaseUser: FirebaseUser?
         get() = FirebaseAuth.getInstance().currentUser

     protected val userEmail: String?
         get() = firebaseUser?.email

     private fun setNavigationGraph(){
         navHostFragment= supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
         navController = navHostFragment.navController

         val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
         navGraph.startDestination =
             if (userEmail==null) {
                 R.id.fragmentLogin
             } else {
                 R.id.map
             }

         navController.graph = navGraph
     }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        //   setNavigationGraph()

    }

    override fun onBackPressed() {  //visszagomb
        val f = currentFragment
        if (f !is BaseFragment || f.onBackPressed()) {
            if (!findNavController(R.id.nav_host_fragment).navigateUp()) {
                super.onBackPressed()
            }
        }
    }

    val currentFragment: Fragment? //visszagomb
        get() = navHostFragment.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
}