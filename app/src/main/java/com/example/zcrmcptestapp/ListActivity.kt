package com.example.zcrmcptestapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toolbar
import com.example.zcrmcpapp.R
import com.zoho.crm.sdk.android.api.handler.DataCallback
import com.zoho.crm.sdk.android.api.response.BulkAPIResponse
import com.zoho.crm.sdk.android.crud.ZCRMQuery
import com.zoho.crm.sdk.android.crud.ZCRMRecord
import com.zoho.crm.sdk.android.exception.ZCRMException
import com.zoho.crm.sdk.android.setup.sdkUtil.ZCRMSDKUtil

class ListActivity : Activity(), RecyclerViewAdapter.OnItemClickListener {

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var recordNames: ArrayList<String?>
    private lateinit var records: ArrayList<ZCRMRecord>
    private lateinit var module: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        module = intent.getStringExtra("Module")

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        val halfLayout = height / 2

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val recyclerView = RecyclerView(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        recordNames = ArrayList()

        // Set Tool bar
        val toolbar = Toolbar(this)
        val toolBarParams = LinearLayout.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, 150)
        toolbar.layoutParams = toolBarParams
        toolbar.setBackgroundColor(Color.GRAY)
        toolbar.popupTheme = R.style.AppTheme
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = module
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        linearLayout.addView(toolbar, 0)

        val params = ZCRMQuery.Companion.GetRecordParams()
        ZCRMSDKUtil.getModuleDelegate(module).getRecords(params, object :
            DataCallback<BulkAPIResponse, List<ZCRMRecord>> {
            override fun completed(response: BulkAPIResponse, records: List<ZCRMRecord>) {

                this@ListActivity.records = records as ArrayList<ZCRMRecord>
                for (record in records) {
                    recordNames.add(record.getFieldValue(getDisplayFieldName(module)) as String)
                }

                runOnUiThread {
                    val layoutManager = LinearLayoutManager(this@ListActivity, LinearLayout.VERTICAL, false)
                    val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)

                    adapter = RecyclerViewAdapter(recordNames, this@ListActivity)
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = layoutManager
                    recyclerView.setPadding(20, 0, 0, 0)
                    recyclerView.adapter = adapter
                    recyclerView.addItemDecoration(dividerItemDecoration)
                    linearLayout.addView(recyclerView)
                    setContentView(linearLayout)
                }
            }

            override fun failed(exception: ZCRMException) {
                throw exception
            }
        })
    }

    private fun getDisplayFieldName(module: String): String {
        return when (module) {
            "Leads", "Contacts" -> "Full_Name"
            "Accounts" -> "Account_Name"
            "Deals", "Potentials" -> "Deal_Name"
            "Activities", "Tasks", "Calls", "Cases", "Quotes", "Sales_Orders", "Purchase_Orders", "Invoices" -> "Subject"
            "Events" -> "Event_Title"
            "Products" -> "Product_Name"
            "Campaigns" -> "Campaign_Name"
            "Solutions" -> "Solution_Title"
            "Vendors" -> "Vendor_Name"
            "Price_Books" -> "Price_Book_Name"
            else -> "Name"
        }
    }

    override fun onClick(position: Int) {
        val detailViewPage = Intent(this, RecordDetails::class.java)
        RecordData.record = this.records[position]
        RecordData.module = this.module
        startActivity(detailViewPage)
    }
}
