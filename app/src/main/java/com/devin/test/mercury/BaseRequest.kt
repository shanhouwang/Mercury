package com.devin.test.mercury

import com.devin.mercury.annotation.Header
import com.devin.model.mercury.MercuryRequest

open class BaseRequest : MercuryRequest() {

    @Header
    var headers = mutableMapOf<String, String>().apply {
        put("x-request-token", "devin")
    }
}