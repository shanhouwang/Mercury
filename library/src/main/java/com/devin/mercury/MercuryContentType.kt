package com.devin.mercury

class MercuryContentType {

    companion object {

        /**
         * JSON方式提交
         */
        const val JSON = "application/json"

        /**
         * 表单提交（不可上传文件）
         */
        const val FORM = "application/x-www-from-urlencoded"

        /**
         * 表单提交（可上传文件）
         */
        const val FORM_DATA = "multipart/form-data"
    }
}

