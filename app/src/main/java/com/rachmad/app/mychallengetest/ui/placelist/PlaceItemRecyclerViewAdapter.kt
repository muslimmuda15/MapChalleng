package com.rachmad.app.mychallengetest.ui.placelist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.rachmad.app.mychallengetest.R
import com.rachmad.app.mychallengetest.structure.Prediction
import com.rachmad.app.mychallengetest.structure.PredictionData

class PlaceItemRecyclerViewAdapter(
    private val values: ArrayList<Prediction?>?, private val listener: PlaceItemFragment.OnPlaceInteractionListener?
) : RecyclerView.Adapter<PlaceItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_place_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        values?.get(position)?.let { item ->
            holder.title.text = item.structured_formatting?.main_text ?: ""
            holder.description.text = item.structured_formatting?.secondary_text ?: ""

            holder.placeItemLayout.setOnClickListener {
                listener?.onPlaceListener(item)
            }
        }
    }

    override fun getItemCount(): Int = values?.size ?: 0

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.place_title)
        val description: TextView = view.findViewById(R.id.place_description)
        val placeItemLayout: LinearLayout = view.findViewById(R.id.place_item_layout)
    }
}