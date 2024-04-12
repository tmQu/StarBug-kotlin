package intech.co.starbug.model

import android.util.Log
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ServerValue.TIMESTAMP
import intech.co.starbug.model.cart.DetailCartItem
import java.text.SimpleDateFormat
import java.util.Date

class OrderModel(
    val listCartItem: MutableList<DetailCartItem>,
    var status: String
){
    val id: String = ""
    var paymentInforModel: PaymentInforModel = PaymentInforModel("", "", "", "")
//    var status: String = "Waiting approved"
    var orderDate: Long =  Date().time
    var uidUser: String = ""

    fun getTotalPrice(): Int {
        var total = 0
        for (item in listCartItem) {
            if(item.product == null)
                continue
            total += item.product!!.price * item.quantity
        }
        return total
    }

    fun getOrderDate(): String {
        val date = SimpleDateFormat("hh:mm:ss dd/MM/yyyy").format(Date(orderDate))
        return date
    }
}