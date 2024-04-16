package intech.co.starbug.utils

import java.text.SimpleDateFormat
import java.util.Date

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
    }


}