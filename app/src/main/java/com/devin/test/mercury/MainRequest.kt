package com.devin.test.mercury

import com.devin.mercury.annotation.*
import com.devin.mercury.annotation.Test
import com.devin.model.mercury.MercuryRequest
import java.io.Serializable

@Post(url = "user")
@Test(value = "{\"code\": 0,\"success\": true,\"data\": {\"name\": \"devin\",\"age\": 28,\"gender\": \"男\"}}")
class MainRequest(@ZipParams val params: MainRequestParams) : MercuryRequest<CommonResponse<Person>>()
class MainRequestParams(@Ignore var id: String = "", @Ignore var name: String = "") : Serializable

data class Person(val name: String, val age: Int, val gender: String)