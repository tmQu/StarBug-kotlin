package intech.co.starbug.model

class PaymentInforModel (
  var name: String,
  var phone: String,
  var address: String,
  var note: String,
  var paymentMethod: String = "Zalo pay",
){
}