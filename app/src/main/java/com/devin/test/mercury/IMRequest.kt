package com.devin.test.mercury

import com.devin.mercury.annotation.*
import com.devin.model.mercury.MercuryRequest

@Get(url = "user/{id}")
@Cache
class IMRequest(@Path(value = "id") var id: String, @Encode(value = true) var name: String) : MercuryRequest("IM")