package com.example.kuikly.platform.share

actual object ShareApi {
    actual fun shareText(text: String): String = "ios:shared:$text"
}
