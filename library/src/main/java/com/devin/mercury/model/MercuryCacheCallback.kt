package com.devin.mercury.model

interface MercuryCacheCallback<T> {
    fun callback(result: T)
}