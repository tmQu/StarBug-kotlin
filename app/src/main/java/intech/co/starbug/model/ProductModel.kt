package intech.co.starbug.model

import java.io.Serializable

class ProductModel(
    var id: String = "",
    var name: String = "",
    var category: String = "",
    var img: List<String> = listOf(),
    var price: Int = 100,
    var medium_price: Int = 0,
    var large_price: Int = 0,
    var avgRate: Double = 0.0,
    var description: String = ""
): Serializable
{



}