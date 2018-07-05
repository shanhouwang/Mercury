package com.devin.mercury.annotation

@Target(AnnotationTarget.CLASS)
annotation class Put(val url: String = "")