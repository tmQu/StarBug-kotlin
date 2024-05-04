package intech.co.starbug.model

import java.io.Serializable
import java.util.Date

class CommentModel(
    val id: String = "",
    var avatar: String = "",
    var user_name: String = "",
    var comment: String = "",
    var food_rate: Double = 0.0,
    var package_rate: Double = 0.0,
    var delivery_rate: Double = 0.0,
    var rating: Double = 0.0,
    var id_product: String = "",
    var reply: String = "",
    var user_uid: String = "",
    val date_comment: Long = Date().time
) : Serializable {
    fun getDate(): String {
        val date = Date(date_comment)
        return date.toString()
    }
}