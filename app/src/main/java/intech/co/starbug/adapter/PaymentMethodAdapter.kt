package intech.co.starbug.adapter

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import intech.co.starbug.R
import kotlin.properties.Delegates

class PaymentMethodAdapter(val listPaymethod: List<String>, val listLogo: List<Int>, val onItemClickListener: (Int) -> Unit) : RecyclerView.Adapter<PaymentMethodAdapter.ViewHodlder>(){
    var lastSelected by Delegates.observable(0) { property, oldPos, newPos ->
        if (newPos in listPaymethod.indices) {
            selectedList[oldPos] = false
            selectedList[newPos] = true
            notifyItemChanged(oldPos)
            notifyItemChanged(newPos)
        }
    }
    var selectedList = BooleanArray(listPaymethod.size)

    inner class ViewHodlder(itemView: View)  : RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                onItemClickListener(adapterPosition)
                lastSelected = adapterPosition
            }
        }
    }

    fun setSelected(position: Int){
        selectedList[position] = true
        onItemClickListener(position)
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHodlder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.payment_method_layout, parent, false)
        return ViewHodlder(layout)
    }

    override fun onBindViewHolder(holder: ViewHodlder, position: Int) {
        with(holder.itemView){
            val logo = findViewById<ShapeableImageView>(R.id.logo_img)
            val name = findViewById<TextView>(R.id.payment_method_name)
            val checkBox = findViewById<CheckBox>(R.id.payment_check_box)

            logo.setImageResource(listLogo[position])
            name.text = listPaymethod[position]
            checkBox.setOnClickListener{
                this.performClick()
            }
            if(selectedList[position]) {
                checkBox.isChecked = true
                (this as MaterialCardView).strokeColor = resources.getColor(R.color.colorSecondPrimary)
            }
            else {
                checkBox.isChecked = false
                (this as MaterialCardView).strokeColor = resources.getColor(R.color.black)

            }
        }
    }

    override fun getItemCount(): Int {
        return listPaymethod.size
    }
}