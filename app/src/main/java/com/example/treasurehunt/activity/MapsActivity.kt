package com.example.treasurehunt.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.treasurehunt.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Map Object
    private lateinit var mMap: GoogleMap
    // gives access to the device's location
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var handler: Handler = Handler()
    private var runnable: Runnable? = null
    private val delay: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initToolbar()
        initMap()
    }

////// FORCES THE CAMERA TO FOLLOW THE USER'S CURRENT LOCATION //////
//    override fun onResume() {
//        handler.postDelayed(Runnable {
//            runnable?.let { handler.postDelayed(it, delay) }
//            getLastLocation()
//        }.also { runnable = it }, delay)
//        super.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible super.onPause();
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //outState.putInt("points", points)
    }

    ////// TOOLBAR STUFF //////
    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.mapToolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.layout_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val view = findViewById<View>(R.id.mapToolBar)

        when (item.itemId) {
            R.id.refresh -> {
                onCreate(null)
                return true
            }
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.profile -> {
                //val intent = Intent(this, SettingsActivity::class.java)
                //startActivity(intent)
            }
            R.id.settingsBtn -> {
                //val intent = Intent(this, SettingsActivity::class.java)
                //startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    ////// MAP STUFF //////
    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setMaxZoomPreference(18F)
        mMap.setMinZoomPreference(15F)

        getLastLocation()
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15F))

        //handle my location FAB
        val fabMy: View = findViewById(R.id.locFab)
        fabMy.setOnClickListener { view ->
            getLastLocation()
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15F))
        }
    }

    private fun getLastLocation() {
        if(isLocationEnabled()) {
            //checking location permission
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //request permission
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 42)
                return
            }

            //once the last location is acquired
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if (location == null) {
                    //if it couldn't be aquired, get some new location data
                    requestNewLocationData()
                }
                else {
                    val lat = location.latitude
                    val long = location.longitude

                    Log.i(getString(R.string.log_map_loc), "$lat & $long")

                    val lastLoc = LatLng(lat, long)

                    //update camera
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(lastLoc))
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(15F))
                    mMap.addMarker(MarkerOptions().position(lastLoc)
                        .title(getString(R.string.map_current_location)))
                }
            }
            // couldn't get location, so go to Settings (may be deprecated)
        } else {
            val mRootView = findViewById<View>(R.id.map)
            val locSnack = Snackbar.make(mRootView, getString(R.string.map_location_switch),
                Snackbar.LENGTH_LONG)
            locSnack.show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    // Request a new location
    private fun requestNewLocationData() {
        //parameters for location
        val mLocationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 100
        }

        //checking location permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 42)
            return
        }

        //update the location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //add a callback so that the location is repeatedly updated according to paremeters
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
            mLocationCallback, Looper.myLooper()!!
        )
    }

    //callback for repeatedly getting location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val lat = mLastLocation.latitude
            val long = mLastLocation.longitude

            val lastLoc = LatLng(lat, long)
            Log.i(getString(R.string.log_map_loc), "$lat & $long")
        }
    }

    // check whether location is enabled
    private fun isLocationEnabled():Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}