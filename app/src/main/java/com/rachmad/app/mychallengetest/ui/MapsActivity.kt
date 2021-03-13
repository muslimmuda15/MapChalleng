package com.rachmad.app.mychallengetest.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rachmad.app.mychallengetest.App
import com.rachmad.app.mychallengetest.R
import com.rachmad.app.mychallengetest.databinding.ActivityMapsBinding
import com.rachmad.app.mychallengetest.helper.Connection
import com.rachmad.app.mychallengetest.helper.OnUpdateGPSListener
import com.rachmad.app.mychallengetest.helper.StoreLocation
import com.rachmad.app.mychallengetest.structure.Prediction
import com.rachmad.app.mychallengetest.ui.placelist.PlaceItemFragment
import com.rachmad.app.mychallengetest.viewmodel.ViewModelApp
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import javax.inject.Inject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnUpdateGPSListener, PlaceItemFragment.OnPlaceInteractionListener {

    @Inject lateinit var storeLocation: StoreLocation
    @Inject lateinit var context: Context
    lateinit var mapBinding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var alert: AlertDialog
    val viewModel: ViewModelApp by viewModels()
    val owner = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapBinding.root)

        App.appComponent.inject(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initialAlert()
        setActivityResult()
        bindingView()
        accessPlaceDetailsAPI()
        accessGeolocationAPI()

        storeLocation.startLocation(this)

        supportFragmentManager.beginTransaction().add(
            R.id.place_list_layout,
            PlaceItemFragment.newInstance()
        ).commit()
    }

    private fun bindingView(){
        with(mapBinding){
            btnSearch.setOnClickListener {
                if(slidingUp.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidingUp.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    btnSearch.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_search_24
                    )
                    txtSearch.text = getString(R.string.select_address)
                }
                else {
                    slidingUp.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                    btnSearch.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_baseline_keyboard_arrow_up_24
                    )
                    txtSearch.text = getString(R.string.search_address)
                    searchPlace.requestFocus()
                }
            }

            slidingUp.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener{
                override fun onPanelSlide(panel: View?, slideOffset: Float) {}

                override fun onPanelStateChanged(
                    panel: View?,
                    previousState: SlidingUpPanelLayout.PanelState?,
                    newState: SlidingUpPanelLayout.PanelState?
                ) {
                    if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(searchPlace.getWindowToken(), 0)
                    }
                    else if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                        if(viewModel.predictionValue == null) {
                            val imm: InputMethodManager =
                                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(searchPlace, InputMethodManager.SHOW_IMPLICIT)
                        }
                    }
                }
            })

            searchButton.setOnClickListener {
                viewModel.getPredictionData(
                    searchPlace.text.toString(),
                    getString(R.string.google_maps_key)
                )
            }

            searchPlace.setOnEditorActionListener { _, i, _ ->
                if(i == EditorInfo.IME_ACTION_SEARCH){
                    viewModel.getPredictionData(
                        searchPlace.text.toString(),
                        getString(R.string.google_maps_key)
                    )
                    true
                }
                false
            }

            selectAddress.setOnClickListener {
                val point = viewModel.location
                viewModel.getGeocodeData(point ?: storeLocation.defaultLatLong(), getString(R.string.google_maps_key))
            }

            clearButton.setOnClickListener {
                searchPlace.setText("")
                viewModel.clearPredictionList()
            }

            slidingUp.isTouchEnabled = false
        }
    }

    private fun accessGeolocationAPI(){
        with(mapBinding) {
            viewModel.connectionGeoLocation.observe(owner, { conn ->
                when (conn) {
                    Connection.OK -> {
                        placeDetailsLoading.visibility = FrameLayout.VISIBLE
                        viewModel.geocodeValue?.results?.let { data ->
                            val streetAddress = data.find { item ->
                                item?.types?.let { type ->
                                    "street_address" in type
                                } ?: run {
                                    false
                                }
                            }
                            if(streetAddress != null){
                                val placeId = streetAddress.place_id
                                viewModel.getPlaceDetailsData(placeId ?: "", getString(R.string.google_maps_key))
                            }
                            else{
                                placeDetailsLoading.visibility = FrameLayout.GONE
                                placeDetailsStatus.visibility = TextView.VISIBLE
                                placeDetailsStatus.setText(getString(R.string.empty))
                            }
                        }
                    }
                    Connection.ACCEPTED -> {
                        placeDetailsLoading.visibility = FrameLayout.VISIBLE
                    }
                    Connection.NO_DATA -> {
                        placeDetailsLoading.visibility = FrameLayout.GONE
                        Toast.makeText(owner, getString(R.string.empty), Toast.LENGTH_SHORT).show()
                    }
                    Connection.FAILED -> {
                        placeDetailsLoading.visibility = FrameLayout.GONE
                        Toast.makeText(owner, getString(R.string.failed), Toast.LENGTH_SHORT).show()
                    }
                    Connection.UNKNOWN_FAILED -> {
                        placeDetailsLoading.visibility = FrameLayout.GONE
                        Toast.makeText(owner, getString(R.string.unknown_error), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
        }
    }

    private fun accessPlaceDetailsAPI(){
        with(mapBinding) {
            viewModel.connectionPlaceDetails.observe(owner, { conn ->
                when (conn) {
                    Connection.OK -> {
                        placeDetailsLoading.visibility = FrameLayout.GONE
                        placeDetailsStatus.visibility = TextView.GONE

                        val data = viewModel.placeDetailsValue

                        placeTitle.setText(data?.result?.name ?: "")
                        placeDescription.setText(data?.result?.formatted_address ?: "")
                    }
                    Connection.ACCEPTED -> {
                        placeDetailsLoading.visibility = FrameLayout.VISIBLE
                        placeDetailsStatus.visibility = TextView.VISIBLE
                        placeDetailsStatus.setText(getString(R.string.please_wait))
                    }
                    Connection.NO_DATA -> {
                        placeDetailsLoading.visibility = FrameLayout.GONE
                        placeDetailsStatus.visibility = TextView.VISIBLE
                        placeDetailsStatus.setText(getString(R.string.empty))
                        Toast.makeText(owner, getString(R.string.empty), Toast.LENGTH_SHORT).show()
                    }
                    Connection.FAILED -> {
                        placeDetailsLoading.visibility = FrameLayout.GONE
                        placeDetailsStatus.visibility = TextView.VISIBLE
                        placeDetailsStatus.setText(getString(R.string.failed))
                        Toast.makeText(owner, getString(R.string.failed), Toast.LENGTH_SHORT).show()
                    }
                    Connection.UNKNOWN_FAILED -> {
                        placeDetailsLoading.visibility = FrameLayout.GONE
                        placeDetailsStatus.visibility = TextView.VISIBLE
                        placeDetailsStatus.setText(getString(R.string.unknown_error))
                        Toast.makeText(owner, getString(R.string.unknown_error), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
        }
    }

    private fun setActivityResult(){
        /**
         * New way for result code
         */
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when(result.resultCode){
                Activity.RESULT_OK -> {
                    if (storeLocation.isLocationUpdate && storeLocation.checkPermission()) {
                        storeLocation.switchAndGettingLocation()
                    }
                }
                Activity.RESULT_CANCELED -> {
                    storeLocation.denyGPS()
                }
            }
        }
    }

    private fun initialAlert(){
        alert = AlertDialog.Builder(this)
            .setCancelable(false)
            .setIcon(R.drawable.ic_gps_fixed_black_24dp)
            .setTitle(getString(R.string.gps_problem_title))
            .setMessage(getString(R.string.gps_problem))
            .setPositiveButton(getString(R.string.close), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    storeLocation.stopLocation()
                }
            })
            .create()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapClickListener { point ->
            mapSelected(point)
            viewModel.location = point
        }

        storeLocation.location.observe(this, {
            it?.let {
                mMap.clear()
                val marker = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(marker))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 15f))

                viewModel.getGeocodeData(marker, getString(R.string.google_maps_key))
            }
        })
    }

    private fun mapSelected(point: LatLng){
        mMap.clear()
        val markerOption = MarkerOptions().position(point)
        mMap.addMarker(markerOption)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f))
    }

    override fun onUpdateUiInteraction(isGrantedLocation: Boolean?) {
        isGrantedLocation?.let {
            storeLocation.isGPSProblem?.let {
                if(it == true){
                    alert.show()
                }
                else{
                    if(!alert.isShowing()){
                        storeLocation.stopLocation()
                    }
                }
            }
        }
    }

    override fun onPlaceListener(prediction: Prediction?) {
        mapBinding.slidingUp.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        mapBinding.btnSearch.background = ContextCompat.getDrawable(
            context,
            R.drawable.ic_baseline_search_24
        )
        viewModel.getPlaceDetailsData(
            prediction?.place_id ?: "",
            getString(R.string.google_maps_key)
        )
    }

    /**
     * Old Way For Result Code
     */
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == 100){
//            when(resultCode){
//                Activity.RESULT_OK -> {
//                    if(storeLocation.isLocationUpdate && storeLocation.checkPermission()) {
//                        storeLocation.switchAndGettingLocation()
//                    }
//                }
//                Activity.RESULT_CANCELED -> {
//                    storeLocation.denyGPS()
//                }
//            }
//        }
//    }
}