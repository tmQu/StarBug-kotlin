package intech.co.starbug.activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import intech.co.starbug.R
import intech.co.starbug.adapter.PaymentMethodAdapter
import intech.co.starbug.model.PaymentInforModel

class CheckoutActivity : AppCompatActivity() {

    private lateinit var nameEdtv : TextInputEditText
    private lateinit var phoneEdtv : TextInputEditText
    private lateinit var addressTv: TextView

    var paymentMethod = ""
    private lateinit var paymentMethodView: MaterialCardView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var paymentInfor: PaymentInforModel
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