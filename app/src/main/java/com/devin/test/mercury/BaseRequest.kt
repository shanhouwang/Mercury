package com.devin.test.mercury

import com.devin.mercury.annotation.Cache
import com.devin.mercury.annotation.Get
import com.devin.model.mercury.MercuryRequest

@Get(url = "http://www.baidu.com")
@Cache
class BaseRequest(var id: String, var name: String) : MercuryRequest()