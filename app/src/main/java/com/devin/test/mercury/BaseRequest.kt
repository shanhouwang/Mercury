package com.devin.test.mercury

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONField
import com.devin.mercury.annotation.Header
import com.devin.mercury.model.MercuryBuildHeaders
import com.devin.model.mercury.MercuryRequest

open class BaseRequest : MercuryRequest(), MercuryBuildHeaders {

    override fun buildHeaders(): MutableMap<String, String> {
        return mutableMapOf<String, String>().apply {
            put("x-request-token", JSON.toJSONString(this@BaseRequest))
        }
    }

    @Header
    @JSONField(serialize = false)
    var headers = mutableMapOf<String, String>().apply {
        put("x-request-session", JSON.toJSONString(this@BaseRequest))
    }
}