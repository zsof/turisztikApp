package hu.bme.aut.android.turisztikapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.databinding.FragmentLoginBinding
import hu.bme.aut.android.turisztikapp.extension.hideKeyboard
import hu.bme.aut.android.turisztikapp.extension.validateNonEmpty

class LoginFragment : BaseFragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding

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

        binding.resetPassword.setOnClickListener {
            val email = binding.etEmail.text
            if (email.isEmpty()) {
                binding.etEmail.error = getString(R.string.empty_email_login)
                return@setOnClickListener
            } else {
                showRationaleDialog(
                    explanation = getString(R.string.forgot_password_login),
                    onPositiveButton = this::onClickOkButton
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
                    toast(getString(R.string.sent_email_login))
                } else
                    toast(getString(R.string.failed_sent_email_login))
            }

    }

    private fun showRationaleDialog(
        title: String = getString(R.string.new_password_sentding_login),
        explanation: String?,
        onNegativeButton: () -> Unit = this::onDestroy,
        onPositiveButton: () -> Unit

    ) {

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setCancelable(false)
            .setMessage(explanation)
            .setPositiveButton(getString(R.string.send_login)) { dialog, _ ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton(getString(R.string.cancel_login)) { _, _ -> onNegativeButton() }
            .create()

        alertDialog.show()
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

                toast(getString(R.string.registration_successful_login))
                findNavController().navigate(
                    R.id.action_login_to_map,
                    null
                )
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                toast(e.localizedMessage)
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
                toast(getString(R.string.login_successful_login))

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
            .addOnFailureListener { e ->
                hideProgressDialog()
                toast(e.localizedMessage)
            }
    }
}