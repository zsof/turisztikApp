package hu.bme.aut.android.turisztikapp.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.data.Category
import hu.bme.aut.android.turisztikapp.data.Image
import hu.bme.aut.android.turisztikapp.data.MyGeoPoint
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.DialogAddNewPlaceBinding
import hu.bme.aut.android.turisztikapp.extension.validateNonEmpty
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URLEncoder
import java.util.*


class AddNewPlaceDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    companion object {
        private const val REQUEST_CODE_CAMERA = 101
        private const val REQUEST_CODE_GALLERY = 10
        const val LATLNG = "latLng"
    }

    private lateinit var binding: DialogAddNewPlaceBinding
    private var setGallery: Boolean = false
    private var setCamera: Boolean = false
    private var latLng: LatLng = LatLng(47.497913, 19.040236)
    private val placeHolder = R.drawable.ic_camera
    private lateinit var galleryPermRequest: ActivityResultLauncher<String>
    private var newPlace: Place? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {                //megkapja az adatokat
            latLng = it.get(LATLNG) as LatLng
        }

        galleryPermRequest =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    Toast.makeText(context, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                    openGalleryForImage()
                } else Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        binding = DialogAddNewPlaceBinding.inflate(LayoutInflater.from(context))
        binding.placeCameraButton.setOnClickListener {
            makePhotoClick()
        }
        binding.placeGalleryButton.setOnClickListener {
            handleGalleryPermission()
        }
        if (latLng != null) {
            binding.placeAddressEditText.setText(getAddress(latLng!!))
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.new_place))
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { dialogInterface, i ->
                sendClick()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)  //scrollable alert
        return dialog
    }

    private fun sendClick() {
        if (!validateForm()) {
            Toast.makeText(activity, "Üres név vagy cím", Toast.LENGTH_LONG).show()
            return
        }

        if (!setCamera && !setGallery
        ) {
            uploadPlace()
            findNavController().navigate(
                R.id.ok_dialog_to_list,
                null
            )
        } else if (setCamera && !setGallery) {
            try {
                uploadPlaceWithImage()
                findNavController().navigate(
                    R.id.ok_dialog_to_list,
                    null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (setGallery &&
            !setCamera
        ) {
            try {
                uploadPlaceWithImageFromGallery()
                findNavController().navigate(
                    R.id.ok_dialog_to_list,
                    null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun validateForm() =
        binding.placeNameEditText.validateNonEmpty() && binding.placeAddressEditText.validateNonEmpty()

    private fun uploadPlace(image: String? = placeHolder.toString()) {  //adatbázist összeköti a layout-tal
        newPlace = Place(
            id = UUID.randomUUID().toString(),
            name = binding.placeNameEditText.text.toString()
                .replaceFirstChar { it.uppercase() },
            address = binding.placeAddressEditText.text.toString()
                .replaceFirstChar { it.uppercase() },
            geoPoint = MyGeoPoint(latLng.latitude, latLng.longitude),
            description = binding.placeDescEditText.text.toString()
                .replaceFirstChar { it.uppercase() },
            rate = binding.placeRatingBar.rating,
            image = image,
            category = Category.getByOrdinal(binding.placeCategorySpinner.selectedItemPosition)
        )

        binding.placeCategorySpinner.adapter = context?.let {
            ArrayAdapter(
                it.applicationContext,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.category_items)
            )
        }

        val db = Firebase.firestore

        db.collection("places")
            .add(newPlace!!)
            .addOnSuccessListener {

                // Toast.makeText(activity, "Place created", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
            }
    }


    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

        return addresses[0].getAddressLine(0)

    }

    private fun makePhotoClick() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA)
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)

        if (resultCode != RESULT_OK) {
            return
        }
        if (reqCode == REQUEST_CODE_CAMERA) {
            try {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                binding.placeCameraButton.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        imageBitmap,
                        binding.placeCameraButton.width,
                        binding.placeCameraButton.height,
                        false
                    )
                )
                setCamera = true
                setGallery = false
                binding.placeGalleryButton.setImageResource(R.drawable.ic_gallery)

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        } else if (reqCode == REQUEST_CODE_GALLERY) {
            try {
                val imageUri = data?.data
                val imageStream: InputStream? = imageUri?.let {
                    activity?.applicationContext?.contentResolver?.openInputStream(
                        it
                    )
                }
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.placeGalleryButton.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        selectedImage,
                        binding.placeGalleryButton.width,
                        binding.placeGalleryButton.height,
                        false
                    )
                )
                setGallery = true
                setCamera = false
                binding.placeCameraButton.setImageResource(R.drawable.ic_camera)

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadPlaceWithImage() {
        val bitmap: Bitmap = (binding.placeCameraButton.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("images/$newImageName")

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                //  Toast.makeText(activity,exception.message, Toast.LENGTH_LONG).show()
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                uploadPlace(downloadUri.toString())
                val newImage = newPlace?.let {
                    Image(
                        id = UUID.randomUUID().toString(),
                        image = downloadUri.toString(),
                        placeId = it.id
                    )
                }

                val db = Firebase.firestore


                if (newImage != null) {
                    db.collection("images")
                        .add(newImage)
                        .addOnSuccessListener {
                            //Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            // Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                }


            }


    }

    private fun uploadPlaceWithImageFromGallery() {
        val bitmapGallery: Bitmap =
            (binding.placeGalleryButton.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmapGallery.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("images/$newImageName")

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                uploadPlace(downloadUri.toString())
                val newImage = newPlace?.let {
                    Image(
                        id = UUID.randomUUID().toString(),
                        image = downloadUri.toString(),
                        placeId = it.id
                    )
                }

                val db = Firebase.firestore


                if (newImage != null) {
                    db.collection("images")
                        .add(newImage)
                        .addOnSuccessListener {
                            //Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            // Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        sendClick()
        findNavController().navigate(
            R.id.ok_dialog_to_list,
            null
        )
    }

    private fun showRationaleDialog(
        @SuppressLint("SupportAnnotationUsage") @StringRes title: String = getString(R.string.attention),
        @StringRes explanation: Int,
        onPositiveButton: () -> Unit,
        onNegativeButton: () -> Unit = this::dismiss
    ) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setCancelable(false)
            .setMessage(explanation)
            .setPositiveButton(R.string.ok_permisson_dialog) { dialog, id ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton(R.string.exit_permission_diaog) { dialog, id -> onNegativeButton() }
            .create()
        alertDialog.show()
    }

    private fun handleGalleryPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(   //jogosultság kérés magyarázata
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
            openGalleryForImage()
        }
    }

    private fun requestExternalStoragePermission() {  //jogosultság elkérése
        galleryPermRequest.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}