package intech.co.starbug.model

import java.io.Serializable

data class FeedbackModel(
    val title: String,
    val description: String,
    val imageUrl: String,
    val senderName: String,
    val senderPhoneNumber: String
): Serializable
{
    constructor(): this("","","","","")
}
