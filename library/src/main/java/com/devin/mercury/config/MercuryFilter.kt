package com.devin.mercury.config

import com.devin.mercury.model.MercuryFilterModel
import java.lang.reflect.Type

interface MercuryFilter {

    fun body(body: String, clazz: Type): MercuryFilterModel
}