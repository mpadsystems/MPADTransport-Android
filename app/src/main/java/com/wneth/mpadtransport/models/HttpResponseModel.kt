package com.wneth.mpadtransport.models

data class HttpResponseModel(
    val status: Int = 0,
    val message: String = "",
    val data: Map<String, Any> = emptyMap()) {
}