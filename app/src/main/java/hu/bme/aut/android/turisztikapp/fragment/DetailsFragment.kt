package hu.bme.aut.android.turisztikapp.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.adapter.CommentAdapter
import hu.bme.aut.android.turisztikapp.adapter.ImageAdapter
import hu.bme.aut.android.turisztikapp.data.Comment
import hu.bme.aut.android.turisztikapp.data.Image
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.FragmentDetailsBinding
import hu.bme.aut.android.turisztikapp.extension.hideKeyboard
import hu.bme.aut.android.turisztikapp.extension.validateNonEmpty
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URLEncoder
import java.util.*

class DetailsFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: FragmentDetailsBinding
    private var place: Place? = null
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var galleryPermRequest: ActivityResultLauncher<String>
    private lateinit var startForPhotoResult: ActivityResultLauncher<Intent>
    private lateinit var startForPhotoFromGalleryResult: ActivityResultLauncher<Intent>

    companion object {
        const val PLACE = "place"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            place = it.get(PLACE) as Place?
        }

        galleryPermRequest =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    Toast.makeText(context, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                    uploadPhotoFromGallery()
                } else Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT)
                    .show()
            }
        startForPhotoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = it.data?.extras?.get("data") as Bitmap?
                    imageBitmap ?: return@registerForActivityResult
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        imageBitmap,
                        100,
                        100,
                        false
                    )
                    uploadImage(scaledBitmap)
                }
            }
        startForPhotoFromGalleryResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val imageUri = it.data?.data
                    imageUri ?: return@registerForActivityResult
                    val imageStream: InputStream? =
                        activity?.applicationContext?.contentResolver?.openInputStream(imageUri)

                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    Bitmap.createScaledBitmap(
                        selectedImage,
                        100,
                        100,
                        false
                    )
                    uploadImage(selectedImage)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDetailsBinding.bind(view)

        binding.commentDetailsRecycler.layoutManager =
            LinearLayoutManager(context)
        commentAdapter = CommentAdapter(place!!.id)
        binding.commentDetailsRecycler.adapter = commentAdapter

        binding.imageDetailsRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(place!!.id)
        binding.imageDetailsRecycler.adapter = imageAdapter

        binding.toolbar.inflateMenu(R.menu.refresh_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.refresh -> {
                    val actionToDetails =
                        DetailsFragmentDirections.actonDetailsToDetails(place = place!!)
                    findNavController().navigate(actionToDetails)
                }
            }
            true
        }

        setToolbar()
        displayPlaceData()
        initPostsListener()
    }

    private fun displayPlaceData() {
        binding.nameDetailsText.text = place?.name
        binding.addressDetailsText.text = place?.address
        binding.descDetailsText.text = getString(R.string.descreption_textview_details)
        binding.rateDetailsText.text = place?.rate.toString()
        place?.rate.also {
            if (it != null) {
                binding.ratingBarDetails.rating = it
            }
        }

        binding.commentSendImage.setOnClickListener {
            sendClick()
        }

        binding.fabUploadImage.setOnClickListener {
            showImageDialog()
        }

        binding.descDetailsText.setOnClickListener {
            showRationaleDialog(
                explanation = place?.description
            )
        }
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

    private fun makePhotoClick() {
        val imageCaptureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startForPhotoResult.launch(Intent(imageCaptureIntent))
    }

    private fun uploadPhotoFromGallery() {
        val galleryImageIntent = Intent(Intent.ACTION_PICK)
        galleryImageIntent.type = "image/*"
        startForPhotoFromGalleryResult.launch(Intent(galleryImageIntent))
    }

    private fun showRationaleDialog(
        title: String = getString(R.string.descreption_details),
        explanation: String?,
        onPositiveButton: () -> Unit = this::onDestroy
    ) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setCancelable(false)
            .setMessage(explanation)
            .setPositiveButton(getString(R.string.ok_permisson_dialog_map)) { dialog, _ ->
                dialog.cancel()
                onPositiveButton()
            }
            .create()
        alertDialog.show()
    }

    private fun initPostsListener() {
        val db = Firebase.firestore
        db.collection("comments")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    toast(e.toString())
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> commentAdapter.addComment(dc.document.toObject())
                        DocumentChange.Type.MODIFIED -> toast(dc.document.data.toString())  //TODO
                        DocumentChange.Type.REMOVED -> commentAdapter.removeComment(dc.document.toObject())
                    }
                }
            }

        db.collection("images")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    toast(e.toString())
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> imageAdapter.addImage(dc.document.toObject())
                        DocumentChange.Type.MODIFIED -> toast(dc.document.data.toString())  //TODO
                        DocumentChange.Type.REMOVED -> imageAdapter.removeImage(dc.document.toObject())
                    }
                }
            }
    }

    private fun sendClick() {
        if (!validateForm()) {
            toast(getString(R.string.empty_comment_details))
            return
        } else {
            uploadComment()
            binding.commentDetailsEditText.text.clear()
            this.hideKeyboard()

        }
    }

    private fun validateForm() = binding.commentDetailsEditText.validateNonEmpty()

    private fun uploadImage(bitmap: Bitmap) {
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
                val newImage = Image(
                    id = UUID.randomUUID().toString(),
                    image = it.toString(),
                    placeId = place?.id.toString(),
                    date = Calendar.getInstance().time
                )

                val db = Firebase.firestore
                db.collection("images")
                    .add(newImage)
                    .addOnSuccessListener {
                        toast(getString(R.string.image_uploaded_details))
                    }
                    .addOnFailureListener { e ->
                        toast(e.localizedMessage)
                    }
            }
    }

    private fun uploadComment() {
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            userId = uid,
            userName = userName,
            placeId = place?.id.toString(),
            comment = binding.commentDetailsEditText.text.toString()
                .replaceFirstChar { it.uppercase() },
            date = Calendar.getInstance().time

        )
        val db = Firebase.firestore

        db.collection("comments")
            .add(newComment)
            .addOnSuccessListener {
                toast(getString(R.string.comment_created_details))
            }
            .addOnFailureListener { e -> toast(e.localizedMessage) }
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(
                    R.id.action_details_to_logout,
                    null
                )
            }
            R.id.menu_places ->
                findNavController().navigate(
                    R.id.action_details_to_place_list,
                    null
                )
            R.id.menu_map ->
                findNavController().navigate(
                    R.id.action_details_to_map,
                    null
                )
            R.id.menu_settings ->
                findNavController().navigate(
                    R.id.action_details_to_settings,
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