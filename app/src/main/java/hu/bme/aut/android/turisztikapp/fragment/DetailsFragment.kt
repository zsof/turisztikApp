package hu.bme.aut.android.turisztikapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.adapter.CommentAdapter
import hu.bme.aut.android.turisztikapp.data.Comment
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.FragmentDetailsBinding

import hu.bme.aut.android.turisztikapp.extension.hideKeyboard
import hu.bme.aut.android.turisztikapp.extension.validateNonEmpty
import java.util.*


class DetailsFragment : BaseFragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: FragmentDetailsBinding //fragment binding!!
    private var place: Place? = null
    private lateinit var commentAdapter: CommentAdapter
    private var rateSum: Float = 0F
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    companion object {
        const val PLACE = "place"
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
        binding.placeComment.layoutManager =
            LinearLayoutManager(context)  //placeComment=recyclerview

        commentAdapter = CommentAdapter(place!!.id)
        binding.placeComment.adapter = commentAdapter
        binding.commentSendImage.setOnClickListener {
            sendClick()
        }
        /* binding.rateDetailsText.setOnClickListener {
             showDialog()
         }*/


        setToolbar()
        displayPlaceData()
        initPostsListener()

    }

    private fun displayPlaceData() {

        Glide.with(this)
            .load(place?.image)
            .transition(DrawableTransitionOptions().crossFade())
            .into(binding.detailsImage)

        binding.nameDetailsText.text = place?.name
        binding.addressDetailsText.text = place?.address

        binding.descDetailsText.text = place?.description
        binding.rateDetailsText.text = place?.rate.toString()
        place?.rate.also {
            if (it != null) {
                binding.ratingBarDetails.rating = it
            }
        }


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
    }

    private fun sendClick() {
        if (!validateForm()) {
            Toast.makeText(activity, "Nincs komment", Toast.LENGTH_LONG).show()
        }
        uploadComment()
        binding.commentDetailsEditText.text.clear()
        this.hideKeyboard()

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
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}