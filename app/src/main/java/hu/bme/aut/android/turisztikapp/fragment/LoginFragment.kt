package hu.bme.aut.android.turisztikapp.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.databinding.FragmentLoginBinding
import hu.bme.aut.android.turisztikapp.extension.hideKeyboard
import hu.bme.aut.android.turisztikapp.extension.validateNonEmpty

class LoginFragment : BaseFragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding
    private var logged: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        binding = FragmentLoginBinding.bind(view)
        binding.btnRegister.setOnClickListener { registerClick() }
        binding.btnLogin.setOnClickListener { loginClick() }

        println(firebaseAuth.currentUser?.email)
        println(userEmail)

        // save(binding.checkLog.isChecked, "LoggedIn")

        binding.resetPassword.setOnClickListener {
            val email = binding.etEmail.text
            if (email.isEmpty()) {
                binding.etEmail.error = "Nincs megadva Email-cím"
                return@setOnClickListener
            } else {
                showRationaleDialog(
                    explanation = "Ha elfelejtette jelszavát, nyomjon a Küldés gombra. " +
                            "Az emailben kapott linken keresztül meg tudja változtatni jelszavát!",
                    onPositiveButton = onClickOkButton()
                )
            }

        }

        if (userEmail != null) {
            findNavController().navigate(
                R.id.action_login_to_map,
                null
            )
        } else return

    }

    private fun onClickOkButton() {
        firebaseAuth.sendPasswordResetEmail(binding.etEmail.text.toString().trim())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toast("Email elküldve")
                } else
                //toast(it.exception?.message)
                    toast("Sikertelen küldés")
            }

    }

    private fun showRationaleDialog(
        @SuppressLint("SupportAnnotationUsage") @StringRes title: String = "Új jelszó küldése",
        explanation: String?,
        onNegativeButton: () -> Unit = this::onDestroy,
        onPositiveButton: Unit

    ) {

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setCancelable(false)
            .setMessage(explanation)
            .setPositiveButton("Küldés") { dialog, id ->
                dialog.cancel()
                onPositiveButton
            }
            .setNegativeButton("Mégse") { dialog, id -> onNegativeButton() }
            .create()

        alertDialog.show()
    }

    private fun save(isChecked: Boolean, key: String) {
        val sharedPreferences: SharedPreferences =
            (activity as AppCompatActivity).getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, isChecked)
        editor.apply()
    }

    private fun load(key: String): Boolean {
        val sharedPreferences: SharedPreferences =
            (activity as AppCompatActivity).getPreferences(Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, false)
    }

    private fun validateForm() =
        binding.etEmail.validateNonEmpty() && binding.etPassword.validateNonEmpty()

    private fun registerClick() {
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        this.hideKeyboard()

        firebaseAuth
            .createUserWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
            .addOnSuccessListener {
                hideProgressDialog()

                val firebaseUser = it.user
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(firebaseUser?.email?.substringBefore('@'))
                    .build()
                firebaseUser?.updateProfile(profileChangeRequest)

                toast("Regisztráció sikeres")
                findNavController().navigate(
                    R.id.action_login_to_map,
                    null
                )
            }
            .addOnFailureListener { exception ->
                hideProgressDialog()
                toast(exception.message)
            }
    }

    private fun loginClick() {
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        this.hideKeyboard()
        firebaseAuth
            .signInWithEmailAndPassword(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
            .addOnSuccessListener {
                hideProgressDialog()

                toast("Login is successful")

                findNavController().navigate(
                    R.id.action_login_to_map,
                    null,
                    navOptions {
                        anim {
                            enter = android.R.animator.fade_in
                            exit = android.R.animator.fade_out
                        }
                    }
                )

            }
            .addOnFailureListener { exception ->
                hideProgressDialog()

                toast("Login is not successful")
            }
    }

}

