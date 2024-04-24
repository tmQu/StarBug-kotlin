package intech.co.starbug.zalopay

import android.util.Log
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.TlsVersion
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


object HttpProvider {
    fun sendPost(URL: String, formBody: RequestBody): JSONObject? {
        var data:JSONObject? = JSONObject()
        try {
            val spec: ConnectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                    CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                    CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                )
                .build()
            val client: OkHttpClient = OkHttpClient.Builder()
                .connectionSpecs(listOf<ConnectionSpec>(spec))
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .build()
            val request: Request = Request.Builder()
                .url(URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.i("Amount", "Create order failed")
                Log.println(Log.ERROR, "BAD_REQUEST", response.body?.string()?: "No response")
                data = null
            } else {
                val jsonString = response.body?.string()
                Log.i("Amount", "Create order success ${jsonString}")

                data = JSONObject(jsonString)

                Log.i("Amount", "Create order2")

            }
        } catch (e: IOException) {
            Log.i("Amount", "Create order ${e.message}")

            e.printStackTrace()
        } catch (e: JSONException) {
            Log.i("Amount", "Create order ${e.message}")

            e.printStackTrace()
        }
        Log.i("Amount", "Bf return")
        return data
    }
}

