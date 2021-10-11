package hu.bme.aut.android.turisztikapp.fragment

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.turisztikapp.R
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        binding = DialogAddNewPlaceBinding.inflate(LayoutInflater.from(context))
        binding.placeCameraButton.setOnClickListener {
            makePhotoClick()
        }
        return AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.new_place))
                .setView(binding.root)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, null)
                .create()
    }


    private fun sendClick() {
        if (!validateForm()) {
            /* if (binding.placeNameEditText.text.isEmpty() && binding.placeAddressEditText.text.isEmpty()) {
                 Toast.makeText(activity, "Üres mező", Toast.LENGTH_LONG).show()
                 findNavController().navigate(
                     R.id.nav_new_place_dialog_fragment,
                     null
                 )
                 return
             }*/
            return
        }

        if (binding.placeCameraButton.visibility != View.VISIBLE) {
            uploadPlace()
        } else {
            try {
                uploadPlaceWithImage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun validateForm() = binding.placeNameEditText.validateNonEmpty() && binding.placeAddressEditText.validateNonEmpty()


    private fun uploadPlace(image: String? = null) {  //adatbázist összeköti a layout-tal
        val newPlace = Place(
                id = UUID.randomUUID().toString(),
                name = binding.placeNameEditText.text.toString(),
                address = binding.placeAddressEditText.text.toString(),
                description = binding.placeDescEditText.text.toString(),
                rate = binding.placeRatingBar.rating,
                image = image)

        /* category = DishItem.Category.getByOrdinal(categorySpinner.selectedItemPosition)
            ?: DishItem.Category.Előétel,    //data-layout */
        /*categorySpinner = binding.placeCategorySpinner
       categorySpinner.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.category_items)
            )
        ) */

        val db = Firebase.firestore

        db.collection("places")
                .add(newPlace)
                .addOnSuccessListener {

                    // Toast.makeText(activity, "Place created", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e -> Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show() }
    }

    private fun makePhotoClick() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_CODE)
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

    override fun onClick(p0: DialogInterface?, p1: Int) {

        sendClick()
        findNavController().navigate(
            R.id.ok_dialog_to_list,
            null
        )

    }

}