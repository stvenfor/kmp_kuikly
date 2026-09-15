plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

kotlin {
    ohosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuikly-open:core:2.16.0-2.0.21-ohos")
                implementation("com.tencent.kuikly-open:core-annotations:2.16.0-2.0.21-ohos")
                implementation("com.tencent.kuikly-open:compose:2.16.0-2.0.21-ohos")
            }
        }
    }
}
