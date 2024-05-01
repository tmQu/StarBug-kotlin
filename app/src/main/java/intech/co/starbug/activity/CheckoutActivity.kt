package intech.co.starbug.activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.ShippingPreference
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.AppContext
import com.paypal.checkout.order.OrderRequest
import com.paypal.checkout.order.PurchaseUnit
import com.paypal.checkout.order.Shipping
import com.paypal.checkout.paymentbutton.PaymentButtonContainer
import intech.co.starbug.BuildConfig
import intech.co.starbug.R
import intech.co.starbug.StarbugApp
import intech.co.starbug.activity.authentication.LoginActivity
import intech.co.starbug.adapter.PaymentMethodAdapter
import intech.co.starbug.constants.PaypalConstant
import intech.co.starbug.constants.ZaloPayConstant
import intech.co.starbug.dialog.LoadingDialog
import intech.co.starbug.model.BranchModel
import intech.co.starbug.model.CommentPermission
import intech.co.starbug.model.OrderModel
import intech.co.starbug.model.PaymentInforModel
import intech.co.starbug.model.cart.DetailCartItem
import intech.co.starbug.utils.Utils
import intech.co.starbug.zalopay.CreateOrder
import kotlinx.coroutines.launch
import okhttp3.Callback
//import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener
import java.io.IOException
import java.net.URL
import java.util.Date



class CheckoutActivity : AppCompatActivity() {
    companion object {
        val LIST_PAYMENT_METHOD = listOf("Zalo Pay", "PayPal", "Cash on Delivery")
        val DEFAULT_PAY_METHOD = "Zalo Pay"
    }


    private lateinit var nameEdtv : TextInputEditText
    private lateinit var phoneEdtv : TextInputEditText
    private lateinit var addressTv: TextView
    private lateinit var shippingFee: TextView
    private var shipFee = 0

    private var paymentMethod = DEFAULT_PAY_METHOD
    private var currentPosition = LatLng(0.0, 0.0)
    private lateinit var paymentMethodView: MaterialCardView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var paymentInfor: PaymentInforModel


    private lateinit var listDetailCartItem: List<DetailCartItem>


    private lateinit var totalPriceTv: TextView
    private lateinit var paymentButtonPaypalContainer: PaymentButtonContainer
    override fun onCreate(savedInstanceState: Bundle?) {

        initZaloPay()
        val configPaypal = CheckoutConfig(
            application = application,
            clientId = PaypalConstant.CLIENT_ID,
            environment = Environment.SANDBOX,
            returnUrl = "${BuildConfig.APPLICATION_ID}://paypalpay",
            currencyCode = CurrencyCode.USD,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true,
                showWebCheckout = false,
            ),

        )
        PayPalCheckout.setConfig(configPaypal)

        PayPalCheckout.registerCallbacks(
            onApprove = OnApprove { approval ->
                val orderId = approval.data.orderId
               Log.i("CheckoutActivity", "OrderId: ${orderId}")
                if(orderId != null)
                    saveOrder(orderId)
                else
                    saveOrder()
            },
            onCancel = OnCancel {
                Log.e("CheckoutActivity", "Cancel paypal")
            },
            onError = OnError { errorInfo ->
                Log.e("CheckoutActivity", "${errorInfo.error.message} error")
                Log.e("CheckoutActivity", "Error paypal")

            }
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        nameEdtv = findViewById(R.id.name_input)
        phoneEdtv = findViewById(R.id.phone_input)
        addressTv = findViewById(R.id.address_input)
        shippingFee = findViewById(R.id.shipping_fee)

        totalPriceTv = findViewById(R.id.total_price)

        paymentMethodView = findViewById(R.id.payment_method)

        paymentButtonPaypalContainer = findViewById(R.id.payment_button_container)

        sharedPref = getPreferences(MODE_PRIVATE)
        val paymentInforJson = sharedPref.getString(getString(R.string.saved_payment_infor_key), null)
        if(paymentInforJson != null){
            paymentInfor = convertJsonToPaymentInfor(paymentInforJson)
            nameEdtv.setText(paymentInfor.name)
            phoneEdtv.setText(paymentInfor.phone)
            addressTv.text = paymentInfor.address
            currentPosition = LatLng(paymentInfor.lat, paymentInfor.lng)

        }
        else{
            paymentInfor = PaymentInforModel("", "", "", "", 0.0, 0.0, "", DEFAULT_PAY_METHOD)
        }
        setUpPaymentMethodView()


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            listDetailCartItem = intent.getParcelableArrayListExtra("listDetailOrder", DetailCartItem::class.java)!!
        else
            listDetailCartItem = intent.getParcelableArrayListExtra("listDetailOrder")!!

        setUpPriceView()

        var mLastClickTime = 0L
        findViewById<Button>(R.id.btn_buy).setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){

            }else {
                mLastClickTime = SystemClock.elapsedRealtime();
                buyNow()
            }

        }

        findViewById<TextView>(R.id.edit_btn).setOnClickListener {
            val intent = Intent(this, GetAddressActivity::class.java)
            getAddressLaucher.launch(intent)
        }

        updateShippingFee(paymentInfor.distance)

    }

    private fun setUpPriceView() {
        val totalPrice = calculateTotalPrice()

        val productAllPrice = listDetailCartItem.sumOf { it.getProductPrice() * it.quantity }
        val productPrice = findViewById<TextView>(R.id.product_price)
        productPrice.text = Utils.formatMoney(productAllPrice)

        // set up price view
        totalPriceTv.text = Utils.formatMoney(totalPrice)


    }
    private fun calculateTotalPrice(): Int
    {
        var totalPrice = 0
        totalPrice = listDetailCartItem.sumOf { it.getProductPrice() * it.quantity}
        totalPrice += shipFee


        return totalPrice
    }

    private fun setUpPaymentMethodView()
    {
        initializeBottomSheetPayment()
        val listPayemnt = LIST_PAYMENT_METHOD
        val listLogo = listOf(R.drawable.logo_zalo_pay, R.drawable.logo_momo, R.drawable.logo_cod)

        val adapter = PaymentMethodAdapter(listPayemnt, listLogo){
            paymentMethod = listPayemnt[it]
            with(paymentMethodView){
                findViewById<ImageView>(R.id.logo_img).setImageResource(listLogo[it])
                findViewById<TextView>(R.id.payment_method_name).text = listPayemnt[it]
            }
        }
        val paymentRv = findViewById<RecyclerView>(R.id.payment_rv)
        adapter.setSelected(0)
        paymentRv.adapter = adapter
        paymentRv.layoutManager = LinearLayoutManager(
            this, VERTICAL, false
        )
    }

    private fun convertJsonToPaymentInfor(data: String): PaymentInforModel{
        // convert json to payment infor
        val gson = Gson()
        return gson.fromJson(data, PaymentInforModel::class.java)
    }

    private fun convertPaymentInforToJson(paymentInfor: PaymentInforModel): String
    {
        val res = Gson().toJson(paymentInfor)
        return res
    }

    private fun initializeBottomSheetPayment() {
        val bottomSheet = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet))
        bottomSheet.isHideable = true
        bottomSheet.peekHeight = 0

        paymentMethodView.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }



    private fun getDeliveryInfor()
    {
        // get delivery infor
        paymentInfor.name = nameEdtv.text.toString()
        paymentInfor.phone = phoneEdtv.text.toString()
        paymentInfor.address = addressTv.text.toString()

        paymentInfor.paymentMethod = paymentMethod
        paymentInfor.lat = currentPosition.latitude
        paymentInfor.lng = currentPosition.longitude



    }

    private fun checkDeliveryInfor(): Boolean
    {
        // check delivery infor
        if(nameEdtv.text.toString().isEmpty() || phoneEdtv.text.toString().isEmpty() || addressTv.text.toString().isEmpty() || paymentMethod.isEmpty())
        {
            return false
        }
        return true
    }

    private fun buyNow()
    {
        // buy now
        getDeliveryInfor()

        if(checkDeliveryInfor())
        {

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null)
            {
                if(paymentMethod == LIST_PAYMENT_METHOD[0])
                {
                    // pay by zalo pay
                    payByZaloPay()
                }
                else if(paymentMethod == LIST_PAYMENT_METHOD[1])
                {
                    val vndPrice = calculateTotalPrice()
                    val usdPrice = Utils.convertVNDtoUSD(vndPrice).toString()
                    PayPalCheckout.startCheckout(
                        createOrder =
                        com.paypal.checkout.createorder.CreateOrder { createOrderActions ->
                            val order =
                                OrderRequest(
                                    intent = OrderIntent.CAPTURE,
                                    appContext = AppContext(userAction = UserAction.PAY_NOW,
                                        shippingPreference = ShippingPreference.NO_SHIPPING),
                                    purchaseUnitList =
                                    listOf(
                                        PurchaseUnit(
                                            amount =
                                            Amount(currencyCode = CurrencyCode.USD, value = usdPrice),
                                            shipping = Shipping(address = null)
                                        )
                                    )
                                )
                            Log.i("CheckoutActivity", "Order: ${usdPrice})")
                            createOrderActions.create(order)
                        }
                    )
                }
                else if(paymentMethod == LIST_PAYMENT_METHOD[2])
                {
                    // pay by cash on delivery
                    saveOrder()
                }
                else
                {
                    Log.e("CheckoutActivity", "Payment method is not valid")
                }

            }
            else {
                Toast.makeText(this, "Please login to buy", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        else {
            Log.e("CheckoutActivity", "Delivery infor is not valid")
            Toast.makeText(this, "Please fill in delivery information", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveOrder(token: String = "") {
        saveCurrentPayment()
        val dbRef = FirebaseDatabase.getInstance().getReference("Orders")
        val key = dbRef.push().key
        val listStatus = resources.getStringArray(R.array.order_status)
        if (key != null) {
            val myOrder = OrderModel(Date().time, listDetailCartItem.toList(), paymentInfor, FirebaseAuth.getInstance().currentUser!!.uid, listStatus[0], token, calculateTotalPrice())
            myOrder.id = key
            dbRef.child(key).setValue(myOrder).addOnSuccessListener {
                Log.d("CheckoutActivity", "Order saved successfully")
                // delete cart in DAO database
                val cartItemDao = (application as StarbugApp).dbSQLite.cartItemDAO()
                lifecycleScope.launch {
                    try {
                        for (item in listDetailCartItem) {
                            cartItemDao.deleteCartItem(item.cartItemModel)
                            saveCommentPermission(item.product!!.id)
                        }
                    }
                    finally {
                        finish()
                    }

                }
            }.addOnFailureListener {
                Log.e("CheckoutActivity", "Order save failed")
            }
        }
        else {
            Log.e("CheckoutActivity", "Key is null")
        }
    }

    private fun saveCommentPermission(id: String) {
        val userUid = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("CommentPermission/$id/$userUid")
        dbRef.setValue("true")
    }

    private fun saveCurrentPayment()
    {
        // save payment infor
        getDeliveryInfor()
        val paymentInforJson = convertPaymentInforToJson(paymentInfor)

        with (sharedPref.edit()) {
            putString(getString(R.string.saved_payment_infor_key), paymentInforJson)
            apply()
        }
    }
    override fun onBackPressed() {
        saveCurrentPayment()
        super.onBackPressed()
    }

    var getAddressLaucher =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val address = data?.getStringExtra("address")
                val lat = data?.getDoubleExtra("lat", 0.0)
                val lng = data?.getDoubleExtra("lng", 0.0)
                currentPosition = LatLng(lat!!, lng!!)
                addressTv.text = address
                calculatShippingFee()
            }
        }

    private fun calculatShippingFee() {
        val listBranch = BranchModel.BRANCHES
        var origin = ""
        for (branch in listBranch) {
            origin += "${branch.lat},${branch.lng}|"
        }
        origin = origin.substring(0, origin.length - 1)
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=$origin&destinations=${currentPosition.latitude},${currentPosition.longitude}&key=${BuildConfig.MAPS_API_KEY}"
        // call api with okhttp
        val client = okhttp3.OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()
        //with async
        val loadingDialog = LoadingDialog(this)
        loadingDialog.startLoadingDialog()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("CheckoutActivity", "Error: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@CheckoutActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismissDialog()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string()
                val listDistance = mutableListOf<Int>()
                val listDuration = mutableListOf<Int>()
                if(body != null) {
                    // extract distance and duration
                    val jsonObject = org.json.JSONObject(body)
                    val rows = jsonObject.getJSONArray("rows")
                    for(i in 0 until rows.length()) {
                        val elements = rows.getJSONObject(i).getJSONArray("elements")
                        val element = elements.getJSONObject(0)
                        val status = element.getString("status")
                        if(status == "OK") {
                            val distance = element.getJSONObject("distance").getInt("value")
                            val duration = element.getJSONObject("duration").getInt("value")
                            listDistance.add(distance)
                            listDuration.add(duration)
                        }
                    }
                    // calculate shipping fee
                    var minIndex = -1
                    var minDistance = Int.MAX_VALUE
                    var minTime = Int.MAX_VALUE
                    for (i in 0 until listDistance.size)
                    {
                        if (listDistance[i] < minDistance)
                        {
                            minDistance = listDistance[i]
                            minTime = listDuration[i]
                            minIndex = i
                        }
                    }
                    paymentInfor.branchName = listBranch[minIndex].name
                    paymentInfor.distance = minDistance
                    paymentInfor.duration = minTime
                    loadingDialog.dismissDialog()
                    // Only the original thread that created a view hierarchy can touch its views
                    Handler(Looper.getMainLooper()).post {
                        updateShippingFee(minDistance)
                    }
                }
            }
        })


    }

    private fun updateShippingFee(minDistance: Int) {

        val kmDistance = minDistance / 1000
        var fee = 0
        if(kmDistance <= 2) {
            fee = kmDistance * 16500
        }
        else
            fee = 2 * 16500 + (kmDistance - 2) * 5500
        shippingFee.text = Utils.formatMoney(fee)
        Log.i("CheckoutActivity", "Shipping fee: $fee")
        paymentInfor.shippingFee = fee
        shipFee = fee
        setUpPriceView()
    }

    @SuppressLint("SetTextI18n")
    private fun payByZaloPay() {
        // pay by zalo pay
        val orderApi = CreateOrder()

        try {
            Log.i("CheckoutActivity", "Amount i: ${calculateTotalPrice()}")
            val amount = calculateTotalPrice().toString()

            val data = orderApi.createOrder(amount)

            val code = data!!.getString("return_code")
            Toast.makeText(applicationContext, "return_code: $code", Toast.LENGTH_LONG).show()
            Log.i("CheckoutActivity", "Code: $code")
            if (code == "1") {
                val token = data!!.getString("zp_trans_token")
                Log.i("Amount", token)
                payZalo(token)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }
    private fun payZalo(token: String)
    {
        Log.i("CheckoutActivity", "Token: $token")
        ZaloPaySDK.getInstance()
            .payOrder(this, token, "demozpdk://app", object : PayOrderListener {
                override fun onPaymentSucceeded(
                    transactionId: String,
                    transToken: String,
                    appTransID: String
                ) {
                    saveOrder()

                    runOnUiThread {
                        AlertDialog.Builder(this@CheckoutActivity)
                            .setTitle("Payment Success")
                            .setMessage(
                                String.format(
                                    "TransactionId: %s - TransToken: %s",
                                    transactionId,
                                    transToken
                                )
                            )
                            .setPositiveButton("OK",
                                DialogInterface.OnClickListener { dialog, which -> })
                            .setNegativeButton("Cancel", null).show()
                    }
                }

                override fun onPaymentCanceled(zpTransToken: String, appTransID: String) {
                    AlertDialog.Builder(this@CheckoutActivity)
                        .setTitle("User Cancel Payment")
                        .setMessage(String.format("zpTransToken: %s \n", zpTransToken))
                        .setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, which -> })
                        .setNegativeButton("Cancel", null).show()
                }

                override fun onPaymentError(
                    zaloPayError: ZaloPayError,
                    zpTransToken: String,
                    appTransID: String
                ) {
                    if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) {
                        ZaloPaySDK.getInstance().navigateToZaloOnStore(applicationContext)
                    }
                }
            })
    }

    private fun initZaloPay() {
        // init zalo pay
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // ZaloPay SDK Init
        ZaloPaySDK.init(ZaloPayConstant.APP_ID, vn.zalopay.sdk.Environment.SANDBOX)
    }



    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent);
    }

}


