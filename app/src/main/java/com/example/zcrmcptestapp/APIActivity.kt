package com.example.zcrmcptestapp

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zoho.crm.sdk.android.authorization.ZCRMSDKClient
import com.example.zcrmcpapp.R
import com.zoho.crm.sdk.android.api.handler.DataCallback
import com.zoho.crm.sdk.android.api.response.BulkAPIResponse
import com.zoho.crm.sdk.android.api.response.FileAPIResponse
import com.zoho.crm.sdk.android.crud.ZCRMQuery
import com.zoho.crm.sdk.android.crud.ZCRMRecord
import com.zoho.crm.sdk.android.exception.ZCRMException
import com.zoho.crm.sdk.android.setup.sdkUtil.ZCRMSDKUtil
import java.io.BufferedInputStream
import java.io.InputStream

class APIActivity : Activity() {

    private lateinit var topLayout: LinearLayout
    private var halfLayout: Int = 0
    private var botLayoutHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        halfLayout = height/2
        botLayoutHeight = height - halfLayout

        val screenLayout = LinearLayout(this)
        screenLayout.layoutParams =  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        screenLayout.orientation = LinearLayout.VERTICAL

        topLayout = LinearLayout(this)
        topLayout.orientation = LinearLayout.VERTICAL
        topLayout.layoutParams =  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, halfLayout)

        // Set Tool bar
        val toolbar = Toolbar(this)
        val toolBarParams = LinearLayout.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, 150)
        toolbar.layoutParams = toolBarParams
        toolbar.setBackgroundColor(Color.GRAY)
        toolbar.popupTheme = R.style.AppTheme
        toolbar.visibility = View.VISIBLE
        toolbar.title = "ZCRMCPApp"
        toolbar.setTitleTextColor(Color.WHITE)

        //Logout Button View
        val imageLayout = LinearLayout(this)
        imageLayout.orientation = LinearLayout.HORIZONTAL
        imageLayout.layoutParams = LinearLayout.LayoutParams(3*width/5, (1.5 * halfLayout/10).toInt() )
        imageLayout.setPadding(width/3, 20, 0, 0)
        imageLayout.gravity = Gravity.END

        val logoutButton = ImageView(this)
        logoutButton.layoutParams = LinearLayout.LayoutParams(halfLayout/10, halfLayout/10)
        logoutButton.setBackgroundResource(R.drawable.logout_24dp)

        logoutButton.setOnClickListener {
            val sdk = ZCRMSDKClient.getInstance(applicationContext)
            sdk.logout(object : ZCRMSDKClient.Companion.ZCRMLogoutCallback {
                override fun onFailed() {
                    println(">> logout failed")
                }

                override fun onSuccess() {
                    println(">> logout success")
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            })
        }

        imageLayout.addView(logoutButton)
        toolbar.addView(imageLayout)
        screenLayout.addView(toolbar, 0)

        // Setting User details
        setUserDetails()

        // Bottom Layout
        val bottomLayout = LinearLayout(this)
        bottomLayout.orientation = LinearLayout.VERTICAL
        bottomLayout.layoutParams =  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height/2)

        // Quotes Horizontal Layout
        val quotesLayout = LinearLayout(this)
        quotesLayout.orientation =  LinearLayout.VERTICAL
        quotesLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, botLayoutHeight/4)
        quotesLayout.gravity = Gravity.CENTER

        val myQuotesButton = Button(this)
        myQuotesButton.layoutParams = LinearLayout.LayoutParams(width/2, botLayoutHeight/6)
        myQuotesButton.text = "Quotes"

        quotesLayout.addView(myQuotesButton)

        // Invoices Horizontal Layout
        val invoicesLayout = LinearLayout(this)
        invoicesLayout.orientation =  LinearLayout.VERTICAL
        invoicesLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, botLayoutHeight/4)
        invoicesLayout.gravity = Gravity.CENTER

        val myInvoicesButton = Button(this)
        myInvoicesButton.layoutParams = LinearLayout.LayoutParams(width/2, botLayoutHeight/6)
        myInvoicesButton.text = "Invoices"

        invoicesLayout.addView(myInvoicesButton)

        // Cases Horizontal Layout
        val casesLayout = LinearLayout(this)
        casesLayout.orientation =  LinearLayout.VERTICAL
        casesLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, botLayoutHeight/4)
        casesLayout.gravity = Gravity.CENTER

        val myCasesButton = Button(this)
        myCasesButton.layoutParams = LinearLayout.LayoutParams(width/2, botLayoutHeight/6)
        myCasesButton.text = "Cases"

        casesLayout.addView(myCasesButton)

        bottomLayout.addView(quotesLayout)
        bottomLayout.addView(invoicesLayout)
        bottomLayout.addView(casesLayout)

        screenLayout.addView(topLayout)

        val viewDivider = View(this)
        viewDivider.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2)
        viewDivider.setBackgroundColor(Color.BLACK)

        screenLayout.addView(viewDivider)
        screenLayout.addView(bottomLayout)
        setContentView(screenLayout)

        myQuotesButton.setOnClickListener {
            getQuotes()
        }

        myInvoicesButton.setOnClickListener {
            getInvoices()
        }

        myCasesButton.setOnClickListener {
            getCases()
        }
    }

    private fun setUserDetails() {
        //Profile Image View
        val profileImageLayout = LinearLayout(this)
        profileImageLayout.orientation = LinearLayout.HORIZONTAL
        profileImageLayout.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 7 * halfLayout/10)
        profileImageLayout.setPadding(0, 100, 0, 0)
        profileImageLayout.gravity = Gravity.CENTER

        val profileImage = ImageView(this)
        profileImage.layoutParams = LinearLayout.LayoutParams(7 * halfLayout/10, 7 * halfLayout/10)
        profileImage.setBackgroundResource(R.drawable.noimage)

        profileImageLayout.addView(profileImage)

        topLayout.addView(profileImageLayout)

        val spaceView = LinearLayout(this)
        spaceView.orientation = LinearLayout.HORIZONTAL
        spaceView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (0.5 * halfLayout/10).toInt())
        spaceView.gravity = Gravity.CENTER
        topLayout.addView(spaceView)

        // User Name View
        val userNameView = LinearLayout(this)
        userNameView.orientation = LinearLayout.HORIZONTAL
        userNameView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (halfLayout/10))
        userNameView.gravity = Gravity.CENTER

        val userName = TextView(this)
        userName.textSize = 18F
        userName.setPadding(0, 5, 0, 5)

        userNameView.addView(userName)

        // User Email View
        val userEmailView = LinearLayout(this)
        userEmailView.orientation = LinearLayout.HORIZONTAL
        userEmailView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (halfLayout/10))
        userEmailView.gravity = Gravity.CENTER

        val userEmail = TextView(this)
        userEmail.textSize = 18F
        userEmail.setPadding(0, 0, 0, 5)

        userEmailView.addView(userEmail)

        topLayout.addView(userNameView)
        topLayout.addView(userEmailView)

        val params = ZCRMQuery.Companion.GetRecordParams()
        ZCRMSDKUtil.getModuleDelegate("Contacts").getRecords(params, object :DataCallback<BulkAPIResponse, List<ZCRMRecord>> {
            override fun completed(response: BulkAPIResponse, records: List<ZCRMRecord>) {

                runOnUiThread {
                    userName.text = "Welcome, ${records[0].getFieldValue("Full_Name") as String}."
                    userEmail.text = records[0].getFieldValue("Email") as String
                }

                records[0].downloadPhoto(object :DataCallback<FileAPIResponse, InputStream?> {
                    override fun completed(response: FileAPIResponse, inputStream: InputStream?) {

                        inputStream?.apply {
                            val bufferedInputStream = BufferedInputStream(this)
                            val bmp = BitmapFactory.decodeStream(bufferedInputStream)
                            runOnUiThread {
                                profileImage.setImageBitmap(bmp)
                            }
                        }
                    }

                    override fun failed(exception: ZCRMException) {
                        throw exception
                    }
                })
            }

            override fun failed(exception: ZCRMException) {
                throw exception
            }
        })

    }

    private fun getQuotes() {
        val intent = Intent(applicationContext, ListActivity::class.java)
        intent.putExtra("Module", "Quotes")
        startActivity(intent)
    }

    private fun getInvoices() {
        val intent = Intent(applicationContext, ListActivity::class.java)
        intent.putExtra("Module", "Invoices")
        startActivity(intent)
    }

    private fun getCases() {
        val intent = Intent(applicationContext, ListActivity::class.java)
        intent.putExtra("Module", "Cases")
        startActivity(intent)
    }
}
