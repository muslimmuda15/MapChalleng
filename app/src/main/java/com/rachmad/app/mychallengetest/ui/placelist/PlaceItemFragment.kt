package com.rachmad.app.mychallengetest.ui.placelist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rachmad.app.mychallengetest.R
import com.rachmad.app.mychallengetest.databinding.FragmentPlaceItemBinding
import com.rachmad.app.mychallengetest.databinding.FragmentPlaceItemListBinding
import com.rachmad.app.mychallengetest.helper.Connection
import com.rachmad.app.mychallengetest.structure.Prediction
import com.rachmad.app.mychallengetest.ui.MapsActivity
import com.rachmad.app.mychallengetest.viewmodel.ViewModelApp

class PlaceItemFragment : Fragment() {
    private lateinit var placeBinding: FragmentPlaceItemListBinding
    private lateinit var viewModel: ViewModelApp
    var listener: OnPlaceInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = (activity as MapsActivity).viewModel

        placeBinding = FragmentPlaceItemListBinding.inflate(inflater, container, false)
        return placeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(placeBinding){
            viewModel.connectionPrediction.observe(viewLifecycleOwner, { conn ->
                when(conn){
                    Connection.OK -> {
                        placeLoading.visibility = FrameLayout.GONE
                        placeList.visibility = RecyclerView.VISIBLE
                        placeError.visibility = TextView.GONE

                        placeList.layoutManager = LinearLayoutManager(context)
                        placeList.adapter = PlaceItemRecyclerViewAdapter(viewModel.predictionValue?.predictions, listener)
                    }
                    Connection.ACCEPTED -> {
                        placeLoading.visibility = FrameLayout.VISIBLE
                        placeList.visibility = RecyclerView.GONE
                        placeError.visibility = TextView.GONE
                    }
                    Connection.NO_DATA -> {
                        placeLoading.visibility = FrameLayout.GONE
                        placeList.visibility = RecyclerView.GONE
                        placeError.visibility = TextView.VISIBLE
                        placeError.text = getString(R.string.empty)
                    }
                    Connection.FAILED -> {
                        placeLoading.visibility = FrameLayout.GONE
                        placeList.visibility = RecyclerView.GONE
                        placeError.visibility = TextView.VISIBLE
                        placeError.text = getString(R.string.failed)
                    }
                    Connection.UNKNOWN_FAILED -> {
                        placeLoading.visibility = FrameLayout.GONE
                        placeList.visibility = RecyclerView.GONE
                        placeError.visibility = TextView.VISIBLE
                        placeError.text = getString(R.string.unknown_error)
                    }
                    Connection.CLEAR -> {
                        placeList.adapter = PlaceItemRecyclerViewAdapter(ArrayList(), listener)
                    }
                }
            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnPlaceInteractionListener){
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnPlaceInteractionListener {
        fun onPlaceListener(prediction: Prediction?)
    }

    companion object {
        @JvmStatic
        fun newInstance() = PlaceItemFragment()
    }
}