package com.devin.test.mercury

fun main(args: Array<String>) {
    BaseRequest("", "").request(BaseResponse::class.java, {})
}