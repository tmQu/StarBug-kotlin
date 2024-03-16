package intech.co.starbug.model

class CommentModel {
     var avatar = ""
     var user_name = ""
     var comment = ""
     var rating : Double = 0.0
     var id_product: String = ""

    constructor(
        avatar: String,
        user_name: String,
        comment: String,
        rating: Double,
        id_product: String
    ) {
        this.avatar = avatar
        this.user_name = user_name
        this.comment = comment
        this.rating = rating
        this.id_product = id_product
    }
}