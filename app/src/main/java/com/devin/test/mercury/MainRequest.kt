package com.devin.test.mercury

import com.devin.mercury.annotation.*

@Post(url = "user/{id}")
@Cache
class MainRequest(@Path(value = "id") var id: String, @Encode(value = true) var name: String) : BaseRequest()