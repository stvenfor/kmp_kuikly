package com.example.kuikly.platform.share

actual object ShareApi {
    actual fun shareText(text: String): String = "ohos:shared:$text"
}
