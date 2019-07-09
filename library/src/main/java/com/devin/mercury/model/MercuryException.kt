package com.devin.mercury.model

data class MercuryException(val errorCode: Int, val errorMessage: String)

const val IO_EXCEPTION = -0x1000

const val DATA_PARSER_EXCEPTION = -0x1001

const val BUSINESS_CODE_EXCEPTION = -0x1002

const val OTHER_EXCEPTION = -0x1003
