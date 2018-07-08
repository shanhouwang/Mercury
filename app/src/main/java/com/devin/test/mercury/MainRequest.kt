package com.devin.test.mercury

import com.devin.mercury.annotation.Cache
import com.devin.mercury.annotation.Get
import com.devin.model.mercury.MercuryRequest

@Get(url = "")
@Cache
class MainRequest(var id: String, var name: String) : BaseRequest()