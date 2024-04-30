package intech.co.starbug.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.imageview.ShapeableImageView
import intech.co.starbug.R

class EditImageAdapter(var listImgUrl: MutableList<String>, val listenerDeleteButton: (Int)-> Unit ): RecyclerView.Adapter<EditImageAdapter.SliderHolderView>(){
    inner class SliderHolderView(private val view: View): RecyclerView.ViewHolder(view)
    {
        private val deleteBtn = view.findViewById<ImageButton>(R.id.delete_btn)
        private val imgSlider = view.findViewById<ShapeableImageView>(R.id.img_slider)

        fun bind(position: Int) {
            val imgUrl = listImgUrl[position]
            Log.i("SliderAdapter", "bind: $position ${listImgUrl.size} $imgUrl")

            Log.i("SliderAdapter", imgSlider.scaleType.toString())

            val radiusImage = 10F
            val radiusInPx = radiusImage * view.resources.displayMetrics.density
            val shapeOverlay =  imgSlider.shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(radiusInPx)
                .build()
            imgSlider.shapeAppearanceModel = shapeOverlay
            imgSlider.load(imgUrl)

            deleteBtn.setOnClickListener {
                Log.i("SliderAdapter", " $adapterPosition $position ${listImgUrl.size}")
                listenerDeleteButton(adapterPosition)
                listImgUrl.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)

                deleteBtn.isClickable = false
            }

        }
    }

    fun addImage(url: String){
        listImgUrl.add(url)
        notifyItemInserted(listImgUrl.size - 1)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderHolderView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.edit_image_item, parent, false)
        return SliderHolderView(view)
    }

    override fun onBindViewHolder(holder: SliderHolderView, position: Int) {
        return holder.bind(position)
    }

    override fun getItemCount(): Int {
        return listImgUrl.size
    }
}