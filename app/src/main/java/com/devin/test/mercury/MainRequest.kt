package com.devin.test.mercury

import com.devin.mercury.annotation.*

@Get(url = "user/{id}")
@Cache
class MainRequest(@Path(value = "id") var id: String, var name: String) : BaseRequest()