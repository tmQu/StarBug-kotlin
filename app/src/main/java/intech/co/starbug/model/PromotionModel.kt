package intech.co.starbug.model


import java.io.Serializable
import kotlin.reflect.full.memberProperties


class PromotionModel(
    var id: String = "",
    var name: String = "",
    var img: String = "",
    var discount: Double = 0.0,
    var minimumBill: Double = 0.0,
    var startDay: String = "",
    var endDay: String = "",
    var timestamps: String = "",
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