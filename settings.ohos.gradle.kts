pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        mavenLocal()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        gradlePluginPortal()
        mavenLocal()
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/") }
    }
}

val ohosBuildfFileName = "build.ohos.gradle.kts"
rootProject.buildFileName = ohosBuildfFileName
rootProject.name = "kmp_kuikly"

include(":core-navigation")
include(":core-data")
include(":core-pager")
include(":platform-permission")
include(":platform-share")
include(":feature-auth")
include(":feature-feed")
include(":feature-home")
include(":app-shared")
project(":app-shared").buildFileName = ohosBuildfFileName
project(":core-pager").buildFileName = ohosBuildfFileName
project(":core-data").buildFileName = ohosBuildfFileName
project(":core-navigation").buildFileName = ohosBuildfFileName
project(":platform-permission").buildFileName = ohosBuildfFileName
project(":platform-share").buildFileName = ohosBuildfFileName
project(":feature-auth").buildFileName = ohosBuildfFileName
project(":feature-feed").buildFileName = ohosBuildfFileName
project(":feature-home").buildFileName = ohosBuildfFileName
