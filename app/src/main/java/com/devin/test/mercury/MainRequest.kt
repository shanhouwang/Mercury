package com.devin.test.mercury

import com.devin.mercury.annotation.*

@Get(url = "user/{id}")
class MainRequest(@Path(value = "id") var id: String, @Encode(value = true) var name: String) : BaseRequest()