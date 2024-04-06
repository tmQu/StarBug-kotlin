package intech.co.starbug.model


import java.io.Serializable
import kotlin.reflect.full.memberProperties


class ProductModel(
    var id: String = "",
    var name: String = "",
    var category: String = "",
    var img: List<String> = listOf(),
    var price: Int = 100,
    var medium_price: Int = 0,
    var large_price: Int = 0,
    var avgRate: Double = 0.0,
    var description: String = "",
    var tempOption: Boolean = true,
    var iceOption: Boolean = true,
    var sugarOption: Boolean = true,
): Serializable
{
    fun setter(key: String, value: Any) {
        val property = this::class.memberProperties.find { it.name == key }
        property?.let {
            val propertyField = this::class.java.getDeclaredField(key)
            propertyField.isAccessible = true
            when (it.returnType.toString()) {
                "kotlin.String" -> propertyField.set(this, value.toString())
                "kotlin.Int" -> propertyField.setInt(this, value.toString().toIntOrNull() ?: 0)
                "kotlin.Double" -> propertyField.setDouble(this, value.toString().toDoubleOrNull() ?: 0.0)
                "kotlin.Boolean" -> propertyField.setBoolean(this, value.toString().toBoolean())
            }
        }
    }


}