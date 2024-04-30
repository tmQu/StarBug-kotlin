package intech.co.starbug.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import intech.co.starbug.activity.AccountSettingActivity
import intech.co.starbug.R

class AccountFragment : Fragment() {
    private lateinit var logout: Button
    private lateinit var order: Button
    private lateinit var accountSetting: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        logout = view.findViewById(R.id.logout)
        order = view.findViewById(R.id.order)
        accountSetting = view.findViewById(R.id.account_setting)

        accountSetting.setOnClickListener {
            requireActivity().startActivity(Intent(requireContext(), AccountSettingActivity::class.java))
        }

        logout.setOnClickListener {
            // Logout
        }

        order.setOnClickListener {
            // Orders
        }

        return view
    }
}