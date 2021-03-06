package hu.bme.aut.android.turisztikapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import hu.bme.aut.android.turisztikapp.databinding.ActivityMainBinding
import hu.bme.aut.android.turisztikapp.fragment.BaseFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private val Context.isConnected: Boolean
        @SuppressLint("ServiceCast")
        get() {
            return (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                .activeNetworkInfo?.isConnected == true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!isConnected) {
            Toast.makeText(
                this,
                "Nincs internet kapcsolat, bizonyos funkciók nem működnek!",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    override fun onBackPressed() {
        val f = currentFragment
        when {
            f !is BaseFragment || f.onBackPressed() -> {
                if (!findNavController(R.id.nav_host_fragment).navigateUp()) {
                    super.onBackPressed()
                }
            }
        }
    }

    private val currentFragment: Fragment?
        get() = navHostFragment.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
}