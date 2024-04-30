package intech.co.starbug.model

class PaymentInforModel (
  var name: String = "",
  var phone: String = "",
  var address: String= "",
  var lat: Double = 0.0,
  var lng: Double = 0.0,
  var note: String= "",
  var paymentMethod: String = "Zalo pay",
){
}