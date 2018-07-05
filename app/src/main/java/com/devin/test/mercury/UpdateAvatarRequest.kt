package com.devin.test.mercury

import com.devin.mercury.MercuryContentType
import com.devin.mercury.annotation.ContentType
import com.devin.mercury.annotation.Post
import com.devin.model.mercury.MercuryRequest
import java.io.File

@Post(url = "")
@ContentType(type = MercuryContentType.FORM_DATA)
class UpdateAvatarRequest(var userId: String, var avatar: File) : MercuryRequest()