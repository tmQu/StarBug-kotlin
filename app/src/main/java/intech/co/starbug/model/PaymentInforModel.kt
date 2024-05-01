package intech.co.starbug.model

import java.io.Serializable
import kotlin.time.Duration

class PaymentInforModel (
  var name: String = "",
  var phone: String = "",
  var avatar: String ="",
  var address: String= "",
  var lat: Double = 0.0,
  var lng: Double = 0.0,
  var note: String= "",
  var paymentMethod: String = "Zalo pay",
  var branchName: String= "",
  var shippingFee: Int = 0,
  var distance: Int = 0, // meter
  var duration: Int = 0 // second
):Serializable{
}