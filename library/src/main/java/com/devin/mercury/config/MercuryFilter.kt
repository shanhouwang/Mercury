package com.devin.mercury.config

interface MercuryFilter {

    fun body(body: String): String
}