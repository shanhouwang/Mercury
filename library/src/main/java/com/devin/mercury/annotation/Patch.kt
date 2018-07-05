package com.devin.mercury.annotation

@Target(AnnotationTarget.CLASS)
annotation class Patch(val url: String = "")