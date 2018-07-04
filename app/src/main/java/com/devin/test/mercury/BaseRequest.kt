package com.devin.test.mercury

import com.devin.mercury.annotation.Post
import com.devin.model.mercury.MercuryRequest

@Post(url = "http://www.baidu.com")
class BaseRequest(id: String, name: String) : MercuryRequest()