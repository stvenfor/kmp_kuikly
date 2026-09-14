package com.example.kuikly.platform.permission

actual object PermissionApi {
    actual fun checkCamera(): String = "ios:granted(mock)"
}
