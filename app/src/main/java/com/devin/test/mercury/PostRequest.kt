package com.devin.test.mercury

import com.devin.mercury.annotation.Cache
import com.devin.mercury.annotation.Get
import com.devin.mercury.annotation.Post
import com.devin.mercury.annotation.ZipParams
import com.devin.model.mercury.MercuryRequest

@Post(url = "")
class PostRequest(@ZipParams var p: Params) : MercuryRequest<String>()

class Params(val id: String, val name: String, var year: Int)