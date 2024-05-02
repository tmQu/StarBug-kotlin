package intech.co.starbug.model

import android.util.Log
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ServerValue.TIMESTAMP
import intech.co.starbug.model.cart.DetailCartItem
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date

class OrderModel(
    val orderDate: Long = Date().time,
    val listCartItem: List<DetailCartItem> = listOf(),
    var paymentInforModel: PaymentInforModel = PaymentInforModel("", "", "", "", 0.0, 0.0, "", ""),
    var uidUser: String = "",
    var status: String = "",
    var orderToken: String = "",
    var price: Int = 0
) : Serializable {
    var id: String = ""
    var reason: String = ""

    fun getTotalProductPrice(): Int {
        var total = 0
        for (item in listCartItem) {
            if(item.product == null)
                continue
            total += item.product!!.price * item.quantity
        }
        return total
    }

    fun genReadableOrderId(): String {
        // only get hhmmss and the price
        val sdf = SimpleDateFormat("hhmmss")
        // only get 2 first number
        val char_2 = getTotalProductPrice().toString().substring(0, 2)
        return "#${sdf.format(orderDate)}${char_2}"
    }
}