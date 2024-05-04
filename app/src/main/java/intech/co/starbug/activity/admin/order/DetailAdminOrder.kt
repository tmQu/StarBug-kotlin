package intech.co.starbug.activity.admin.order

import AdminCartAdapter
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ramotion.foldingcell.FoldingCell
import com.shuhart.stepview.StepView
import intech.co.starbug.BuildConfig
import intech.co.starbug.CustomMapFragment
import intech.co.starbug.R
import intech.co.starbug.adapter.AdminOrderAdapter
import intech.co.starbug.dialog.LoadingDialog
import intech.co.starbug.dialog.ReasonDialog
import intech.co.starbug.model.OrderModel
import intech.co.starbug.utils.Utils

class DetailAdminOrder : AppCompatActivity(), ReasonDialog.ConfirmListener, OnMapReadyCallback {
    companion object {
        val listStepView = mutableListOf<String>(
            "Approved",
            "Prepare",
            "pick-up",
            "Deliver",
            "Finished"
        )

    }
    private val TAG = "DetailAdminOrder"
    private val REQUEST_CODE_PERMISSION: Int = 0
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mapContainer: ConstraintLayout
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private var desMarker: Marker? = null
    private var currentMarker: Marker? = null


    private lateinit var cartListView: RecyclerView
    private lateinit var orderId: String
    private lateinit var foldingCellCustomer: FoldingCell
    private lateinit var foldingCellOrder: FoldingCell
    private lateinit var stepView: StepView
    private var order: OrderModel? = null
    private lateinit var mapNavBtn: ImageButton
    private lateinit var mapBtn: Button

    private lateinit var message: TextView


    private lateinit var cancelButton: Button
    private lateinit var nextButton: Button

    private var listStatus = mutableListOf<String>()
    private var currentStep = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_admin_order)
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
//        placesClient = Places.createClient(this)


        cartListView = findViewById(R.id.list_cart)
        foldingCellCustomer = findViewById(R.id.folding_cell)
        foldingCellOrder = findViewById(R.id.folding_cell_order)
        stepView = findViewById(R.id.step_view)
        cancelButton = findViewById(R.id.cancelButton)
        nextButton = findViewById(R.id.nextButton)
        mapContainer = findViewById(R.id.map_container)
        message = findViewById(R.id.message)
        stepView.setStepsNumber(listStepView.size)
        stepView.setSteps(listStepView)
        mapBtn = findViewById(R.id.map_btn)
        mapNavBtn = findViewById(R.id.mapNavBtn)

        mapNavBtn.setOnClickListener {
            getDirection()
        }

        mapBtn.setOnClickListener {
            if (mapContainer.visibility == VISIBLE)
                mapContainer.visibility = GONE
            else
                mapContainer.visibility = VISIBLE
        }

        listStatus =  resources.getStringArray(R.array.order_status).toMutableList()
        orderId = intent.getStringExtra("order_id").toString()

        foldingCellCustomer.setOnClickListener {
            foldingCellCustomer.toggle(false)
        }
        foldingCellOrder.setOnClickListener {
            foldingCellOrder.toggle(false)
        }
        message.visibility = GONE
        getOrderFromDB()


    }

    private fun getCurrentStepView(): String
    {
        when(order?.status)
        {
            listStatus[0] ->  {
                currentStep = 0
                stepView.go(0, true)
            }
            listStatus[1] -> {
                currentStep = 1
                stepView.go(1, true)

            }
            listStatus[2] -> {
                currentStep = 2
                stepView.go(2, true)

            }
            listStatus[3] -> {
                currentStep = 3
                stepView.go(3, true)
            }
            listStatus[4] -> {
                currentStep = 4
                stepView.done(true)
                stepView.go(4, true)
            }
        }

        if(currentStep >= 2)
            cancelButton.visibility = GONE

        if(currentStep == 2) {
            // show the map
            mapContainer.visibility = VISIBLE

        }
        else
        {
            mapContainer.visibility = GONE
        }


        if(currentStep == 3)
        {
            nextButton.visibility = GONE
            val doneBtn = findViewById<Button>(R.id.doneBtn)
            doneBtn.visibility = VISIBLE
            doneBtn.setOnClickListener {
                order?.status = listStatus[4]
                updateCartToDB()
                finish()
            }
        }

        if(currentStep == 4)
        {
            nextButton.visibility = GONE
            cancelButton.visibility = GONE
            message.visibility = GONE
        }
        return listStepView[currentStep]
    }

    private fun getOrderFromDB() {
        Log.i("DetailAdminOrder", " get Order bf bf $orderId")
        val dialog = LoadingDialog(this)
        dialog.startLoadingDialog()
        // get order from database
        val db = FirebaseDatabase.getInstance().getReference("Orders").orderByKey().equalTo(orderId)
        db.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                order = snapshot.getValue(OrderModel::class.java)
                Log.i("DetailAdminOrder", "Order bf: ${order?.id}")
                if (order != null) {
                    Log.i("DetailAdminOrder", "LatLng ${order?.paymentInforModel?.lat}, ${order?.paymentInforModel?.lng}")
                    setDesLocationMarker(LatLng(order?.paymentInforModel?.lat ?: 0.0, order?.paymentInforModel?.lng ?: 0.0))

                    // set order to view
                    Log.i("DetailAdminOrder", "Order: ${order?.id}")
                    setOrderToView()
                    setCustomerInformation()
                    handleButton()
                }
                if (order?.paymentInforModel?.paymentMethod == resources.getString(R.string.cod) && currentStep != 4)
                {
                    message.visibility = VISIBLE
                }
                dialog.dismissDialog()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val order = snapshot.getValue(OrderModel::class.java)
                if (order != null) {
                    // set order to view
                    Log.i("DetailAdminOrder", "Change Order: ${order.id}")
                    setOrderToView()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // remove order from view
                Toast.makeText(this@DetailAdminOrder, "Order removed", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setOrderToView()
    {
        val oderIdTv = findViewById<TextView>(R.id.orderId)
        oderIdTv.text = "${order?.genReadableOrderId()}"
        // set order to view
        getCurrentStepView()
        cartListView.adapter = AdminCartAdapter(order?.listCartItem ?: mutableListOf())
        cartListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        // set customer information
    }

    private fun setOrderStatus()
    {
        // set order status

    }


    private fun setCustomerInformation()
    {
        // set customer information
        var customerName = findViewById<TextView>(R.id.user_name)
        var customerPhone = findViewById<TextView>(R.id.user_phone)
        var customerAddress = findViewById<TextView>(R.id.user_address)
        var avatarImage = findViewById<ImageView>(R.id.user_avatar)

        customerName.text = order?.paymentInforModel?.name
        customerPhone.text = order?.paymentInforModel?.phone
        customerAddress.text = order?.paymentInforModel?.address
        avatarImage.load(order?.paymentInforModel?.avatar){
            target(
                onError = {
                    avatarImage.setImageResource(R.drawable.default_avatar)
                }
            )
        }


        var paymentMethod = findViewById<TextView>(R.id.payment_method)
        var total = findViewById<TextView>(R.id.total_price)
        var note = findViewById<TextView>(R.id.note)
        paymentMethod.text = order?.paymentInforModel?.paymentMethod
        total.text = Utils.formatMoney(order?.getTotalProductPrice() ?: 0)
        if(order?.paymentInforModel?.note.isNullOrEmpty()) {
            note.visibility = GONE
        }
        else
            note.visibility = VISIBLE
        note.text = order?.paymentInforModel?.note


    }

    private fun handleButton()
    {
        // handle button

        if (currentStep >= 2)
        {
            cancelButton.visibility = GONE
        }
        cancelButton.setOnClickListener {
            val reasonDialog = ReasonDialog()
            reasonDialog.show(supportFragmentManager, "ReasonDialog")
            updateCartToDB()
        }

        nextButton.setOnClickListener {
            order?.status = listStatus[currentStep + 1]
            currentStep += 1
            updateCartToDB()
        }
    }

    private fun updateCartToDB()
    {
        val db = FirebaseDatabase.getInstance().reference.child("Orders").child(orderId)
        db.setValue(order)
    }

    override fun onYesButton(reason: String) {
        order?.status = listStatus[5]
        order?.reason = reason
        updateCartToDB()
        finish()
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
        getDeviceLocation()

//        mMap.setOnMapClickListener {
//            getDirection()
//        }

    }

    private fun getDirection() {
        try {
            val uri =  Uri.parse("google.navigation:q=${order?.paymentInforModel?.lat},${order?.paymentInforModel?.lng}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
        catch (error: ActivityNotFoundException)
        {
            Log.e(TAG, "Error getting direction: ${error.message}")
        }
        catch (error: Exception)
        {
            Log.e(TAG, "Error getting direction: ${error.message}")
        }
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        (mapFragment as? CustomMapFragment)?.let {
            it.listener = object: CustomMapFragment.OnTouchListener {
                override fun onTouch() {
                    findViewById<NestedScrollView>(R.id.scrollview)?.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Permission denied")
                        return
                    }
                }
                Log.d(TAG, "Permission granted")

                initMap()

            }
        }
    }


    private fun setCurrentLocation(latLng: LatLng) {
        currentMarker?.remove()
        val markerOptions = MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true)
        currentMarker = mMap.addMarker(markerOptions)
    }
    private fun setDesLocationMarker(latLng: LatLng, address: String = "Your Destination") {
        desMarker?.remove()
        val markerOptions = MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(address)
        desMarker = mMap.addMarker(markerOptions)
        moveCamera(latLng)

        if(desMarker != null)
        {
            Log.i("DetailAdminOrder", "Des Marker: ${desMarker!!.position.latitude}, ${desMarker!!.position.longitude}")
            desMarker!!.isVisible = true
            desMarker!!.showInfoWindow()
        }

    }

    private fun moveCamera(latLng: LatLng) {
        mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f))
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
                            setCurrentLocation(LatLng(location.latitude, location.longitude))
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
}