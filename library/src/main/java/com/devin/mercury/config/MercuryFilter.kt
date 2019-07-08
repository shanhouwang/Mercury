package com.devin.mercury.config

import com.devin.mercury.model.MercuryFilterModel

interface MercuryFilter {

    fun body(body: String, clazz: Class<*>): MercuryFilterModel
}