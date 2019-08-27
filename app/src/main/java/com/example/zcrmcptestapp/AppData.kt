package com.example.zcrmcptestapp

import com.zoho.crm.sdk.android.crud.ZCRMRecord

class AppData {
    companion object {
        var width: Int = 0
        var height: Int = 0
        var recordsPerPage: Int = 20
        var record: ZCRMRecord? = null
        var module: String = String()
    }
}