package com.devin.test.mercury

import com.devin.mercury.annotation.Encode
import com.devin.mercury.annotation.Get
import com.devin.mercury.annotation.Path
import com.devin.model.mercury.MercuryRequest

@Get(url = "user/{id}")
class MainRequest(@Path(value = "id") var id: String, @Encode(value = true) var name: String) : MercuryRequest<String>()