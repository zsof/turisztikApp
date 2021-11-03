package hu.bme.aut.android.turisztikapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import hu.bme.aut.android.turisztikapp.databinding.ActivityMainBinding
import hu.bme.aut.android.turisztikapp.fragment.BaseFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    override fun onBackPressed() {
        val f = currentFragment
        if (f !is BaseFragment || f.onBackPressed()) {
            if (!findNavController(R.id.nav_host_fragment).navigateUp()) {
                super.onBackPressed()
            }
        }
    }

    private val currentFragment: Fragment?
        get() = navHostFragment.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
}