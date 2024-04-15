package intech.co.starbug.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.Place.*
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.SphericalUtil.computeDistanceBetween
import intech.co.starbug.R
import intech.co.starbug.databinding.ActivityGetAddressBinding
import kotlinx.coroutines.launch
import java.util.Locale


const val REQUEST_CODE_PERMISSION = 1
val TAG =  "GetAddressActivity"
class GetAddressActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
    private lateinit var binding: ActivityGetAddressBinding

    private lateinit var coordinates: LatLng
    private val acceptedProximity = 150.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGetAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkLocationPermissions()


    }



    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
//            getAndCompareLocations()
        } else {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Since ACCESS_FINE_LOCATION is the only permission in this sample,
            // run the location comparison task once permission is granted.
            // Otherwise, check which permission is granted.
        } else {
            // Fallback behavior if user denies permission

            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_PERMISSION)
            Log.d("TAG", "User denied permission")
        }
    }

    override fun onLocationChanged(location: Location) {
        val geocoder = Geocoder(this, Locale("vn", "VN"))
        var address : List<Address> = listOf()
        lifecycleScope.launch {
            try {
                address = geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
            }catch (e: Exception){

                Log.e(TAG, "Error getting address: ${e.message}")
            }
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.setOnCameraIdleListener {
            val geocoder = Geocoder(this, Locale("vn", "VN"))
            lifecycleScope.launch() {
                try {
                    val address = geocoder.getFromLocation(mMap.cameraPosition.target.latitude, mMap.cameraPosition.target.longitude, 1,
                    )

                }
                catch (e: Exception){
                    Log.e("tag", "Error getting address: ${e.message}")
                }
            }


        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

}