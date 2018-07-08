package com.devin.test.mercury

import com.devin.model.mercury.MercuryRequest

open class BaseRequest : MercuryRequest() {


    var headers = HashMap<String, String>().apply {
        put("x-request-token", "devin")
    }
}