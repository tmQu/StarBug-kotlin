package intech.co.starbug.model

import android.util.Log
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ServerValue.TIMESTAMP
import intech.co.starbug.model.cart.DetailCartItem
import java.text.SimpleDateFormat
import java.util.Date

class OrderModel(
    val orderDate: Long = Date().time,
    val listCartItem: List<DetailCartItem> = listOf(),
    var paymentInforModel: PaymentInforModel = PaymentInforModel("", "", "", 0.0, 0.0, "", ""),
    var uidUser: String = "",
    var status: String = "",
    var orderToken: String = ""
){
    var id: String = ""

    


    fun getTotalPrice(): Int {
        var total = 0
        for (item in listCartItem) {
            if(item.product == null)
                continue
            total += item.product!!.price * item.quantity
        }
        return total
    }
}