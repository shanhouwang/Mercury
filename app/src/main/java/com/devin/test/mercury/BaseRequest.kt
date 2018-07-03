package com.devin.test.mercury

import com.devin.mercury.annotation.Get
import com.devin.model.mercury.MercuryRequest

@Get(url = "http://www.baidu.com")
class BaseRequest(id: String, name: String) : MercuryRequest()