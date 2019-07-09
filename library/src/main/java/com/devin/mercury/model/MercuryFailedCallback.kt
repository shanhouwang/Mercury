package com.devin.mercury.model

interface MercuryFailedCallback {
    fun callback(exception: MercuryException)
}