package intech.co.starbug.utils

class Utils {
    companion object {
        fun formatMoney(money: Int ): String
        {
            val formatter = java.text.DecimalFormat("#,###")
            return "${formatter.format(money)} VNĐ"
        }
    }


}