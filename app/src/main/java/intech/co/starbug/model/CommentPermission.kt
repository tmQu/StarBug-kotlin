package intech.co.starbug.model

class CommentPermission (
    var productId: String = "",
    val userUID: List<String> = mutableListOf<String>()
){
}