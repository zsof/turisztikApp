package hu.bme.aut.android.turisztikapp.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.data.Category
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.DialogAddNewPlaceBinding
import hu.bme.aut.android.turisztikapp.extension.validateNonEmpty
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*


class AddNewPlaceDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    companion object {
        private const val REQUEST_CODE = 101
    }

    private lateinit var binding: DialogAddNewPlaceBinding
    // private lateinit var categorySpinner: Spinner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        binding = DialogAddNewPlaceBinding.inflate(LayoutInflater.from(context))
        binding.placeCameraButton.setOnClickListener {
            makePhotoClick()
        }
        binding.placeGalleryButton.setOnClickListener {
            handleReadContactsPermission()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.new_place))
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { dialogInterface, i ->
                sendClick()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    }


    private fun sendClick() {
        if (!validateForm()) {
            Toast.makeText(activity, "Üres név vagy cím", Toast.LENGTH_LONG).show()
            findNavController().navigate(
                R.id.nav_new_place_dialog_fragment,
                null
            )
        }

        if (binding.placeCameraButton.visibility != View.VISIBLE) {
            uploadPlace()
        } else {
            try {
                uploadPlaceWithImage()
                findNavController().navigate(
                    R.id.ok_dialog_to_list,
                    null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun validateForm() = binding.placeNameEditText.validateNonEmpty() && binding.placeAddressEditText.validateNonEmpty()


    private fun uploadPlace(image: String? = null) {  //adatbázist összeköti a layout-tal
        val newPlace = Place(
            id = UUID.randomUUID().toString(),
            name = binding.placeNameEditText.text.toString().capitalize(),
            address = binding.placeAddressEditText.text.toString().capitalize(),
            description = binding.placeDescEditText.text.toString().capitalize(),
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
            .add(newPlace)
            .addOnSuccessListener {

                // Toast.makeText(activity, "Place created", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
            }
    }


    private fun makePhotoClick() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_CODE)
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            binding.placeCameraButton.setImageBitmap(imageBitmap)
            binding.placeCameraButton.visibility = View.VISIBLE
            binding.placeGalleryButton.setImageURI(data?.data)
            binding.placeGalleryButton.visibility = View.VISIBLE
        }
    }

    private fun uploadPlaceWithImage() {
        val bitmap: Bitmap = (binding.placeCameraButton.drawable as BitmapDrawable).bitmap
        val bitmapGallery: Bitmap = (binding.placeGalleryButton.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        bitmapGallery.compress(Bitmap.CompressFormat.JPEG, 100, baos)
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
        /*   @SuppressLint("SupportAnnotationUsage") @StringRes title: String = "Attention!",
           @StringRes explanation: Int,*/
        onPositiveButton: () -> Unit,
        onNegativeButton: () -> Unit = this::dismiss
    ) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setPositiveButton("Proceed") { dialog, id ->
                dialog.cancel()
                onPositiveButton()
            }
            .setNegativeButton("Exit") { dialog, id -> onNegativeButton() }
            .create()
        alertDialog.show()
    }

    private fun handleReadContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity as AppCompatActivity,
                    Manifest.permission.READ_CONTACTS
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                showRationaleDialog(
                    //  explanation = R.string.contacts_permission_explanation,
                    onPositiveButton = this::requestContactsPermission
                )

            } else {
                // No explanation needed, we can request the permission.
                requestContactsPermission()
            }
        } else {
            openGalleryForImage()
        }
    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            activity as AppCompatActivity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    openGalleryForImage()
                } else {
                    // permission denied! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }


}




