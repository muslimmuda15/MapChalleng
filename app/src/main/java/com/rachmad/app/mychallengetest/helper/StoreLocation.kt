package com.rachmad.app.mychallengetest.helper

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rachmad.app.mychallengetest.App
import com.rachmad.app.mychallengetest.BuildConfig
import java.lang.Exception
import javax.inject.Inject

class StoreLocation(val context: Context) {
    var isGPSProblem: Boolean? = null
    var listener: OnUpdateGPSListener? = null
    @Inject lateinit var fuseLocationProviderClient: FusedLocationProviderClient
    @Inject lateinit var settingClient: SettingsClient
    @Inject lateinit var locationRequest: LocationRequest
    @Inject lateinit var locationSettingRequest: LocationSettingsRequest
    var locationCallBack: LocationCallback

    var location = MutableLiveData<Location>()
    var isUpdateUI: Boolean? = null
    var isLocationUpdate: Boolean = false

    init {
        App.appComponent.inject(this)

        if(context is OnUpdateGPSListener){
            listener = context
        }

        isGPSProblem = null
        isUpdateUI = null

        location.postValue(defaultLocation())

        listener?.onUpdateUiInteraction(null)
        locationCallBack = object: LocationCallback(){
            override fun onLocationResult(loc: LocationResult) {
                super.onLocationResult(loc)
                Log.d("main", "LatLogn Success")
                location.postValue(loc.lastLocation)
                isUpdateUI = true
                isGPSProblem = false
                listener?.onUpdateUiInteraction(true)
                stopLocation()
            }
        }

        isLocationUpdate = false

        Log.d("main", "End Store Location Context")
    }

    fun defaultLocation(): Location {
        val loc = Location("")
        loc.latitude = -6.225199551349465
        loc.longitude = 106.84844981878994
        return loc
    }

    fun defaultLatLong(): LatLng {
        return LatLng(-6.225199551349465, 106.84844981878994)
    }

    fun checkPermission(): Boolean {
        val state = ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
        return state == PackageManager.PERMISSION_GRANTED
    }

    fun stopLocation(){
        isLocationUpdate = false
        fuseLocationProviderClient.removeLocationUpdates(locationCallBack)
            .addOnCompleteListener {
                Log.d("main", "Location Stopped")
            }
    }

    fun startLocation(activity: Activity){
        Dexter.withContext(context)
            .withPermission(ACCESS_FINE_LOCATION)
            .withListener(object: PermissionListener{
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    startLocationUpdate(activity)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    isLocationUpdate = false
                    isGPSProblem = false
                    isUpdateUI = false
                    location.postValue(defaultLocation())
                    listener?.onUpdateUiInteraction(false)
                    if(response.isPermanentlyDenied){
                        openSetting()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    @SuppressLint("MissingPermission")
    fun switchAndGettingLocation(){
        val looper = Looper.myLooper()!!
        fuseLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, looper)
        val handle = Handler(looper)
        handle.postDelayed(object: Runnable{
            override fun run() {
                if(isUpdateUI == null){
                    isUpdateUI = false
                    isGPSProblem = true
                    location.postValue(defaultLocation())
                    listener?.onUpdateUiInteraction(false)
                    stopLocation()
                }
                handle.removeCallbacksAndMessages(null)
            }
        }, 10000)
    }

    private fun openSetting(){
        var intent = Intent()
        intent.setAction((Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.setData(uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate(activity: Activity){
        settingClient.checkLocationSettings(locationSettingRequest)
            .addOnSuccessListener {
                val looper = Looper.myLooper()!!
                fuseLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, looper)
                val handle = Handler(looper)
                handle.postDelayed(object: Runnable{
                    override fun run() {
                        if(isUpdateUI == null) {
                            isUpdateUI = false
                            isGPSProblem = true
                            location.postValue(defaultLocation())
                            listener?.onUpdateUiInteraction(false)
                            stopLocation()
                        }
                        handle.removeCallbacksAndMessages(null)
                    }
                }, 10000)
            }
            .addOnFailureListener(activity, object: OnFailureListener{
                override fun onFailure(e: Exception) {
                    val statusCode = (e as ApiException).statusCode
                    when(statusCode){
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try{
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(activity, 100)
                            }
                            catch(sie: IntentSender.SendIntentException){
                                Toast.makeText(context, "Pending unable to execute request.", Toast.LENGTH_LONG).show()
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Toast.makeText(context, "Location settings are inadequate, and cannot be fixed here, fix settings.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
    }

    fun denyGPS(){
        isUpdateUI = false
        isGPSProblem = false
        listener?.onUpdateUiInteraction(false)
    }
}

interface OnUpdateGPSListener{
    fun onUpdateUiInteraction(isGrantedLocation: Boolean?)
}