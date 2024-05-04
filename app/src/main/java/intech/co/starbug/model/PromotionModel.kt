package intech.co.starbug.model


import java.io.Serializable
import java.text.SimpleDateFormat
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
    var listUser: List<String> = listOf()
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

    fun getStartDate(): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val date = sdf.parse(startDay)
        return date.time
    }

    fun getEndDate(): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val date = sdf.parse(endDay)
        return date.time
    }

    fun isActive(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime in getStartDate()..getEndDate()
    }



}