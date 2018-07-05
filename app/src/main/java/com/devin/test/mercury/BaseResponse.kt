package com.devin.test.mercury

class BaseResponse {
    var code = -1
    var msg = ""

    override fun toString(): String {
        return "code, $code msg, $msg"
    }
}