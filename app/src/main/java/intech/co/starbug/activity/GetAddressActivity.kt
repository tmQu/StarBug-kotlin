package intech.co.starbug.activity


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchByTextResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.play.integrity.internal.i
import intech.co.starbug.BuildConfig
import intech.co.starbug.R
import intech.co.starbug.adapter.MapPredictionAdapter
import org.json.JSONObject
import java.net.URL
import java.util.Arrays


class GetAddressActivity : AppCompatActivity(), OnMapReadyCallback {
    val REQUEST_CODE_PERMISSION = 1
    val TAG =  "GetAddressActivity"

    private lateinit var mMap: GoogleMap

    private lateinit var locationProviderClient: FusedLocationProviderClient

    private var locationPermissionGranted = false
    private lateinit var placesClient: PlacesClient
    private lateinit var adapterMapPrediction: MapPredictionAdapter

    private val sessionToken = AutocompleteSessionToken.newInstance()

    private lateinit var autocomplete_district: AutoCompleteTextView
    private lateinit var autocomplete_addr: AutoCompleteTextView
    private lateinit var edt_city: TextInputLayout

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var bottomNavigation: BottomSheetBehavior<ConstraintLayout>


    private var desMarker: Marker? = null
    private var full_addr = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_get_address)

        checkLocationPermissions()

        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.MAPS_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty()) {
            Log.e("Places test", "No api key")
            finish()
            return
        }

        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        placesClient = Places.createClient(this)
        adapterMapPrediction = MapPredictionAdapter(this)

        autocomplete_addr = findViewById(R.id.autocomplete_addr)
        autocomplete_district = findViewById(R.id.autocomplete_district)
        edt_city = findViewById(R.id.edt_city)

        bottomNavigation = BottomSheetBehavior.from(findViewById(R.id.bottom_choose_location))
        bottomNavigation.isHideable = true
        bottomNavigation.state = BottomSheetBehavior.STATE_HIDDEN

        handleAutocompleteAddr()
        handleAutocompleteDistrict()

        findViewById<ImageButton>(R.id.here_btn).setOnClickListener {
            getDeviceLocation()
            reverseGeocoding()

        }

        findViewById<MaterialButton>(R.id.choose_location).setOnClickListener {
            if(autocomplete_addr.text.toString().isEmpty() || autocomplete_district.text.toString().isEmpty() || edt_city.editText?.text.toString().isEmpty())
            {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = intent
            intent.putExtra("address", full_addr)
            intent.putExtra("lat", desMarker?.position?.latitude)
            intent.putExtra("lng", desMarker?.position?.longitude)
            Log.i(TAG, "Lat: ${desMarker?.position?.latitude} Lng: ${desMarker?.position?.longitude}")
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun handleAutocompleteDistrict() {
        val listDistrict = resources.getStringArray(R.array.district_hcm)
        val districtAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listDistrict)
        autocomplete_district.setAdapter(districtAdapter)
    }


    private fun getFullAddr(): String
    {

        full_addr =  autocomplete_addr.text.toString() + " " + autocomplete_district.text.toString() + " thành phố " + edt_city.editText?.text.toString()
        return full_addr
    }

    private fun handleAutocompleteAddr() {
        autocomplete_addr.setAdapter(adapterMapPrediction)
        autocomplete_addr.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {    // Cancel any previous place prediction requests
                    handler.removeCallbacksAndMessages(null)

                    // Start a new place prediction request in 300 ms
                    handler.postDelayed({ getPlacePredictions(getFullAddr()) }, 300)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
        //catch enter key
        autocomplete_addr.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                // handle enter key

                searchText(getFullAddr())
                clearFocusAndHideKeyboard(autocomplete_addr)
                return@setOnKeyListener true
            }
            false
        }

        autocomplete_addr.setOnItemClickListener { parent, view, position, id ->
            val id = adapterMapPrediction.getItem(position).placeId
            clearFocusAndHideKeyboard(autocomplete_addr)

            searchById(id)
        }
    }

    private fun searchById(placeId: String, replace: Boolean = true) {
        val placeFields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS)



        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.i(TAG, "Place found: ${place.address}")
                var temp_addr = ""
                val components = place.addressComponents

                // get ward
                val ward = place.address
                val ward_split = ward.split(",")
                var wardName = ""
                for (i in ward_split)
                {
                    if(i.contains("Phường") || i.contains("phường"))
                    {
                        wardName = i
                    }
                }

                for (component in components.asList()) {
                    Log.i(TAG, "Component: ${component.types} ${component.name}")
                    when {
                        "street_number" in component.types -> {
                            temp_addr += component.name + " "
                            Log.i(TAG, "Temp addr: $temp_addr")
                        }
                        "route" in component.types -> temp_addr += component.name + " "
                        "sublocality" in component.types -> temp_addr += component.name + " "
                        "locality" in component.types -> temp_addr += component.name
                        "administrative_area_level_2" in component.types -> autocomplete_district.setText(component.name)
                    }
                }
                if(wardName != "" && !temp_addr.contains("Phường") && !temp_addr.contains("phường"))
                {
                    temp_addr += ", $wardName"
                }

                bottomNavigation.state = BottomSheetBehavior.STATE_EXPANDED
                findViewById<TextView>(R.id.address).text = place.address


                if (replace)
                {
                    autocomplete_addr.setText(temp_addr)

                }
                bottomNavigation.state = BottomSheetBehavior.STATE_EXPANDED
                findViewById<TextView>(R.id.address).text = getFullAddr()

                setDesLocationMarker(place.latLng, getFullAddr())
                moveCamera(place.latLng, 20f)

            }
            .addOnFailureListener {
                Log.e(TAG, "Place not found: ${it.message}")
            }
    }

    private fun reverseGeocoding()
    {
        val latLng = "${desMarker?.position?.latitude},${desMarker?.position?.longitude}"
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=${latLng}&result_type=street_address&key=${BuildConfig.MAPS_API_KEY}"
        val request = ReverseGeocodingRequest(this, url)
        request.execute()
    }

    fun updateDesAddress(result: String?)
    {
        if(result != null)
        {
            val jsonObject = JSONObject(result)
            val results = jsonObject.getJSONArray("results")
            if(results.length() > 0)
            {
                val address = results.getJSONObject(0).getString("formatted_address")
                // get address component
                val components = results.getJSONObject(0).getJSONArray("address_components")
                var temp_addr = ""
                for (i in 0 until components.length())
                {
                    val component = components.getJSONObject(i)
                    val types = component.getJSONArray("types")
                    if(types.length() > 0)
                    {
                        when(types.getString(0))
                        {
                            "street_number" -> temp_addr += component.getString("long_name") + " "
                            "route" -> temp_addr += component.getString("long_name") + " "
                            "sublocality" -> temp_addr += component.getString("long_name") + " "
                            "locality" -> temp_addr += component.getString("long_name")
                            "administrative_area_level_2" -> autocomplete_district.setText(component.getString("long_name"))
                        }
                    }
                }
                autocomplete_addr.setText(temp_addr)
                findViewById<TextView>(R.id.address).text = address
                full_addr = address

            }
            bottomNavigation.state = BottomSheetBehavior.STATE_EXPANDED

        }
        else {
            findViewById<TextView>(R.id.address).text = getFullAddr()
        }
    }

    class ReverseGeocodingRequest(private val activity: GetAddressActivity, private val url: String) :
        AsyncTask<Void?, Void?, String?>() {
        override fun doInBackground(vararg params: Void?): String? {
            val result: String
            val handler = Handler(Looper.getMainLooper())
            try {
                val data = url.let { URL(it).readText() }
                result = data
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return null
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            activity.updateDesAddress(result)
        }



    }

    private fun setDesLocationMarker(latLng: LatLng, address: String) {
        desMarker?.remove()
        val markerOptions = MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).title("Drag me").snippet(address)
        desMarker = mMap.addMarker(markerOptions)
    }



    private fun searchText(name: String) {
        val newRequest = FindAutocompletePredictionsRequest.builder()
            .setCountries("VN")
            // Session Token only used to link related Place Details call. See https://goo.gle/paaln
            .setSessionToken(sessionToken)
            .setQuery(name)
            .build()

        // Perform autocomplete predictions request
        placesClient.findAutocompletePredictions(newRequest)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                searchById(predictions[0].placeId, false)
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                }
            }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }


    private fun initMap()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Permission granted")

            initMap()
            locationPermissionGranted = true

        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE_PERMISSION
            )
        }
    }



    private fun getDeviceLocation()
    {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if(true)
            {

                val locationResult = locationProviderClient.lastLocation
                locationResult.addOnCompleteListener {
                    if(it.isSuccessful)
                    {
                        val location = it.result
                        if(location != null)
                        {
                            Log.i("Location", "Location: ${location.latitude}, ${location.longitude}")
                            setDesLocationMarker(LatLng(location.latitude, location.longitude), "")

                            moveCamera(LatLng(location.latitude, location.longitude), 20f)
                        }
                    }
                }

            }
        }
        catch (error: SecurityException)
        {
            Log.e(TAG, "Error getting device location: ${error.message}")
        }

    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mMap.isMyLocationEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        mMap.uiSettings.isCompassEnabled = false
        getDeviceLocation()

        mMap.setOnMarkerDragListener(object: OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {

            }

            override fun onMarkerDragEnd(p0: Marker) {
                Log.i(TAG, "Location: ${p0.position.latitude}, ${p0.position.longitude} ${desMarker?.position}")

            }

            override fun onMarkerDragStart(p0: Marker) {

            }
        })

        mMap.setOnMapLongClickListener {
            setDesLocationMarker(it, "")

        }




    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty()){
                for (result in grantResults){
                    if (result != PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "Permission denied")
                        return
                    }
                }
                Log.d(TAG, "Permission granted")

                initMap()
                locationPermissionGranted = true

            }
        }
    }


    private fun getPlacePredictions(query: String) {
        // The value of 'bias' biases prediction results to the rectangular region provided
        // (currently Kolkata). Modify these values to get results for another area. Make sure to
        // pass in the appropriate value/s for .setCountries() in the
        // FindAutocompletePredictionsRequest.Builder object as well.
        Log.i(TAG, "Query: $query")
//        val bias: LocationBias = RectangularBounds.newInstance(
//            LatLng(22.458744, 88.208162),  // SW lat, lng
//            LatLng(22.730671, 88.524896) // NE lat, lng
//        )

        // Create a new programmatic Place Autocomplete request in Places SDK for Android
        val newRequest = FindAutocompletePredictionsRequest.builder()
            .setCountries("VN")
            // Session Token only used to link related Place Details call. See https://goo.gle/paaln
            .setSessionToken(sessionToken)
            .setQuery(query)
            .build()

        // Perform autocomplete predictions request
        placesClient.findAutocompletePredictions(newRequest)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions

                adapterMapPrediction.setPredictions(predictions)
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                }
            }
    }
    private fun clearFocusAndHideKeyboard(searchBox: AutoCompleteTextView) {
        searchBox.clearFocus()
        val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(searchBox.windowToken, 0)
    }

}