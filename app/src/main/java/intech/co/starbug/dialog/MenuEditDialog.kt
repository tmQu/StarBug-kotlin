package intech.co.starbug.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.imageview.ShapeableImageView
import intech.co.starbug.R
import intech.co.starbug.model.ProductModel
import intech.co.starbug.model.cart.CartItemModel
import intech.co.starbug.model.cart.DetailCartItem
import intech.co.starbug.utils.Utils


private const val HOT_OPTION = 0
private const val ICED_OPTION = 1

class MenuEditDialog(var currentCartItem: DetailCartItem): DialogFragment() {
    internal lateinit var listener: DialogListener
    private lateinit var btnAddCart: Button
    private lateinit var sizeGroup: RadioGroup
    private lateinit var iceGroup: RadioGroup
    private lateinit var sugarGroup: RadioGroup
    private lateinit var tempGroup: RadioGroup
    private lateinit var addBtn: ShapeableImageView
    private lateinit var removeBtn: ShapeableImageView
    private lateinit var quantityView: TextView
    private lateinit var totalPrice: TextView


    private lateinit var cartItem: CartItemModel
    interface DialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, className: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        cartItem = currentCartItem.cartItemModel
        try {
            // Verify that the host activity implements the callback interface
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement Dialog Listner"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder =  AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;

            val viewDialog = inflater.inflate(R.layout.menu_option_layout, null)
            with(viewDialog) {
                sugarGroup = findViewById(R.id.sugarGroup)
                sizeGroup = findViewById(R.id.sizeGroup)
                iceGroup = findViewById(R.id.iceGroup)
                tempGroup = findViewById(R.id.tempGroup)
                btnAddCart = findViewById(R.id.add_cart_btn)
                addBtn = findViewById(R.id.add_quantity_btn)
                removeBtn = findViewById(R.id.remove_quantity_btn)
                totalPrice = findViewById(R.id.total_price)
                quantityView = findViewById(R.id.quantity)
            }
            builder
                .setView(viewDialog)

//            viewDialog.findViewById<Button>(R.id.yes_btn).setOnClickListener {
//                listener.onYesButton()
//                this.dismiss()
//            }
//            viewDialog.findViewById<Button>(R.id.no_btn).setOnClickListener {
//                this.dismiss()
//            }



            builder.create()
        }?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initializeTheMenuOption()
    {
        val product = currentCartItem.product?: ProductModel()
        if(product.medium_price == -1){
            sizeGroup.getChildAt(1).visibility = View.GONE
        }
        if(product.large_price == -1)
        {
            sizeGroup.getChildAt(2).visibility = View.GONE
        }
        sizeGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadio = group.findViewById<RadioButton>(checkedId)
            if(checkedRadio.text == "S"){
                cartItem.size = "S"
            }
            else if(checkedRadio.text == "M")
            {
                cartItem.size = "M"
            }
            else{
                cartItem.size = "M"
            }
            showTheTotalPrice()
        }

        if (currentCartItem.size == "S")
            sizeGroup.check(sizeGroup.getChildAt(0).id)
        else if (currentCartItem.size == "M")
            sizeGroup.check(sizeGroup.getChildAt(1).id)
        else
            sizeGroup.check(sizeGroup.getChildAt(2).id)



        iceGroup.setOnCheckedChangeListener { group, checkedId ->
            resetAllTheRadioButton(group)
            val checkedRadio = group.findViewById<RadioButton>(checkedId)
            checkedRadio.alpha = 1f
            val idx = group.indexOfChild(checkedRadio)
            cartItem.amountIce = resources.getStringArray(R.array.ice_option)[idx]
        }
//        setDefaultMenu(iceGroup)


        if(product.tempOption) {
            val tempOption = resources.getStringArray(R.array.temp_option)
            tempGroup.setOnCheckedChangeListener { group, checkedId ->
                resetAllTheRadioButton(group)
                val checkedRadio = group.findViewById<RadioButton>(checkedId)
                checkedRadio.alpha = 1f
                val idx = group.indexOfChild(checkedRadio)
                cartItem.temperature = tempOption[idx]
                if(idx == HOT_OPTION)
                {
                    iceGroup.visibility = View.INVISIBLE
                }
                else {
                    iceGroup.visibility = View.VISIBLE
                }
            }
//            setDefaultMenu(tempGroup)

        }
        else {
//            findViewById<LinearLayout>(R.id.temp_view).visibility = View.GONE
//            if(product.iceOption == false)
//                findViewById<LinearLayout>(R.id.ice_view).visibility = View.GONE

        }



        if(product.sugarOption) {
            sugarGroup.setOnCheckedChangeListener { group, checkedId ->
                resetAllTheRadioButton(group)
                val checkedRadio = group.findViewById<RadioButton>(checkedId)
                checkedRadio.alpha = 1f
                val idx = group.indexOfChild(checkedRadio)
                cartItem.amountSugar = resources.getStringArray(R.array.sugar_option)[idx]
            }
//            setDefaultMenu(sugarGroup)
        }
        else {
//            findViewById<LinearLayout>(R.id.sugar_view).visibility = View.GONE
        }


    }

    private fun resetAllTheRadioButton(radioGroup: RadioGroup)
    {
        val childCount = radioGroup.childCount
        for (i in 0 until childCount)
        {
            radioGroup.getChildAt(i).alpha = 0.3f
        }
    }


    private fun handleQuantityButton()
    {
        addBtn.setOnClickListener{
            cartItem.quantity += 1
            quantityView.text = cartItem.quantity.toString()
            showTheTotalPrice()
        }

        removeBtn.setOnClickListener {
            if(cartItem.quantity == 1)
            {
                // Toast
            }
            else {
                cartItem.quantity -= 1
                quantityView.text = cartItem.quantity.toString()
                showTheTotalPrice()
            }
        }
    }

    private fun showTheTotalPrice()
    {
        val totalMoney = cartItem.quantity *  cartItem.getProductPrice(currentCartItem.product?:ProductModel())
        totalPrice.text = Utils.formatMoney(totalMoney)
    }

}


