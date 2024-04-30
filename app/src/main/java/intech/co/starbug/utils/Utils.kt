package intech.co.starbug.utils

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

class Utils {
    companion object {
        fun formatMoney(money: Int ): String
        {
            val formatter = java.text.DecimalFormat("#,###")
            return "${formatter.format(money)} VNĐ"
        }

        fun convertDate(dateLong: Long): String {
            val date = SimpleDateFormat("hh:mm:ss dd/MM/yyyy").format(Date(dateLong))
            return date
        }

        fun convertVNDtoUSD(calculateTotalPrice: Int): Double {
            // get the ratio from the inter
            val ratio = 25000
            val usd = calculateTotalPrice.toDouble() / 23000
            // get 2 decimal precision
            return (usd * 100.0).roundToInt() / 100.0
        }
    }


}