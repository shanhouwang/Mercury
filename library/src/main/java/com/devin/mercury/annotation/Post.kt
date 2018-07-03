package com.devin.mercury.annotation

@Target(AnnotationTarget.CLASS)
annotation class Post(val url: String = "")