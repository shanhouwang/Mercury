package com.devin.test.mercury

import com.devin.mercury.annotation.*

@Post(url = "user/{id}/{name}")
@Cache
class MainRequest(@Path(value = "id") var id: String, @Path var name: String) : BaseRequest()