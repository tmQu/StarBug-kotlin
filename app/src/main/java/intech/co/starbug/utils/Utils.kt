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

        fun convertCommentDate(date: Long): String
        {
            val dateRemain = getPassDate(date)
            if(dateRemain == 0)
            {
                return "Today"
            }
            else if(dateRemain == 1)
            {
                return "Yesterday"
            }
            else
            {
                if (dateRemain  > 30)
                {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                    return dateFormat.format(Date(date))
                }
                return dateRemain.toString() + " days ago"
            }
        }

        fun convertVNDtoUSD(calculateTotalPrice: Int): Double {
            // get the ratio from the inter
            val ratio = 25000
            val usd = calculateTotalPrice.toDouble() / 23000
            // get 2 decimal precision
            return (usd * 100.0).roundToInt() / 100.0
        }

        fun formatAvgRate(avgRate: Double): String {
            return String.format("%.1f", avgRate)
        }

        fun getRemainDate(endDate: Long): Int {
            val currentDate = Date()
            val diff = endDate - currentDate.time
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

        fun getPassDate(endDate: Long): Int {
            val currentDate = Date()
            val diff = currentDate.time - endDate
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }
    }


}