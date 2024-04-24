package intech.co.starbug.zalopay


import android.util.Log
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Date

import intech.co.starbug.zalopay.helper.Helpers
import intech.co.starbug.constants.ZaloPayConstant
import okhttp3.FormBody


class CreateOrder {
    inner class CreateOrderData(amount: String) {
        var AppId: String
        var AppUser: String
        var AppTime: String
        var Amount: String
        var AppTransId: String
        var EmbedData: String
        var Items: String
        var BankCode: String
        var Description: String
        var Mac: String

        init {
            Log.d("Amount", "Create order $amount")
            val appTime = Date().time
            AppId = java.lang.String.valueOf(ZaloPayConstant.APP_ID)
            AppUser = "Android_Demo"
            AppTime = appTime.toString()
            Amount = amount
            AppTransId = Helpers.appTransId
            EmbedData = "{}"
            Items = "[]"
            BankCode = "zalopayapp"
            Description = "Merchant pay for order #" + Helpers.appTransId
            val inputHMac = String.format(
                "%s|%s|%s|%s|%s|%s|%s",
                AppId,
                AppTransId,
                AppUser,
                Amount,
                AppTime,
                EmbedData,
                Items
            )
            Mac = Helpers.getMac(ZaloPayConstant.MAC_KEY, inputHMac)
            Log.d("Amount", "Create order $amount after")

        }
    }

    @Throws(Exception::class)
    fun createOrder(amount: String): JSONObject? {
        val input = CreateOrderData(amount)
        val formBody: RequestBody = FormBody.Builder()
            .add("app_id", input.AppId)
            .add("app_user", input.AppUser)
            .add("app_time", input.AppTime)
            .add("amount", input.Amount)
            .add("app_trans_id", input.AppTransId)
            .add("embed_data", input.EmbedData)
            .add("item", input.Items)
            .add("bank_code", input.BankCode)
            .add("description", input.Description)
            .add("mac", input.Mac)
            .build()

        val data = HttpProvider.sendPost(ZaloPayConstant.URL_CREATE_ORDER, formBody)
        Log.i("Amount", "Create order2 $amount")

        return data
    }
}


