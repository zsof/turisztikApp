package hu.bme.aut.android.turisztikapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.databinding.FragmentSettingsBinding


class SettingsFragment : BaseFragment() {

    companion object {
        const val REQUEST_CODE_CAMERA = 100
        const val REQUEST_CODE_GALLERY = 101
    }

    private lateinit var binding: FragmentSettingsBinding
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var updates: UserProfileChangeRequest

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        currentUser?.let {

            binding.profileName.text = it.displayName?.capitalize()
            binding.profileEmail.text = it.email

            if (it.isEmailVerified) {
                binding.emailVerify.visibility = View.INVISIBLE
            } else {
                binding.emailVerify.visibility = View.VISIBLE

            }

        }

        binding.emailVerify.setOnClickListener {
            currentUser?.sendEmailVerification()
                ?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        toast("Megerősítő email elküldve!")
                        binding.emailVerify.visibility = View.INVISIBLE
                    } else toast(it.exception?.message)
                }
        }

        binding.profileNameChangeIcon.setOnClickListener {
            showNameChangeDialog(
            )
        }

        binding.btnSave.setOnClickListener {
            currentUser?.updateProfile(updates)
                ?.addOnSuccessListener {
                    toast("Profil sikeresen frissítve")

                }
                ?.addOnFailureListener {
                    toast(it.localizedMessage)
                }
        }

    }

    private fun showNameChangeDialog(
        @SuppressLint("SupportAnnotationUsage") @StringRes title: String = "Becenév",
        onNegativeButton: () -> Unit = this::onDestroy
    ) {
        val inputText = EditText(context)
        inputText.hint = "Név"
        inputText.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, id ->
                dialog.cancel()
                binding.profileName.text = inputText.text.toString()
                updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(inputText.text.toString())
                    .build()
            }
            .setNegativeButton("Mégse") { dialog, id -> onNegativeButton() }
            .setView(inputText)
            .create()
        alertDialog.show()
    }
}