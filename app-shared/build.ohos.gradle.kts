plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

val KEY_PAGE_NAME = "pageName"

kotlin {
    ohosArm64 {
        binaries.sharedLib("shared") {
            freeCompilerArgs += "-Xadd-light-debug=enable"
            if (buildType == org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE) {
                val clangOpt = "-Os -mllvm -enable-machine-outliner=always -ffunction-sections"
                val clangFlags = "clangOptFlags.ohos_arm64=$clangOpt;clangDebugFlags.ohos_arm64=$clangOpt"
                freeCompilerArgs += "-Xoverride-konan-properties=$clangFlags"
                linkerOpts += "--pack-dyn-relocs=relr"
                linkerOpts += "--gc-sections"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core-pager"))
                api(project(":core-data"))
                api(project(":core-navigation"))
                api(project(":feature-auth"))
                api(project(":feature-feed"))
                api(project(":platform-permission"))
                api(project(":platform-share"))
                implementation("com.tencent.kuikly-open:core:2.16.0-2.0.21-ohos")
                implementation("com.tencent.kuikly-open:core-annotations:2.16.0-2.0.21-ohos")
                implementation("com.tencent.kuikly-open:compose:2.16.0-2.0.21-ohos")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

ksp {
    arg(KEY_PAGE_NAME, (project.properties[KEY_PAGE_NAME] as? String) ?: "")
    arg("moduleId", "app-shared")
    arg("isMainModule", "true")
    arg("subModules", "feature_auth&feature_feed")
    arg("enableMultiModule", "true")
}

dependencies {
    add("kspOhosArm64", "com.tencent.kuikly-open:core-ksp:2.16.0-2.0.21-ohos")
}
