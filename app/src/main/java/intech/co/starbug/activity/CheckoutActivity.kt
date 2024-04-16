package intech.co.starbug.activity

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import intech.co.starbug.R
import intech.co.starbug.StarbugApp
import intech.co.starbug.adapter.PaymentMethodAdapter
import intech.co.starbug.model.OrderModel
import intech.co.starbug.model.PaymentInforModel
import intech.co.starbug.model.cart.DetailCartItem
import intech.co.starbug.utils.Utils
import kotlinx.coroutines.launch
import java.util.Date

class CheckoutActivity : AppCompatActivity() {

    private lateinit var nameEdtv : TextInputEditText
    private lateinit var phoneEdtv : TextInputEditText
    private lateinit var addressTv: TextView

    var paymentMethod = "Zalo Pay"
    private lateinit var paymentMethodView: MaterialCardView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var paymentInfor: PaymentInforModel


    private lateinit var listDetailCartItem: List<DetailCartItem>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        nameEdtv = findViewById(R.id.name_input)
        phoneEdtv = findViewById(R.id.phone_input)
        addressTv = findViewById(R.id.address_input)

        paymentMethodView = findViewById(R.id.payment_method)

        sharedPref = getPreferences(MODE_PRIVATE)
        val paymentInforJson = sharedPref.getString(getString(R.string.saved_payment_infor_key), null)
        if(paymentInforJson != null){
            paymentInfor = convertJsonToPaymentInfor(paymentInforJson)
            nameEdtv.setText(paymentInfor.name)
            phoneEdtv.setText(paymentInfor.phone)
            addressTv.text = paymentInfor.address
        }
        else{
            paymentInfor = PaymentInforModel("", "", "", "")
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
                Log.i("CheckoutActivity", "Buy now")
                mLastClickTime = SystemClock.elapsedRealtime();
                buyNow()
            }

        }
    }

    private fun setUpPriceView() {
        val totalPrice = listDetailCartItem.sumOf { it.getProductPrice() * it.quantity}
        val productPrice = findViewById<TextView>(R.id.product_price)
        productPrice.text = Utils.formatMoney(totalPrice)

        // set up price view
        val priceTv = findViewById<TextView>(R.id.total_price)
        priceTv.text = Utils.formatMoney(totalPrice)


    }

    private fun setUpPaymentMethodView()
    {
        initializeBottomSheetPayment()
        val listPayemnt = listOf("Zalo pay", "Mo Mo","Cash on Delivery")
        val listLogo = listOf(R.drawable.logo_zalo_pay, R.drawable.logo_momo, R.drawable.logo_cod)

        val adapter = PaymentMethodAdapter(listPayemnt, listLogo){
            paymentMethod = listPayemnt[it]
            with(paymentMethodView){
                findViewById<ImageView>(R.id.logo_img).setImageResource(listLogo[it])
                findViewById<TextView>(R.id.payment_method_name).text = listPayemnt[it]
            }
        }
        val paymentRv = findViewById<RecyclerView>(R.id.payment_rv)

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
                // save order to firebase
                saveOrder()
            }
            else {
                Log.e("CheckoutActivity", "User is null")

            }
        }
        else {
            Log.e("CheckoutActivity", "Delivery infor is not valid")
        }
    }

    private fun saveOrder() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Orders")
        val key = dbRef.push().key
        val listStatus = resources.getStringArray(R.array.order_status)
        if (key != null) {
            val myOrder = OrderModel(Date().time, listDetailCartItem.toList(), paymentInfor, FirebaseAuth.getInstance().currentUser!!.uid, listStatus[0])
            myOrder.id = key
            dbRef.child(key).setValue(myOrder).addOnSuccessListener {
                Log.d("CheckoutActivity", "Order saved successfully")
                // delete cart in DAO database
                val cartItemDao = (application as StarbugApp).dbSQLite.cartItemDAO()
                lifecycleScope.launch {
                    try {
                        for (item in listDetailCartItem) {
                            cartItemDao.deleteCartItem(item.cartItemModel)
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


    override fun onBackPressed() {
        // save payment infor
        getDeliveryInfor()
        val paymentInforJson = convertPaymentInforToJson(paymentInfor)

        with (sharedPref.edit()) {
            putString(getString(R.string.saved_payment_infor_key), paymentInforJson)
            apply()
        }
        super.onBackPressed()
    }


}