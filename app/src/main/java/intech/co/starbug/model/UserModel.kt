package intech.co.starbug.model

import java.io.Serializable

class UserModel(
    var uid: String = "",
    var email: String = "",
    var name: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var role: String = ""
) : Serializable {
}
