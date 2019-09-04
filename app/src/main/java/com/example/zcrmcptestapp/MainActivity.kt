package com.example.zcrmcptestapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.zcrmcpapp.R
import com.zoho.crm.sdk.android.authorization.ZCRMSDKClient
import com.zoho.crm.sdk.android.common.CommonUtil
import com.zoho.crm.sdk.android.configuration.ZCRMSDKConfigs
import com.zoho.crm.sdk.android.exception.ZCRMException
import com.zoho.crm.sdk.android.exception.ZCRMLogger
import java.util.logging.Level

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sdk = ZCRMSDKClient.getInstance(applicationContext)
        val scopes = "ZohoCRM.modules.ALL,ZohoCRM.settings.ALL,ZohoCRM.users.ALL,ZohoCRM.org.READ,Aaaserver.profile.Read,profile.userphoto.ALL,TeamDrive.team.All,TeamDrive.workspace.All,TeamDrive.files.All,TeamDrive.license.All,ZohoFiles.files.ALL,SalesIQ.chatdetails.READ,SalesIQ.portals.READ"

        val configs = ZCRMSDKConfigs()
        configs.apiBaseURL = "https://crm.localzoho.com"
        configs.appType = CommonUtil.AppType.ZCRMCP
        configs.oauthScopes = scopes
        configs.setClientDetails(
            "1001757695.HB4Q3LDBE8PU101701XJRZAWTWV6CH",
            "ee1db2cb03ec42d92ee898e4b7db51c44350bee111"
        )
        configs.portalID = "1001757695"
        configs.setLoggingPreferences(Level.INFO, true)
        configs.customerPortalName = "testportal55"
        sdk.init(configs, object : ZCRMSDKClient.Companion.ZCRMInitCallback {
            override fun onFailed(ex: ZCRMException) {
                ZCRMLogger.logError("> Login failed - " + ex)
            }

            override fun onSuccess() {
                ZCRMLogger.logInfo("> Login success")
                configs.setLoggingPreferences(Level.INFO, true)
                startActivity(Intent(applicationContext, APIActivity::class.java))
            }
        })

    }
}
