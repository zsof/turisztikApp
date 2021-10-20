package hu.bme.aut.android.turisztikapp.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
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
import java.io.FileNotFoundException
import java.io.InputStream
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

        if (binding.placeCameraButton.visibility != View.VISIBLE) {
            uploadPlace()
        } else if (binding.placeCameraButton.visibility == View.VISIBLE) {
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

        if (binding.placeGalleryButton.visibility != View.VISIBLE) {
            uploadPlace()
        } else if (binding.placeGalleryButton.visibility == View.VISIBLE) try {
            uploadPlaceWithImageFromGallery()
            findNavController().navigate(
                R.id.ok_dialog_to_list,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun validateForm() =
        binding.placeNameEditText.validateNonEmpty() && binding.placeAddressEditText.validateNonEmpty()


    private fun uploadPlace(image: String? = null) {  //adatbázist összeköti a layout-tal
        val newPlace = Place(
            id = UUID.randomUUID().toString(),
            name = binding.placeNameEditText.text.toString()
                .replaceFirstChar { it.uppercase() },
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

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        if (reqCode == REQUEST_CODE) {
            try {
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                binding.placeCameraButton.setImageBitmap(imageBitmap)
                binding.placeCameraButton.visibility = View.VISIBLE

                val imageUri = data?.data
                val imageStream: InputStream? = imageUri?.let {
                    activity?.applicationContext?.contentResolver?.openInputStream(
                        it
                    )
                }
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                binding.placeGalleryButton.setImageBitmap(selectedImage)
                binding.placeGalleryButton.visibility = View.VISIBLE

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
            }
            /* try {   //scaleBitmappel nem fut le a kamerából!! való feltöltés
                 val imageUri = data?.data
                 val imageStream: InputStream? = imageUri?.let {
                     activity?.applicationContext?.contentResolver?.openInputStream(
                         it
                     )
                 }
                 val selectedImage = BitmapFactory.decodeStream(imageStream)
                 binding.placeGalleryButton.setImageBitmap(scaleBitmap(selectedImage,150, 140))
                 binding.placeGalleryButton.visibility = View.VISIBLE
             }
             catch (e: FileNotFoundException) {
                 e.printStackTrace()
                 Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
             }*/
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
                Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
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

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity as AppCompatActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showRationaleDialog(
                    //  explanation = R.string.contacts_permission_explanation,
                    onPositiveButton = this::requestExternalStoragePermission
                )

            } else {
                requestExternalStoragePermission()
            }
        } else {
            openGalleryForImage()
        }
    }

    private fun requestExternalStoragePermission() {
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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryForImage()
                } else {

                }
                return
            }
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, wantedWidth: Int, wantedHeight: Int): Bitmap? {
        val output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val m = Matrix()
        m.setScale(wantedWidth.toFloat() / bitmap.width, wantedHeight.toFloat() / bitmap.height)
        canvas.drawBitmap(bitmap, m, Paint())
        return output
    }
}

/*

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode != Activity.RESULT_OK) {
        return
    }

    if (requestCode == AddNewPlaceDialogFragment.REQUEST_CODE) {

        //    val imageBitmap = data?.extras?.get("data") as? Bitmap
        val imageUri = data?.extras?.get("image/*") as? Uri
        binding.placeGalleryButton.setImageURI(imageUri)
        binding.placeGalleryButton.visibility = View.VISIBLE

        Toast.makeText(context, imageUri.toString(), Toast.LENGTH_SHORT).show()
        //   binding.placeCameraButton.setImageBitmap(imageBitmap)
        // binding.placeCameraButton.visibility = View.VISIBLE

    }
}
*/

*/