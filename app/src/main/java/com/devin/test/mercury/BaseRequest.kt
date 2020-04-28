package com.devin.test.mercury

import com.devin.mercury.annotation.Header
import com.devin.mercury.model.MercuryBuildHeaders
import com.devin.model.mercury.MercuryRequest
import com.google.gson.Gson
import com.google.gson.annotations.Expose

open class BaseRequest : MercuryRequest<String>(), MercuryBuildHeaders {

    override fun buildHeaders(): MutableMap<String, String> {
        return mutableMapOf<String, String>().apply {
            put("x-request-token", Gson().toJson(this))
        }
    }

    @Header
    @Expose(serialize = false)
    var headers = mutableMapOf<String, String>().apply {
        put("x-request-session", Gson().toJson(this@BaseRequest))
    }
}