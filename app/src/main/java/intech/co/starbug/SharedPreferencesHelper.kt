package intech.co.starbug

import android.content.Context
import com.google.gson.Gson
import intech.co.starbug.model.UserModel

class SharedPreferencesHelper(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val USER_KEY = "user"
        private const val USER_UID_KEY = "uid"
        private const val USER_EMAIL_KEY = "email"
        private const val USER_NAME_KEY = "name"
        private const val USER_PASSWORD_KEY = "password"
        private const val USER_PHONE_NUMBER_KEY = "phoneNumber"
        private const val USER_ROLE_KEY = "role"
    }

    fun saveUser(user: UserModel) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_UID_KEY, user.uid)
        editor.putString(USER_EMAIL_KEY, user.email)
        editor.putString(USER_NAME_KEY, user.name)
        editor.putString(USER_PASSWORD_KEY, user.password)
        editor.putString(USER_PHONE_NUMBER_KEY, user.phoneNumber)
        editor.putString(USER_ROLE_KEY, user.role)
        editor.apply()
    }

    fun getUser(): UserModel? {
        val uid = sharedPreferences.getString(USER_UID_KEY, null)
        val email = sharedPreferences.getString(USER_EMAIL_KEY, null)
        val name = sharedPreferences.getString(USER_NAME_KEY, null)
        val password = sharedPreferences.getString(USER_PASSWORD_KEY, null)
        val phoneNumber = sharedPreferences.getString(USER_PHONE_NUMBER_KEY, null)
        val role = sharedPreferences.getString(USER_ROLE_KEY, null)

        return if (uid != null &&
            email != null &&
            name != null &&
            password != null &&
            phoneNumber != null &&
            role != null) {
            UserModel(uid, email, name, password, phoneNumber, role)
        } else {
            null
        }
    }

    fun getUid(): String? {
        return sharedPreferences.getString(USER_UID_KEY, null)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(USER_EMAIL_KEY, null)
    }

    fun getName(): String? {
        return sharedPreferences.getString(USER_NAME_KEY, null)
    }

    fun getPassword(): String? {
        return sharedPreferences.getString(USER_PASSWORD_KEY, null)
    }

    fun getPhoneNumber(): String? {
        return sharedPreferences.getString(USER_PHONE_NUMBER_KEY, null)
    }

    fun getRole(): String? {
        return sharedPreferences.getString(USER_ROLE_KEY, null)
    }
}
