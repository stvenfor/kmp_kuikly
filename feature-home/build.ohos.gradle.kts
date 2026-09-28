plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

val KEY_PAGE_NAME = "pageName"

kotlin {
    ohosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core-pager"))
                api(project(":core-data"))
                api(project(":core-navigation"))
                implementation("com.tencent.kuikly-open:core:2.16.0-2.0.21-ohos")
                implementation("com.tencent.kuikly-open:core-annotations:2.16.0-2.0.21-ohos")
                implementation("com.tencent.kuikly-open:compose:2.16.0-2.0.21-ohos")
            }
        }
    }
}

ksp {
    arg(KEY_PAGE_NAME, (project.properties[KEY_PAGE_NAME] as? String) ?: "")
    arg("moduleId", "feature_home")
    arg("isMainModule", "false")
    arg("enableMultiModule", "true")
}

dependencies {
    add("kspOhosArm64", "com.tencent.kuikly-open:core-ksp:2.16.0-2.0.21-ohos")
}
