package intech.co.starbug.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import intech.co.starbug.R
import kotlin.properties.Delegates

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {


    // This keeps track of the currently selected position
    var selectedPosition by Delegates.observable(0) { property, oldPos, newPos ->
        if (newPos in categories.indices) {
            notifyItemChanged(oldPos)
            notifyItemChanged(newPos)
        }
    }

    fun updateDefaultCategory(){
        selectedPosition = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_button_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        with(holder.itemView)
        {
            val categoryButton: TextView = findViewById(R.id.primaryBtn)
            categoryButton.text = category
            categoryButton.isSelected = (position == selectedPosition)
            Log.i("CategoryAdpater", "${position} ${selectedPosition}")
            if (categoryButton.isSelected){
                categoryButton.setBackgroundColor(Color.parseColor("#C67C4E"))
                categoryButton.setTextColor(Color.parseColor("#FEFEFE"))
            }else {
                categoryButton.setBackgroundColor(Color.parseColor("#00000000"))
                categoryButton.setTextColor(Color.parseColor("#D5D5D7"))

            }
            categoryButton.setOnClickListener{
                selectedPosition = position
                onCategorySelected(category)
            }
        }

    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }


}
