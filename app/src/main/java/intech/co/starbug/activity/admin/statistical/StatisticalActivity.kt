package intech.co.starbug.activity.admin.statistical

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import intech.co.starbug.R
import intech.co.starbug.model.OrderModel
import java.lang.Float.max
import java.text.SimpleDateFormat
import java.util.Date


class StatisticalActivity : AppCompatActivity() {

    private lateinit var barChartRevenue: CombinedChart
    private var todayRevenue = 0.0
    private var todayOrder = 0
    private var todaySuccessOrder = 0
    private var todayFailedOrder = 0

    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistical)

        barChartRevenue = findViewById(R.id.barChartRevenue)
        pieChart = findViewById(R.id.pieChartToday)
        pieChart.centerText = "Successfull/Failured Orders"
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)

        val pieLegend = pieChart.legend
        pieLegend.form = Legend.LegendForm.CIRCLE
        pieLegend.textSize = 12f
        pieLegend.formSize = 20f
        pieLegend.formToTextSpace = 2f



        barChartRevenue.setBackgroundColor(Color.WHITE)
        barChartRevenue.setDrawGridBackground(false)
        barChartRevenue.setDrawBarShadow(false)
        barChartRevenue.isHighlightFullBarEnabled = false

        // draw bars behind lines
        barChartRevenue.setDrawOrder(
            arrayOf(
                DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.CANDLE, DrawOrder.LINE, DrawOrder.SCATTER
            )
        )

        val l: Legend = barChartRevenue.getLegend()
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        revenueAccordingToMonth()
    }

    private fun getDate(date: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        return sdf.format(Date(date))
    }

    private fun isToday(date: Long): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val today = sdf.format(Date(System.currentTimeMillis()))
        return sdf.format(Date(date)) == today
    }

    private fun revenueAccordingToMonth() {

        val dbRef = FirebaseDatabase.getInstance().getReference("Orders")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val revenue = HashMap<String, MutableList<Double>>() // HashMap<Year, List<Revenue>>
                val successFullOrder = HashMap<String, MutableList<Double>>()
                val failedOrder = HashMap<String, MutableList<Double>>()
                // Initialize revenue for each year
                for (order in snapshot.children) {
                    val orderModel = order.getValue(OrderModel::class.java)
                    if (orderModel != null &&  (orderModel.status == resources.getStringArray(R.array.order_status)[4] || orderModel.status == resources.getStringArray(R.array.order_status)[5])) {
                        val date = orderModel.orderDate
                        val total = orderModel.price
                        val month = getDate(date).split("/")[1]
                        val year = getDate(date).split("/")[2]

                        if(orderModel.status == resources.getStringArray(R.array.order_status)[4]) {
                            if (!successFullOrder.containsKey(year)) {
                                successFullOrder[year] = MutableList(12) { 0.0 }
                            }
                            val monthlyRevenue = successFullOrder[year]!!
                            val monthIndex = month.toInt() - 1 // Month index starts from 0
                            if(monthIndex in 0..11)
                                monthlyRevenue[monthIndex] = monthlyRevenue[monthIndex] + 1
                        } else if(orderModel.status == resources.getStringArray(R.array.order_status)[5]) {
                            if (!failedOrder.containsKey(year)) {
                                failedOrder[year] = MutableList(12) { 0.0 }
                            }
                            val monthlyRevenue = failedOrder[year]!!
                            val monthIndex = month.toInt() - 1 // Month index starts from 0
                            if(monthIndex in 0..11)
                                monthlyRevenue[monthIndex] = monthlyRevenue[monthIndex] + 1
                        }


                        // If the year doesn't exist in the revenue HashMap, add it with an empty list
                        if (!revenue.containsKey(year)) {
                            revenue[year] = MutableList(12) { 0.0 }
                        }

                        // Update revenue for the month in the corresponding year
                        val monthlyRevenue = revenue[year]!!
                        val monthIndex = month.toInt() - 1 // Month index starts from 0
                        if(monthIndex in 0..11)
                            monthlyRevenue[monthIndex] = monthlyRevenue[monthIndex] + total
                    }

                    if (orderModel != null) {
                        if(isToday(orderModel.orderDate)) {
                            todayOrder++
                            if(orderModel.status == resources.getStringArray(R.array.order_status)[4]) {
                                todayRevenue += orderModel.price
                                todaySuccessOrder++
                            } else if(orderModel.status == resources.getStringArray(R.array.order_status)[5]) {
                                todayFailedOrder++
                            }
                        }
                    }
                }
                showRevenueAccordingToMonth(revenue, successFullOrder, failedOrder)
                setUpPieChartTodaySuccessFailedOrder()
                // After calculating revenue for each month in each year, you can display it or do further processing

                // Here you can update your UI to display revenue according to year and month
                // For example, populate a Spinner with years and when a year is selected, display revenue for each month
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Revenue", "Failed to read value.", error.toException())
            }
        })
    }

    private fun showRevenueAccordingToMonth(
        revenue: HashMap<String, MutableList<Double>>,
        successFullOrder: HashMap<String, MutableList<Double>>,
        failedOrder: HashMap<String, MutableList<Double>>
        ) {
        val listDataEntry = ArrayList<BarEntry>()
        for ((year, monthlyRevenue) in revenue) {
            for (i in 0 until monthlyRevenue.size) {
                listDataEntry.add(BarEntry(i.toFloat(), monthlyRevenue[i].toFloat()))
            }
        }
        val spinner = findViewById<Spinner>(R.id.spinnerYear)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, revenue.keys.toTypedArray())
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val year = spinner.selectedItem.toString()
                val monthlyRevenue = revenue[year]!!
                val listDataEntry  = ArrayList<BarEntry>()
                val orderSuccessEntry = ArrayList<Entry>()
                val orderFailedEntry = ArrayList<Entry>()
                for (i in 0 until monthlyRevenue.size) {
                    listDataEntry.add(BarEntry(i.toFloat(), monthlyRevenue[i].toFloat()))
                    orderSuccessEntry.add(Entry(i.toFloat(), successFullOrder[year]?.get(i)?.toFloat() ?: 0.0f))
                    orderFailedEntry.add(Entry(i.toFloat(), failedOrder[year]?.get(i)?.toFloat() ?: 0.0f))
                }
                val barDataSet = BarDataSet(listDataEntry, "Revenue")
                val barData = BarData(barDataSet)
                val lineSuccess =  LineDataSet(orderSuccessEntry, "Success Orders")
                val lineFailed =  LineDataSet(orderFailedEntry, "Failed Orders")


                barDataSet.axisDependency = YAxis.AxisDependency.LEFT

                lineSuccess.color = Color.GREEN
                lineSuccess.setCircleColor(Color.GREEN)
                lineSuccess.setDrawValues(true)
                lineSuccess.setLineWidth(2.5f);
                lineSuccess.setCircleColor(Color.GREEN);
                lineSuccess.setCircleRadius(5f);
                lineSuccess.setFillColor(Color.GREEN);
                lineSuccess.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                lineSuccess.setDrawValues(true);
                lineSuccess.setValueTextSize(10f);
                lineSuccess.setValueTextColor(Color.rgb(0,100,0));
                lineSuccess.axisDependency = YAxis.AxisDependency.RIGHT


                lineFailed.color = Color.RED
                lineFailed.setCircleColor(Color.RED)
                lineFailed.setDrawValues(true)
                lineFailed.setLineWidth(2.5f);
                lineFailed.setCircleColor(Color.rgb(0,100,0));
                lineFailed.setCircleRadius(5f);
                lineFailed.setFillColor(Color.rgb(0,100,0));
                lineFailed.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                lineFailed.setDrawValues(true);
                lineFailed.setValueTextSize(10f);
                lineFailed.setValueTextColor(Color.rgb(0,100,0));
                lineFailed.axisDependency = YAxis.AxisDependency.RIGHT

                val lineData = LineData(lineSuccess, lineFailed)

                val combineData = CombinedData()
                combineData.setData(barData)
                combineData.setData(lineData)


                // Customize x-axis labels
                val xAxis = barChartRevenue.xAxis
                val monthName = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                xAxis.valueFormatter = IndexAxisValueFormatter(monthName)
                xAxis.labelCount = 12 // Show all 12 months
                xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTH_SIDED
                xAxis.axisMinimum = 0f;
                xAxis.granularity = 1f;
                xAxis.setAxisMaximum(combineData.getXMax() + 0.25f);

                // Customize y-axis labels
                val axisLeft = barChartRevenue.axisLeft
                axisLeft.valueFormatter = MillionValueFormatter()
                axisLeft.setDrawGridLines(false);
                axisLeft.granularity = 1f;
                axisLeft.axisMinimum = 0f;


                val axisRight = barChartRevenue.axisRight
                axisRight.setDrawGridLines(false);
                axisRight.granularity = 1f;
                axisRight.axisMinimum = 0f;
                axisRight.axisMaximum = max(orderSuccessEntry.maxByOrNull { it.y }?.y ?: 0f, orderFailedEntry.maxByOrNull { it.y }?.y ?: 0f) + 3f;
                barChartRevenue.axisRight.isEnabled = true
                barChartRevenue.description.isEnabled = false
                barChartRevenue.animateY(1000)


                barChartRevenue.data = combineData
                barChartRevenue.invalidate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


    }

    private fun setUpPieChartTodaySuccessFailedOrder()
    {
        val todayRevenueTv = findViewById<TextView>(R.id.today_revenue)
        todayRevenueTv.text = "Today Revenue: $todayRevenue"

        val todayOrderTv = findViewById<TextView>(R.id.today_order)
        todayOrderTv.text = "Today Order: $todayOrder"


        val entries = ArrayList<PieEntry>()

        entries.add(PieEntry(todaySuccessOrder.toFloat(), "Success"))
        entries.add(PieEntry(todayFailedOrder.toFloat(), "Failed"))

        val dataSet = PieDataSet(entries, "")

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}"
            }
        }
        dataSet.setColors(Color.GREEN, Color.RED)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)
        pieChart.data = data

        pieChart.invalidate()
    }
}


class MillionValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${value / 1000000}M"
    }
}
