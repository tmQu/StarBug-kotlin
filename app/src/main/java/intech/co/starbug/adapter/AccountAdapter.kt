package intech.co.starbug.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import intech.co.starbug.R
import intech.co.starbug.model.AccountModel

class AccountAdapter(context: Context, private val itemList: List<AccountModel>) : ArrayAdapter<AccountModel>(context, R.layout.account_layout, itemList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.account_layout, parent, false)

        val itemNameTextView: TextView = view.findViewById(R.id.AccountNameTextView)
        val itemEmailTextView: TextView = view.findViewById(R.id.GmailTextView)
        val itemRoleTextView: TextView = view.findViewById(R.id.RoleTextView)

        val currentItem = itemList[position]
        itemNameTextView.text = currentItem.Name
        itemEmailTextView.text = currentItem.Email
        itemRoleTextView.text = currentItem.Role

        return view
    }
}