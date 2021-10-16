package hu.bme.aut.android.turisztikapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import hu.bme.aut.android.turisztikapp.R
import hu.bme.aut.android.turisztikapp.data.Place
import hu.bme.aut.android.turisztikapp.databinding.FragmentDetailsBinding

class DetailsFragment : BaseFragment() {
    private var binding: FragmentDetailsBinding? = null //fragment binding!!
    private var place: Place? = null

    companion object {
        val LETTER = "letter"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            place = it.get(LETTER) as Place?
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

        displayPlaceData()

    }

    private fun displayPlaceData() {

        Glide.with(this)
            .load(place?.image)
            .transition(DrawableTransitionOptions().crossFade())
            .into(binding?.detailsImage!!)

        binding?.nameDetailsText?.text = place?.name
        binding?.addressDetailsText?.text = place?.address

        binding?.descDetailsText?.text = place?.description
        binding?.rateDetailsText?.text = place?.rate.toString()


    }

}