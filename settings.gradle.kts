pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "kmp_kuikly"

include(":androidApp")
include(":h5App")

include(":core-navigation")
include(":core-data")
include(":platform-permission")
include(":platform-share")
include(":feature-auth")
include(":feature-feed")
include(":core-design")
include(":feature-analytics")
include(":feature-settings")
include(":feature-chat")
include(":feature-bfui")
include(":feature-community")
include(":feature-video")
include(":feature-mall")
include(":feature-pay")
include(":feature-classroom")
include(":feature-music")
include(":feature-ai")
include(":feature-wallet")
include(":feature-live")
include(":feature-friend")
include(":platform-chart")
include(":platform-im")
include(":platform-push")
include(":platform-realtime")
include(":platform-linking")
include(":platform-face-verify")
include(":platform-bluetooth")
include(":platform-login-share-pay")
include(":platform-dokit")
include(":platform-dokit-bootstrap")
include(":app-shared")
