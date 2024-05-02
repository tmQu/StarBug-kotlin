package intech.co.starbug.adapter

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import intech.co.starbug.PromotionAdapter
import intech.co.starbug.R
import intech.co.starbug.model.PromotionModel
import intech.co.starbug.utils.Utils
import kotlin.properties.Delegates

class PromotionSelectionAdapter(val promotionList: List<PromotionModel>, val totalBill: Int,val onItemClickListener: (Int, Boolean) -> Unit) : RecyclerView.Adapter<PromotionSelectionAdapter.ViewHodlder>(){
    var lastSelected by Delegates.observable(-1) { property, oldPos, newPos ->
        if(oldPos == newPos)
        {
            selectedList[oldPos] = !selectedList[oldPos]
            notifyItemChanged(oldPos)
        }
        else {
            if(oldPos in promotionList.indices)
            {
                selectedList[oldPos] = !selectedList[oldPos]
                notifyItemChanged(oldPos)
            }
            if (newPos in promotionList.indices) {
                selectedList[newPos] = !selectedList[newPos]
                notifyItemChanged(newPos)
            }
        }

    }
    var selectedList = BooleanArray(promotionList.size)

    inner class ViewHodlder(itemView: View)  : RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {
                val checked = !selectedList[adapterPosition]
                Log.i("PromotionSelectionAdapter", "onItemClick: $adapterPosition, $checked")
                onItemClickListener(adapterPosition, checked)
                lastSelected = adapterPosition
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHodlder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.promotion_row, parent, false)
        return ViewHodlder(layout)
    }

    override fun onBindViewHolder(holder: ViewHodlder, position: Int) {
        with(holder.itemView){
            val bgImage = findViewById<ImageView>(R.id.bg_image)
            val promotionName = findViewById<TextView>(R.id.promo_name)
            val promotionDiscount = findViewById<TextView>(R.id.promo_discount)
            val promotionMinPrice = findViewById<TextView>(R.id.promo_min_price)
            val promoDate = findViewById<TextView>(R.id.promo_date)

            bgImage.load(promotionList[position].img) {
                target(
                    onError = {
                        bgImage.setBackgroundColor(resources.getColor(com.paypal.pyplcheckout.R.color.lightGrey))
                    }
                )
            }
            promotionName.text = promotionList[position].name
            promotionDiscount.text = "${promotionList[position].discount} %"

            val minPrice = Utils.formatMoney(promotionList[position].minimumBill.toInt())
            promotionMinPrice.text = "Apply for bill from $minPrice"

            val remainDate = Utils.getRemainDate(promotionList[position].getEndDate())
            promoDate.text = "Remain $remainDate days"

            val checkBox = findViewById<CheckBox>(R.id.promotion_check_box)
            if(promotionList[position].minimumBill > totalBill)
            {
                checkBox.isEnabled = false
                this.isEnabled = false
                this.alpha = 0.5f
            }
            else {
                checkBox.isEnabled = true
                checkBox.setOnClickListener{
                    this.performClick()
                }


            }


            if(selectedList[position]) {
                checkBox.isChecked = true
//                (this as Lie).strokeColor = resources.getColor(R.color.colorSecondPrimary)
            }
            else {
                checkBox.isChecked = false
//                (this as MaterialCardView).strokeColor = resources.getColor(R.color.LightGrey)

            }
        }
    }

    override fun getItemCount(): Int {
        return promotionList.size
    }
}