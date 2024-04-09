package intech.co.starbug.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.imageview.ShapeableImageView
import intech.co.starbug.R

class SliderAdapter(val listImgUrl: List<String>, val radiusImage:Float = 0F): RecyclerView.Adapter<SliderAdapter.SliderHolderView>(){
    inner class SliderHolderView(private val view: View): RecyclerView.ViewHolder(view)
    {
        private val imgSlider = view.findViewById<ShapeableImageView>(R.id.img_slider)

        fun bind(imgUrl: String) {
            Log.i("SliderAdapter", "bind: $imgUrl")

            Log.i("SliderAdapter", imgSlider.scaleType.toString())


            val radiusInPx = radiusImage * view.resources.displayMetrics.density
            val shapeOverlay =  imgSlider.shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(radiusInPx)
                .build()
            imgSlider.shapeAppearanceModel = shapeOverlay
            imgSlider.load(imgUrl)


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderHolderView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slider_item, parent, false)
        return SliderHolderView(view)
    }

    override fun onBindViewHolder(holder: SliderHolderView, position: Int) {
        return holder.bind(listImgUrl[position])
    }

    override fun getItemCount(): Int {
        return listImgUrl.size
    }
}