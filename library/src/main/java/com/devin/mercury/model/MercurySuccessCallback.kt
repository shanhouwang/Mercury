package com.devin.mercury.model

interface MercurySuccessCallback<T> {
    fun callback(result: T)
}