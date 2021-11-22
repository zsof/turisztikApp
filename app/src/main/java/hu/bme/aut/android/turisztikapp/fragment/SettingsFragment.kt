package hu.bme.aut.android.turisztikapp.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.databinding.FragmentSettingsBinding
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URLEncoder
import java.util.*

class SettingsFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val REQUEST_CODE_CAMERA = 100
        const val REQUEST_CODE_GALLERY = 101

    }

    private var newImageUri: Uri? = null
    private lateinit var binding: FragmentSettingsBinding
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private var reAuthSuccess: Boolean = false
    private lateinit var galleryPermRequest: ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryPermRequest =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    Toast.makeText(context, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                    uploadPhotoFromGallery()
                } else Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        currentUser?.let {

            binding.profileName.text = it.displayName?.capitalize()
            binding.profileEmail.text = it.email
            Glide.with(this)
                .load(it.photoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(
                    binding.profileImage
                )
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
                        toast(getString(R.string.verify_email_settings))
                        binding.emailVerify.text =
                            getString(R.string.verify_email_textview_settings)
                    } else toast(it.exception?.localizedMessage)
                }
        }

        binding.profileNameChangeIcon.setOnClickListener {
            showNameChangeDialog(
                title = getString(R.string.name_settings),
                inputTextHint = getString(R.string.name_hint_settings),
                inputTextType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
            )
        }

        binding.profileEmailChangeIcon.setOnClickListener {
            if (reAuthSuccess)
                showEmailChangeDialog()
            else {
                binding.etReAuth.visibility = View.VISIBLE
                binding.btnAuth.visibility = View.VISIBLE
                binding.til.visibility = View.VISIBLE
            }
        }

        binding.profilePasswordChangeIcon.setOnClickListener {
            if (reAuthSuccess)
                showPasswordChangeDialog()
            else {
                binding.etReAuth.visibility = View.VISIBLE
                binding.btnAuth.visibility = View.VISIBLE
                binding.til.visibility = View.VISIBLE
            }
        }

        binding.btnAuth.setOnClickListener {
            currentUser?.let {
                val credential =
                    EmailAuthProvider.getCredential(it.email!!, binding.etReAuth.text.toString())

                it.reauthenticate(credential)
                    .addOnSuccessListener {
                        toast(getString(R.string.authentication_successful_settings))
                        binding.etReAuth.visibility = View.GONE
                        binding.btnAuth.visibility = View.GONE
                        binding.til.visibility = View.GONE

                        reAuthSuccess = true
                        binding.profileAuthCheckbox.isChecked = true
                    }
                    .addOnFailureListener { e ->
                        toast(e.localizedMessage)

                        reAuthSuccess = false
                    }
            }
        }

        binding.btnSave.setOnClickListener {
            val updates = UserProfileChangeRequest.Builder()
            if (!binding.profileName.text.isNullOrEmpty())
                updates.displayName = binding.profileName.text.toString()
            if (newImageUri != null)
                updates.photoUri = newImageUri
            currentUser?.updateProfile(updates.build())
                ?.addOnSuccessListener {
                    toast(getString(R.string.update_profile_settings))
                }
                ?.addOnFailureListener { e ->
                    toast(e.localizedMessage)
                }
        }

        binding.profileImage.setOnClickListener {
            showImageDialog()
        }
        setToolbar()
    }

    private fun showImageDialog(
        title: String = getString(R.string.choose_image),
    ) {

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setPositiveButton(getString(R.string.gallery)) { dialog, _ ->
                handleGalleryPermission()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.camera)) { dialog, _ ->
                makePhotoClick()
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }

    private fun showNameChangeDialog(
        title: String?,
        onNegativeButton: () -> Unit = this::onDestroy,
        inputTextHint: String?,
        inputTextType: Int
    ) {

        val inputText = EditText(context)
        inputText.hint = inputTextHint
        inputText.inputType = inputTextType

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
                if (!inputText.text.isNullOrEmpty()) {
                    binding.profileName.text = inputText.text.toString()
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> onNegativeButton() }
            .setView(inputText)
            .create()
        alertDialog.show()
    }

    private fun showEmailChangeDialog(
        title: String = getString(R.string.change_email_settings),
        onNegativeButton: () -> Unit = this::onDestroy
    ) {
        val inputText = EditText(context)
        inputText.hint = getString(R.string.new_email_hint_settings)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
                if (inputText.text.isNotEmpty())
                    currentUser?.updateEmail(inputText.text.toString())
                        ?.addOnSuccessListener {
                            binding.profileEmail.text = inputText.text.toString()
                            toast(getString(R.string.email_successful_refresh_settings))
                        }
                        ?.addOnFailureListener { e ->
                            toast(e.localizedMessage)
                        }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> onNegativeButton() }
            .setView(inputText)
            .create()
        alertDialog.show()
    }

    private fun showPasswordChangeDialog(
        title: String = getString(R.string.change_password),
        onNegativeButton: () -> Unit = this::onDestroy
    ) {
        val inputText = EditText(context)
        inputText.hint = getString(R.string.new_password_hint)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.cancel()
                if (inputText.text.isNotEmpty())
                    currentUser?.updatePassword(inputText.text.toString())
                        ?.addOnSuccessListener {
                            toast(getString(R.string.password_update_settings))
                        }
                        ?.addOnFailureListener { e ->
                            toast(e.localizedMessage)
                        }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> onNegativeButton() }
            .setView(inputText)
            .create()
        alertDialog.show()
    }

    private fun makePhotoClick() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA)
    }

    private fun uploadPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.profileImage.setImageBitmap(
                Bitmap.createScaledBitmap(
                    imageBitmap,
                    binding.profileImage.width,
                    binding.profileImage.height,
                    false
                )
            )
            binding.btnSave.isEnabled = false
            uploadProfileImage(imageBitmap)
        } else if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            try {
                val imageUri = data?.data
                val imageStream: InputStream? = imageUri?.let {
                    activity?.applicationContext?.contentResolver?.openInputStream(
                        it
                    )
                }
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.profileImage.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        selectedImage,
                        binding.profileImage.width,
                        binding.profileImage.height,
                        false
                    )
                )
                uploadProfileImage(selectedImage)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                toast(getString(R.string.wrong_message_settings))
            }
        } else {
            toast(getString(R.string.havent_picked_photo_add_new))
        }
    }

    private fun uploadProfileImage(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"

        val newImageRef =
            storageReference.child("images/$newImageName")

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener { e ->
                toast(e.localizedMessage)
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                newImageRef.downloadUrl
            }
            .addOnSuccessListener {
                newImageUri = it
                binding.btnSave.isEnabled = true
            }

    }

    private fun setToolbar() {
        navHostFragment =
            (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navView.setNavigationItemSelectedListener(this)
    }


    override fun onResume() {
        super.onResume()
        reAuthSuccess = false
        binding.etReAuth.text.clear()
        binding.profileAuthCheckbox.isChecked = false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(
                    R.id.action_settings_to_logout,
                    null
                )
            }
            R.id.menu_places ->
                findNavController().navigate(
                    R.id.action_settings_to_place_list,
                    null
                )
            R.id.menu_map ->
                findNavController().navigate(
                    R.id.action_settings_to_map,
                    null
                )
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleGalleryPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity as AppCompatActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showRationaleDialog(
                    explanation = R.string.contacts_permission_explanation,
                    onPositiveButton = this::requestExternalStoragePermission
                )
            } else {
                requestExternalStoragePermission()
            }
        } else {
            uploadPhotoFromGallery()
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
            .setCancelable(false)
            .setMessage(explanation)
            .setPositiveButton(R.string.ok_permisson_dialog_map) { dialog, _ ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton(R.string.exit_permission_diaog_map) { _, _ -> onNegativeButton() }
            .create()
        alertDialog.show()
    }

    private fun requestExternalStoragePermission() {
        galleryPermRequest.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
