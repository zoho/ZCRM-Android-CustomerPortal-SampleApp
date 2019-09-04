package com.example.zcrmcptestapp

import android.app.Activity
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.zcrmcpapp.R
import com.zoho.crm.sdk.android.api.handler.DataCallback
import com.zoho.crm.sdk.android.api.response.BulkAPIResponse
import com.zoho.crm.sdk.android.crud.ZCRMField
import com.zoho.crm.sdk.android.crud.ZCRMLayout
import com.zoho.crm.sdk.android.crud.ZCRMRecord
import com.zoho.crm.sdk.android.crud.ZCRMRecordDelegate
import com.zoho.crm.sdk.android.exception.ZCRMException
import com.zoho.crm.sdk.android.setup.sdkUtil.ZCRMSDKUtil

class RecordDetails : Activity() {

    private lateinit var viewLayout: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var llParams: LinearLayout.LayoutParams
    private lateinit var viewParams: LinearLayout.LayoutParams
    private lateinit var txtParams: LinearLayout.LayoutParams
    private var width: Int = 0
    private var height: Int = 0
    private lateinit var field: ZCRMField
    private lateinit var fields: ArrayList<ZCRMField>
    private lateinit var record: ZCRMRecord
    private lateinit var gridLayout: GridLayout
    private var aggregateNames = arrayListOf(
        Pair("Sub_Total", "Sub Total"),
        Pair("Discount", "Discount"),
        Pair("Tax", "Tax"),
        Pair("Adjustment", "Adjustment"),
        Pair("Grand_Total", "Grand Total")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y

        scrollView = ScrollView(this)
        scrollView.layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        viewLayout = LinearLayout(this)
        viewLayout.orientation = LinearLayout.VERTICAL

        llParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        viewParams = LinearLayout.LayoutParams(width / 4, 50)
        txtParams = LinearLayout.LayoutParams((3 * width) / 4, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Set Tool bar
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL

        val toolbar = Toolbar(this)
        val toolBarParams = LinearLayout.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, 150)
        toolbar.layoutParams = toolBarParams
        toolbar.setBackgroundColor(Color.GRAY)
        toolbar.popupTheme = R.style.AppTheme
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = "My ${AppData.module}"
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }
        mainLayout.addView(toolbar, 0)
        AppData.record?.apply { this@RecordDetails.record = this }

        setDetails()

        viewLayout.setPadding(20, 0, 0, 0)
        scrollView.addView(viewLayout)
        mainLayout.addView(scrollView)
        setContentView(mainLayout)
    }

    private fun setDetails() {
        ZCRMSDKUtil.getModuleDelegate(record.moduleAPIName)
            .getLayouts(object : DataCallback<BulkAPIResponse, List<ZCRMLayout>> {
                override fun completed(response: BulkAPIResponse, layouts: List<ZCRMLayout>) {

                    val sections = layouts[0].sections
                    sections.forEach { section ->
                        runOnUiThread {
                            when {
                                section.fields.isNotEmpty() -> this@RecordDetails.setHeaderView(section.displayName)
                            }

                            val fields = section.fields
                            this@RecordDetails.fields = fields
                            fields.forEach { field ->
                                when {
                                    field.isPresentInCreateLayout!! -> {
                                        this@RecordDetails.field = field
                                        this@RecordDetails.setField()
                                    }
                                }
                            }
                        }
                    }

                }

                override fun failed(exception: ZCRMException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RecordDetails, exception.getErrorMsg(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    private fun setHeaderView(displayName: String) {
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 15)
        linearLayout.gravity = Gravity.START

        val header = TextView(this)
        header.textSize = 22F
        header.text = displayName
        header.setTextColor(Color.BLACK)
        header.gravity = Gravity.START

        linearLayout.addView(header)
        viewLayout.addView(linearLayout)
    }

    private fun setField() {
        if (field.apiName == "Product_Details") setProductDetails()
        else if (!aggregateNames.contains(Pair(field.apiName, field.displayName))) {

            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 20)

            val fieldTxt = TextView(this)
            fieldTxt.text = field.displayName
            fieldTxt.layoutParams = LinearLayout.LayoutParams(width / 3, height / 15)
            fieldTxt.textSize = 16F
            fieldTxt.setTextColor(Color.GRAY)

            val valueTxt = TextView(this)
            valueTxt.layoutParams = txtParams

            val json = record.getFieldValue(field.apiName) as? ZCRMRecordDelegate
            var value = record.getFieldValue(field.apiName).toString()
            json?.apply { this.label?.apply { value = this } }
            if (record.getFieldValue(field.apiName) == null) {
                value = "-"
            }

            valueTxt.text = value
            valueTxt.textSize = 16F
            valueTxt.setTextColor(Color.BLACK)

            linearLayout.addView(fieldTxt)
            linearLayout.addView(valueTxt)

            viewLayout.addView(linearLayout)

        }

    }

    private fun setProductDetails() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        gridLayout = GridLayout(this)
        gridLayout.alignmentMode = GridLayout.ALIGN_BOUNDS
        gridLayout.columnCount = 8
        gridLayout.rowCount = 1

        var cellValue = arrayListOf<String?>()
        cellValue.add("S.No")
        cellValue.add("Product Details")
        cellValue.add("List Price")
        cellValue.add("Quantity")
        cellValue.add("Amount (Rs.)")
        cellValue.add("Discount (Rs.)")
        cellValue.add("Tax (Rs.)")
        cellValue.add("Total (Rs.)")

        setRow(cellValue)

        record.lineItems?.apply {
            (0 until this.size).forEach { it ->
                cellValue = arrayListOf()
                cellValue.add((it + 1).toString())
                cellValue.add(this[it].product.label)
                cellValue.add(this[it].listPrice.toString())
                cellValue.add(this[it].quantity.toString())
                cellValue.add(this[it].total.toString())
                cellValue.add(this[it].discount.toString())
                cellValue.add(this[it].taxAmount.toString())
                cellValue.add(this[it].netTotal.toString())

                setRow(cellValue)
            }
        }

        val col7 = "Tax (Rs.)"
        val col8 = "Total (Rs.)"

        val aggregateSubLayout = LinearLayout(this)
        aggregateSubLayout.orientation = LinearLayout.HORIZONTAL
        aggregateSubLayout.gravity = Gravity.END

        val aggregateLayout = LinearLayout(this)
        aggregateLayout.orientation = LinearLayout.VERTICAL
        aggregateLayout.layoutParams =
            LinearLayout.LayoutParams((col7.length + col8.length) * width / 25, ViewGroup.LayoutParams.MATCH_PARENT)
        aggregateLayout.gravity = Gravity.CENTER

        aggregateLayout.addView(getAggregateValue(aggregateNames[0]))
        aggregateLayout.addView(getAggregateValue(aggregateNames[1]))
        aggregateLayout.addView(getAggregateValue(aggregateNames[2]))
        aggregateLayout.addView(getAggregateValue(aggregateNames[3]))
        aggregateLayout.addView(getAggregateValue(aggregateNames[4]))

        aggregateSubLayout.addView(aggregateLayout)

        layout.addView(gridLayout)
        layout.addView(aggregateSubLayout)

        val scrollView2 = HorizontalScrollView(this)
        scrollView2.layoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        scrollView2.setPadding(0, 10, 0, 0)

        scrollView2.addView(layout)
        viewLayout.addView(scrollView2)

    }

    private fun getAggregateValue(field: Pair<String, String>): LinearLayout {
        val col7 = "Tax (Rs.)"
        val col8 = "Total (Rs.)"

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 20)

        val fieldTxt = TextView(this)
        fieldTxt.text = field.second
        fieldTxt.layoutParams = LinearLayout.LayoutParams((col7.length) * width / 25, height / 20)
        fieldTxt.textSize = 16F
        fieldTxt.gravity = Gravity.CENTER
        fieldTxt.setTextColor(Color.GRAY)


        val valueTxt = TextView(this)
        valueTxt.layoutParams = LinearLayout.LayoutParams((col8.length) * width / 25, height / 20)
        valueTxt.text = record.getFieldValue(field.first).toString()
        valueTxt.textSize = 16F
        valueTxt.gravity = Gravity.CENTER

        valueTxt.setTextColor(Color.BLACK)

        linearLayout.addView(fieldTxt)
        linearLayout.addView(valueTxt)

        return linearLayout
    }

    private fun setRow(cellValue: ArrayList<String?>) {

        val border = GradientDrawable()
        border.setColor(Color.WHITE)
        border.setStroke(1, Color.BLACK)

        val col1 = "S.No"
        val col2 = "Product Details"
        val col3 = "List Price"
        val col4 = "Quantity"
        val col5 = "Amount (Rs.)"
        val col6 = "Discount (Rs.)"
        val col7 = "Tax (Rs.)"
        val col8 = "Total (Rs.)"

        val sNo = TextView(this)
        sNo.gravity = Gravity.CENTER
        sNo.text = cellValue[0]
        sNo.setTextColor(Color.BLACK)
        val sNoView = LinearLayout(this)
        sNoView.orientation = LinearLayout.VERTICAL
        sNoView.gravity = Gravity.CENTER
        sNoView.layoutParams = LinearLayout.LayoutParams((col1.length) * width / 25, 120)
        sNoView.addView(sNo)
        sNoView.background = border

        val productDetails = TextView(this)
        productDetails.text = cellValue[1]
        productDetails.gravity = Gravity.CENTER
        productDetails.setTextColor(Color.BLACK)
        val productDetailsView = LinearLayout(this)
        productDetailsView.orientation = LinearLayout.VERTICAL
        productDetailsView.gravity = Gravity.CENTER
        productDetailsView.layoutParams = LinearLayout.LayoutParams((col2.length) * width / 25, 120)
        productDetailsView.addView(productDetails)
        productDetailsView.background = border

        val listPrice = TextView(this)
        listPrice.text = cellValue[2]
        listPrice.gravity = Gravity.CENTER
        listPrice.setTextColor(Color.BLACK)
        val listPriceView = LinearLayout(this)
        listPriceView.orientation = LinearLayout.VERTICAL
        listPriceView.gravity = Gravity.CENTER
        listPriceView.layoutParams = LinearLayout.LayoutParams((col3.length) * width / 25, 120)
        listPriceView.addView(listPrice)
        listPriceView.background = border

        val qty = TextView(this)
        qty.text = cellValue[3]
        qty.gravity = Gravity.CENTER
        qty.setTextColor(Color.BLACK)
        val qtyView = LinearLayout(this)
        qtyView.orientation = LinearLayout.VERTICAL
        qtyView.gravity = Gravity.CENTER
        qtyView.layoutParams = LinearLayout.LayoutParams((col4.length) * width / 25, 120)
        qtyView.addView(qty)
        qtyView.background = border

        val amt = TextView(this)
        amt.text = cellValue[4]
        amt.gravity = Gravity.CENTER
        amt.setTextColor(Color.BLACK)
        val amtView = LinearLayout(this)
        amtView.orientation = LinearLayout.VERTICAL
        amtView.gravity = Gravity.CENTER
        amtView.layoutParams = LinearLayout.LayoutParams((col5.length) * width / 25, 120)
        amtView.addView(amt)
        amtView.background = border

        val discount = TextView(this)
        discount.text = cellValue[5]
        discount.gravity = Gravity.CENTER
        discount.setTextColor(Color.BLACK)
        val discountView = LinearLayout(this)
        discountView.orientation = LinearLayout.VERTICAL
        discountView.gravity = Gravity.CENTER
        discountView.layoutParams = LinearLayout.LayoutParams((col6.length) * width / 25, 120)
        discountView.addView(discount)
        discountView.background = border

        val tax = TextView(this)
        tax.text = cellValue[6]
        tax.gravity = Gravity.CENTER
        tax.setTextColor(Color.BLACK)
        val taxView = LinearLayout(this)
        taxView.orientation = LinearLayout.VERTICAL
        taxView.gravity = Gravity.CENTER
        taxView.layoutParams = LinearLayout.LayoutParams((col7.length) * width / 25, 120)
        taxView.addView(tax)
        taxView.background = border

        val total = TextView(this)
        total.text = cellValue[7]
        total.gravity = Gravity.CENTER
        total.setTextColor(Color.BLACK)
        val totalView = LinearLayout(this)
        totalView.orientation = LinearLayout.VERTICAL
        totalView.gravity = Gravity.CENTER
        totalView.layoutParams = LinearLayout.LayoutParams((col8.length) * width / 25, 120)
        totalView.addView(total)
        totalView.background = border

        gridLayout.addView(sNoView)
        gridLayout.addView(productDetailsView)
        gridLayout.addView(listPriceView)
        gridLayout.addView(qtyView)
        gridLayout.addView(amtView)
        gridLayout.addView(discountView)
        gridLayout.addView(taxView)
        gridLayout.addView(totalView)

    }
}
