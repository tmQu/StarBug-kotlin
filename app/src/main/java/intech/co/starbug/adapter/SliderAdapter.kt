package intech.co.starbug.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import intech.co.starbug.R

class SliderAdapter(val listImgUrl: List<String>): RecyclerView.Adapter<SliderAdapter.SliderHolderView>(){
    inner class SliderHolderView(private val view: View): RecyclerView.ViewHolder(view)
    {
        private val imgSlider = view.findViewById<ImageView>(R.id.img_slider)

        fun bind(imgUrl: String) {
            imgSlider.load(imgUrl) {
                transformations(RoundedCornersTransformation(8f))
            }
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