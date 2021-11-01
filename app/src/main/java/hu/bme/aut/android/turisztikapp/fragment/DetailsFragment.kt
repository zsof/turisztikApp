package hu.bme.aut.android.turisztikapp.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.adapter.CommentAdapter
import hu.bme.aut.android.turisztikapp.adapter.ImageAdapter
import hu.bme.aut.android.turisztikapp.data.Comment
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.FragmentDetailsBinding

import hu.bme.aut.android.turisztikapp.extension.hideKeyboard
import hu.bme.aut.android.turisztikapp.extension.validateNonEmpty
import java.util.*


class DetailsFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: FragmentDetailsBinding
    private var place: Place? = null
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var imageAdapter: ImageAdapter
    private var rateSum: Float = 0F
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    companion object {
        const val PLACE = "place"
        private const val REQUEST_CODE_CAMERA_DETAILS = 103
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {                //megkapja az adatokat
            place = it.get(PLACE) as Place?
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
            LinearLayoutManager(context)
        imageAdapter = ImageAdapter(place!!.id)
        binding.imageDetailsRecycler.adapter = imageAdapter


        setToolbar()
        displayPlaceData()
        initPostsListener()
    }

    private fun displayPlaceData() {

        /* Glide.with(this)
             .load(place?.image)
             .transition(DrawableTransitionOptions().crossFade())
             .into(binding.imageDetailsRecycler)
 */
        binding.nameDetailsText.text = place?.name
        binding.addressDetailsText.text = place?.address
        binding.descDetailsText.text = "Leírás"
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
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA_DETAILS)
        }
        /* binding.rateDetailsText.setOnClickListener {
             showDialog()
         }*/

        binding.descDetailsText.setOnClickListener {
            showRationaleDialog(
                explanation = place?.description
            )
        }
    }

    private fun showRationaleDialog(
        @SuppressLint("SupportAnnotationUsage") @StringRes title: String = "Leírás",
        explanation: String?,
        onPositiveButton: () -> Unit = this::onDestroy
    ) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setCancelable(false)
            .setMessage(explanation)
            .setPositiveButton(getString(R.string.ok_permisson_dialog)) { dialog, id ->
                dialog.cancel()
                onPositiveButton()
            }
            .create()
        alertDialog.show()
    }

    private fun initPostsListener() {
        val db = Firebase.firestore
        db.collection("comment")
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


        /* db.collection("images")
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
             }*/
    }

    private fun sendClick() {
        if (!validateForm()) {
            Toast.makeText(activity, "Nincs komment", Toast.LENGTH_LONG).show()
            return
        } else {
            uploadComment()
            binding.commentDetailsEditText.text.clear()
            this.hideKeyboard()

        }
    }

    private fun validateForm() = binding.commentDetailsEditText.validateNonEmpty()

    private fun uploadComment() {
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            userId = uid,
            userName = userName,
            placeId = place?.id.toString(),
            comment = binding.commentDetailsEditText.text.toString()
                .replaceFirstChar { it.uppercase() }

        )

        val db = Firebase.firestore

        db.collection("comment")
            .add(newComment)
            .addOnSuccessListener {
                Toast.makeText(activity, "comment created", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e -> toast(e.toString()) }
    }

    private fun makePhotoClick() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA_DETAILS)
        // uploadImageWithPhoto()
    }

    /*private fun uploadImage(image: String? = placeHolder.toString()) {
        val newImage = Image(
            id = UUID.randomUUID().toString(),
            placeId = place?.id.toString(),
            image = image
        )

        val db = Firebase.firestore

        db.collection("images")
            .add(newImage)
            .addOnSuccessListener {

                Toast.makeText(activity, "photo created", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e -> toast(e.toString()) }


    }*/

    /* private fun uploadImageWithPhoto(){
         val bitmap: Bitmap = (binding.fabUploadImage.drawable as BitmapDrawable).bitmap
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
              uploadImage(downloadUri.toString())
          }
     }*/
    /* override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(reqCode, resultCode, data)

         if (resultCode != Activity.RESULT_OK) {
             return
         }
         if (reqCode == REQUEST_CODE_CAMERA_DETAILS) {
             try {
                 val imageBitmap = data?.extras?.get("data") as Bitmap
                 binding.fabUploadImage.setImageBitmap(
                     Bitmap.createScaledBitmap(
                         imageBitmap,
                         binding.fabUploadImage.width,
                         binding.fabUploadImage.height,
                         false
                     )
                 )

             } catch (e: FileNotFoundException) {
                 e.printStackTrace()
                 Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
             }
         } else {
             Toast.makeText(context, "You haven't picked Image", Toast.LENGTH_LONG).show()
         }
     }*/

    private fun setToolbar() {
        navHostFragment =
            (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navView.setNavigationItemSelectedListener(this)
    }

    /*  private fun showDialog() {  //ratinbar alertdialog
          val popDialog = AlertDialog.Builder(requireContext())
          val linearLayout = LinearLayout(context)
          val rating = RatingBar(context)
          val lp = LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT,
              LinearLayout.LayoutParams.WRAP_CONTENT
          )
          rating.layoutParams = lp
          rating.numStars = 5
          rating.stepSize = 1f

          //add ratingBar to linearLayout
          linearLayout.addView(rating)
          popDialog.setIcon(android.R.drawable.btn_star_big_on)
          popDialog.setTitle("Értékelés: ")

          //add linearLayout to dailog
          popDialog.setView(linearLayout)
          rating.onRatingBarChangeListener =
              OnRatingBarChangeListener { ratingBar, v, b -> rateSum += v }

          // Button OK
          popDialog.setPositiveButton(android.R.string.ok) { dialoginterface, i ->
              binding.rateDetailsText.text = rateSum.toString()
              binding.rateDetailsText.text = rating.progress.toString()

          }
              .setNegativeButton("Cancel", null)
          popDialog.create()
          popDialog.show()
      }*/


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

}