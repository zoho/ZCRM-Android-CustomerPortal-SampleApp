package com.example.zcrmcptestapp

import com.zoho.crm.sdk.android.exception.ZCRMException

interface ResponseHandler<V> {
    /**
     * Invoked when an operation has completed.
     *
     * @param response The Response of the API hit
     */
    fun completed(response: V)

    /**
     * Invoked when an operation fails.
     *
     * @param exception The exception to indicate why the API hit failed
     */
    fun failed(exception: ZCRMException)
}
