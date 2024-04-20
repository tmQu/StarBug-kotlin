package intech.co.starbug.model

import java.io.Serializable

class AccountModel (
    var Name: String = "",
    var Username: String = "",
    var Email: String = "",
    var Phone: String = "",
    var Password: String = "",
    var Role: String = "",
) : Serializable
