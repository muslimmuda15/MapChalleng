package com.rachmad.app.mychallengetest.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.rachmad.app.mychallengetest.App
import com.rachmad.app.mychallengetest.EMPTY_ERROR
import com.rachmad.app.mychallengetest.UNKNOWN_ERROR
import com.rachmad.app.mychallengetest.api.ServiceApi
import com.rachmad.app.mychallengetest.helper.Connection
import com.rachmad.app.mychallengetest.structure.GeocodeData
import com.rachmad.app.mychallengetest.structure.PlaceDetailsData
import com.rachmad.app.mychallengetest.structure.Prediction
import com.rachmad.app.mychallengetest.structure.PredictionData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ViewModelApp: ViewModel() {
    @Inject lateinit var serviceApi: ServiceApi
    @Inject lateinit var context: Context

    var connectionGeoLocation = MutableLiveData<Int>()
    var connectionPrediction = MutableLiveData<Int>()
    var connectionPlaceDetails = MutableLiveData<Int>()

    var geocodeValue: GeocodeData? = null
    var predictionValue: PredictionData? = null
    var placeDetailsValue: PlaceDetailsData? = null

    var geocodeError: Int? = null
    var predictionError: Int? = null
    var placeDetailsError: Int? = null

    init {
        App.appComponent.inject(this)
    }

    fun getGeocodeData(latLng: LatLng, key: String){
        geocodeValue = null
        geocodeError = null
        connectionGeoLocation.postValue(Connection.ACCEPTED)

        val locationParams = Location.convert(latLng.latitude, Location.FORMAT_DEGREES) + "," + Location.convert(latLng.longitude, Location.FORMAT_DEGREES)

        val call = serviceApi.geocodeAccess(locationParams, key)
        call.enqueue(object: Callback<GeocodeData>{
            override fun onResponse(call: Call<GeocodeData>, response: Response<GeocodeData>) {
                if(response.isSuccessful){
                    response.body()?.let {
                        it.results?.let { data ->
                            if(data.size > 0){
                                geocodeValue = it
                                connectionGeoLocation.postValue(Connection.OK)
                            }
                            else{
                                geocodeValue = null
                                geocodeError = EMPTY_ERROR
                                connectionGeoLocation.postValue(Connection.NO_DATA)
                            }
                        } ?: run {
                            geocodeValue = null
                            geocodeError = UNKNOWN_ERROR
                            connectionGeoLocation.postValue(Connection.FAILED)
                        }
                    } ?: run {
                        geocodeValue = null
                        geocodeError = UNKNOWN_ERROR
                        connectionGeoLocation.postValue(Connection.FAILED)
                    }
                }
                else{
                    geocodeValue = null
                    geocodeError = UNKNOWN_ERROR
                    connectionGeoLocation.postValue(Connection.FAILED)
                }
            }

            override fun onFailure(call: Call<GeocodeData>, t: Throwable) {
                geocodeValue = null
                geocodeError = UNKNOWN_ERROR
                connectionGeoLocation.postValue(Connection.UNKNOWN_FAILED)
            }
        })
    }

    fun clearPredictionList(){
        predictionValue = null
        predictionError = null
        connectionPrediction.postValue(Connection.CLEAR)
    }

    fun getPredictionData(search: String, key: String){
        predictionValue = null
        predictionError = null
        connectionPrediction.postValue(Connection.ACCEPTED)

        val call = serviceApi.predictionAccess(search, key)
        call.enqueue(object: Callback<PredictionData>{
            override fun onResponse(call: Call<PredictionData>, response: Response<PredictionData>) {
                if(response.isSuccessful){
                    response.body()?.let {
                        it.predictions?.let { data ->
                            if(data.size > 0){
                                predictionValue = it
                                connectionPrediction.postValue(Connection.OK)
                            }
                            else{
                                predictionValue = null
                                predictionError = EMPTY_ERROR
                                connectionPrediction.postValue(Connection.NO_DATA)
                            }
                        } ?: run {
                            predictionValue = null
                            predictionError = UNKNOWN_ERROR
                            connectionPrediction.postValue(Connection.FAILED)
                        }

                    } ?: run {
                        predictionValue = null
                        predictionError = UNKNOWN_ERROR
                        connectionPrediction.postValue(Connection.FAILED)
                    }
                }
                else{
                    predictionValue = null
                    predictionError = UNKNOWN_ERROR
                    connectionPrediction.postValue(Connection.FAILED)
                }
            }

            override fun onFailure(call: Call<PredictionData>, t: Throwable) {
                predictionValue = null
                predictionError = UNKNOWN_ERROR
                connectionPrediction.postValue(Connection.UNKNOWN_FAILED)
            }
        })
    }

    fun getPlaceDetailsData(placeId: String, key: String){
        placeDetailsValue = null
        placeDetailsError = null
        connectionPlaceDetails.postValue(Connection.ACCEPTED)

        val call = serviceApi.placeDetailsAccess(placeId, key)
        call.enqueue(object: Callback<PlaceDetailsData>{
            override fun onResponse(call: Call<PlaceDetailsData>, response: Response<PlaceDetailsData>) {
                if(response.isSuccessful){
                    response.body()?.let {
                        it.result?.let { result ->
                            result.address_components?.let { data ->
                                if(data.size > 0){
                                    placeDetailsValue = it
                                    connectionPlaceDetails.postValue(Connection.OK)
                                }
                                else{
                                    placeDetailsValue = null
                                    placeDetailsError = EMPTY_ERROR
                                    connectionPlaceDetails.postValue(Connection.NO_DATA)
                                }
                            } ?: run {
                                placeDetailsValue = null
                                placeDetailsError = EMPTY_ERROR
                                connectionPlaceDetails.postValue(Connection.FAILED)
                            }
                        } ?: run {
                            placeDetailsValue = null
                            placeDetailsError = UNKNOWN_ERROR
                            connectionPlaceDetails.postValue(Connection.FAILED)
                        }

                    } ?: run {
                        placeDetailsValue = null
                        placeDetailsError = UNKNOWN_ERROR
                        connectionPlaceDetails.postValue(Connection.FAILED)
                    }
                }
                else{
                    placeDetailsValue = null
                    placeDetailsError = UNKNOWN_ERROR
                    connectionPlaceDetails.postValue(Connection.FAILED)
                }
            }

            override fun onFailure(call: Call<PlaceDetailsData>, t: Throwable) {
                placeDetailsValue = null
                placeDetailsError = UNKNOWN_ERROR
                connectionPlaceDetails.postValue(Connection.UNKNOWN_FAILED)
            }
        })
    }

    var location: LatLng? = null
}