package intech.co.starbug.model

import java.io.Serializable

class CommentModel(
    var avatar: String = "",
    var user_name: String = "",
    var comment: String = "",
    var rating: Double = 0.0,
    var id_product: String = "",
    var reply: String = ""
) : Serializable