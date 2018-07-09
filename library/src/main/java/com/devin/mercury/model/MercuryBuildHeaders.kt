package com.devin.mercury.model

interface MercuryBuildHeaders {
    fun buildHeaders(): MutableMap<String, String>
}