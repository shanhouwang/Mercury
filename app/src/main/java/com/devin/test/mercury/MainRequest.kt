package com.devin.test.mercury

import com.devin.mercury.annotation.Encode
import com.devin.mercury.annotation.Get
import com.devin.mercury.annotation.Path
import com.devin.mercury.annotation.Test
import com.devin.model.mercury.MercuryRequest

@Get(url = "user/{id}")
@Test(value = "{\"code\": 0,\"success\": true,\"data\": {\"name\": \"devin\",\"age\": 28,\"gender\": \"男\"}}")
class MainRequest(@Path(value = "id") var id: String, @Encode(value = true) var name: String) : MercuryRequest<CommonResponse<Person>>()

data class Person(val name: String, val age: Int, val gender: String)