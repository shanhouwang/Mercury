package com.devin.mercury.annotation

@Target(AnnotationTarget.CLASS)
annotation class ContentType(val type: String = "")