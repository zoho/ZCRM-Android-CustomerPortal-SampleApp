package com.example.zcrmcptestapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.zcrmcpapp.R
import com.zoho.crm.sdk.android.api.handler.DataCallback
import com.zoho.crm.sdk.android.api.response.BulkAPIResponse
import com.zoho.crm.sdk.android.crud.ZCRMQuery
import com.zoho.crm.sdk.android.crud.ZCRMRecord
import com.zoho.crm.sdk.android.exception.ZCRMException
import com.zoho.crm.sdk.android.setup.sdkUtil.ZCRMSDKUtil
import android.widget.Toast

class ListActivity : Activity(), RecyclerViewAdapter.OnItemClickListener {

    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var records: ArrayList<ZCRMRecord>
    private lateinit var searchedRecords: ArrayList<ZCRMRecord>
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayout: LinearLayout
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var module: String
    private var list1Page: Int = 1
    private var list2Page: Int = 1
    private var width: Int = 0
    private var height: Int = 0
    private var getRecordsResponse: BulkAPIResponse? = null
    private var searchRecordsResponse: BulkAPIResponse? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    private var isSearchRecords: Boolean = false
    private var searchKeyWord: String = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y

        val intent = intent
        module = intent.getStringExtra("Module")

        initializeViews()
        setToolBar()
        linearLayout.addView(toolbar)
        getRecords()
        setContentView(linearLayout)

    }

    private fun initializeViews() {
        toolbar = Toolbar(this)
        linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL

        layoutManager = LinearLayoutManager(this@ListActivity, LinearLayout.VERTICAL, false)
        recyclerView = RecyclerView(this)
        recyclerView.itemAnimator = DefaultItemAnimator()
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        records = ArrayList()
        searchedRecords = ArrayList()
    }

    private fun setToolBar() {
        val toolbarWidth = 0.85 * width

        if (linearLayout.childCount != 0) {
            val t1 = linearLayout.getChildAt(0) as Toolbar
            t1.removeViewAt(0)
            t1.removeViewAt(0)
            t1.removeViewAt(0)
            progressBar.visibility = View.VISIBLE
            linearLayout.removeView(recyclerView)
            this.isSearchRecords = false
            setListView(records)
            progressBar.visibility = View.GONE
        }

        val toolBarParams = LinearLayout.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, 150)
        toolbar.layoutParams = toolBarParams
        toolbar.setBackgroundColor(Color.GRAY)
        toolbar.popupTheme = R.style.AppTheme
        toolbar.visibility = View.VISIBLE
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        val txtView = LinearLayout(this)
        txtView.orientation = LinearLayout.VERTICAL
        txtView.layoutParams = LinearLayout.LayoutParams((toolbarWidth / 2).toInt(), (1.5 * height / 20).toInt())
        txtView.gravity = Gravity.START

        val txt = TextView(this)
        txt.text = module
        txt.textSize = 20F
        txt.setTextColor(Color.WHITE)
        txt.setPadding(0, 30, 0, 0)
        txtView.addView(txt)

        toolbar.addView(txtView)
        setSearchButton()
    }

    private fun setSearchButton() {
        val toolbarWidth = 0.85 * width

        val searchButtonView = LinearLayout(this)
        searchButtonView.orientation = LinearLayout.HORIZONTAL
        searchButtonView.layoutParams =
            LinearLayout.LayoutParams((toolbarWidth / 2).toInt(), (1.5 * height / 20).toInt())
        searchButtonView.gravity = Gravity.CENTER_VERTICAL

        val space = Space(this)
        space.layoutParams = LinearLayout.LayoutParams((toolbarWidth / 3).toInt(), (1.5 * height / 20).toInt())

        val searchButton = ImageButton(this)
        searchButton.layoutParams = LinearLayout.LayoutParams((toolbarWidth / 10).toInt(), (toolbarWidth / 10).toInt())
        searchButton.setBackgroundResource(R.drawable.ic_search_black_24dp)
        searchButton.setPadding(0, 50, 0, 0)
        searchButton.setOnClickListener {
            setSearchBar()
        }

        searchButtonView.addView(space)
        searchButtonView.addView(searchButton)

        toolbar.addView(searchButtonView)
    }

    private fun setSearchBar() {
        val toolbarWidth = 0.85 * width

        toolbar.removeViewAt(1)
        toolbar.removeViewAt(1)

        val searchView = LinearLayout(this)
        searchView.orientation = LinearLayout.VERTICAL
        searchView.layoutParams =
            LinearLayout.LayoutParams((toolbarWidth * 0.85).toInt(), (1.5 * height / 20).toInt())
        searchView.gravity = Gravity.START

        val search = EditText(this)
        search.hint = "Enter search keyword..."
        search.setBackgroundResource(android.R.color.transparent)
        search.textSize = 20F
        search.setHintTextColor(Color.LTGRAY)
        search.setTextColor(Color.WHITE)
        search.setPadding(0, 30, 0, 0)
        search.setSingleLine()
        search.setOnClickListener { search.isCursorVisible = true }
        search.requestFocus()
        search.imeOptions = EditorInfo.IME_ACTION_DONE
        search.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search.isCursorVisible = false
                hideKeyboard()
                if (search.text.toString() != this.searchKeyWord) {
                    this.searchKeyWord = search.text.toString()
                    searchedRecords = ArrayList()
                    setSearchedRecords()
                }
                return@OnEditorActionListener true
            }
            false
        })

        searchView.addView(search)

        val oKButtonView = LinearLayout(this)
        oKButtonView.orientation = LinearLayout.VERTICAL
        oKButtonView.layoutParams =
            LinearLayout.LayoutParams((toolbarWidth * 0.2).toInt(), (1.5 * height / 20).toInt())
        oKButtonView.gravity = Gravity.CENTER_VERTICAL

        val oKButton = ImageButton(this)
        oKButton.layoutParams = LinearLayout.LayoutParams((toolbarWidth / 10).toInt(), (toolbarWidth / 10).toInt())
        oKButton.setBackgroundResource(R.drawable.ic_done_black_24dp)
        oKButton.setPadding(0, 50, 0, 0)
        oKButton.setOnClickListener {
            search.isCursorVisible = false
            hideKeyboard()
            if (search.text.toString() != this.searchKeyWord) {
                this.searchKeyWord = search.text.toString()
                searchedRecords = ArrayList()
                setSearchedRecords()
            }
        }
        oKButtonView.addView(oKButton)

        showKeyboard()
        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            setToolBar()
        }

        toolbar.addView(searchView)
        toolbar.addView(oKButtonView)
    }

    private fun setSearchedRecords() {
        setProgressBar()
        this@ListActivity.isSearchRecords = true

        this.getRecordsFromServer(true, object : ResponseHandler<List<ZCRMRecord>> {
            override fun completed(recs: List<ZCRMRecord>) {

                runOnUiThread {
                    recs.forEach { record -> searchedRecords.add(record) }

                    if (this@ListActivity.list2Page == 1) {
                        linearLayout.removeView(recyclerView)
                        setListView(searchedRecords)
                    }

                    progressBar.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                    recyclerView.layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                }
            }

            override fun failed(exception: ZCRMException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ListActivity, exception.getErrorMsg(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun getRecords() {
        setProgressBar()
        this@ListActivity.isSearchRecords = false

        this.getRecordsFromServer(false, object : ResponseHandler<List<ZCRMRecord>> {
            override fun completed(recs: List<ZCRMRecord>) {

                runOnUiThread {
                    recs.forEach { record -> records.add(record) }

                    if (this@ListActivity.list1Page == 1) {
                        setListView(records)
                    }
                    progressBar.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                    recyclerView.layoutParams =
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                }
            }

            override fun failed(exception: ZCRMException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ListActivity, exception.getErrorMsg(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

    }

    private fun setListView(records: ArrayList<ZCRMRecord>) {

        progressBar.visibility = View.GONE
        val recordNames = ArrayList<String?>()
        records.forEach { rec -> recordNames.add(rec.getFieldValue(getDisplayFieldName(module)) as String) }
        adapter = RecyclerViewAdapter(recordNames, this@ListActivity)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        progressBarLayout.layoutParams = LinearLayout.LayoutParams(0, 0)
        linearLayout.addView(recyclerView)
        setLoadDataListener()
        resizeRecyclerView()
    }

    private fun setLoadDataListener() {
        adapter.setLoadDataListener(object : RecyclerViewAdapter.LoadDataListener {
            override fun loadData(position: Int) {

                if (!isSearchRecords) {
                    getRecordsResponse?.info?.apply {
                        when {
                            this.moreRecords -> {
                                this@ListActivity.list1Page++
                                getRecords()
                            }
                        }
                    }
                } else {
                    searchRecordsResponse?.info?.apply {
                        when {
                            this.moreRecords -> {
                                this@ListActivity.list2Page++
                                setSearchedRecords()
                            }
                        }
                    }
                }

            }
        })
    }

    private fun resizeRecyclerView() {
        adapter.setOnBottomReachedListener(object : RecyclerViewAdapter.OnBottomReachedListener {
            override fun onBottomReached(position: Int) {
                var resizeRecyclerView = false
                when {
                    isSearchRecords -> searchRecordsResponse?.info?.apply {
                        resizeRecyclerView = this.moreRecords
                    }
                    else -> getRecordsResponse?.info?.apply {
                        resizeRecyclerView = this.moreRecords
                    }
                }

                when {
                    resizeRecyclerView -> recyclerView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (0.82 * height).toInt()
                    )
                }

            }
        })
    }

    private fun getRecordsFromServer(isSearch: Boolean, responseHandler: ResponseHandler<List<ZCRMRecord>>) {
        when {
            isSearch -> {
                ZCRMSDKUtil.getModuleDelegate(module)
                    .searchByText(this.searchKeyWord, this.list2Page, AppData.recordsPerPage,
                        object : DataCallback<BulkAPIResponse, List<ZCRMRecord>> {
                            override fun completed(response: BulkAPIResponse, recs: List<ZCRMRecord>) {
                                this@ListActivity.searchRecordsResponse = response
                                responseHandler.completed(recs)
                            }

                            override fun failed(exception: ZCRMException) {
                                responseHandler.failed(exception)
                            }
                        })
            }
            else -> {
                val params = ZCRMQuery.Companion.GetRecordParams()
                params.page = this.list1Page
                params.perPage = AppData.recordsPerPage

                ZCRMSDKUtil.getModuleDelegate(module)
                    .getRecords(params, object : DataCallback<BulkAPIResponse, List<ZCRMRecord>> {
                        override fun completed(response: BulkAPIResponse, recs: List<ZCRMRecord>) {
                            this@ListActivity.getRecordsResponse = response
                            responseHandler.completed(recs)
                        }

                        override fun failed(exception: ZCRMException) {
                            responseHandler.failed(exception)
                        }
                    })
            }
        }
    }

    private fun setProgressBar() {
        progressBarLayout = LinearLayout(this)
        progressBarLayout.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        progressBarLayout.gravity = Gravity.CENTER
        progressBar = ProgressBar(this)
        progressBar.visibility = View.VISIBLE
        progressBarLayout.addView(progressBar)
        linearLayout.addView(progressBarLayout)
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

    private fun hideKeyboard() {
        val inputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    override fun onClick(position: Int) {
        Log.d("A", "<<< Item clicked...$position")
        val detailViewPage = Intent(this, RecordDetails::class.java)
        when {
            isSearchRecords -> AppData.record = this.searchedRecords[position]
            else -> AppData.record = this.records[position]
        }
        AppData.module = this.module
        startActivity(detailViewPage)

    }
}
