package intech.co.starbug.model

import java.io.Serializable
import kotlin.reflect.full.memberProperties

data class FeedbackModel(
    var id: String = "",
    var description: String = "",
    var img: List<String> = listOf(),
    var senderName: String = "",
    var senderPhoneNumber: String = "0909090909",
    var time: String = "19/04/2024"
): Serializable
{
    fun setter(key: String, value: Any) {
        val property = this::class.memberProperties.find { it.name == key }
        property?.let {
            val propertyField = this::class.java.getDeclaredField(key)
            propertyField.isAccessible = true
            when (it.returnType.toString()) {
                "kotlin.String" -> propertyField.set(this, value.toString())
                "kotlin.Double" -> propertyField.setDouble(this, value.toString().toDoubleOrNull() ?: 0.0)
            }
        }
    }

}