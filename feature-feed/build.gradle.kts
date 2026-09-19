import com.tencent.kuikly.gradle.config.KuiklyConfig

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("maven-publish")
    id("com.tencent.kuikly-open.kuikly")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

val KEY_PAGE_NAME = "pageName"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "1.8" }
        }
        publishLibraryVariants("release")
    }

    js(IR) {
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Kuikly submodule feature_feed"
        homepage = "https://github.com/stvenfor/kmp_kuikly"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "feature_feed"
            freeCompilerArgs = freeCompilerArgs + listOf("-Xallocator=std")
            isStatic = true
            license = "MIT"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core-pager"))
                api(project(":core-data"))
                api(project(":core-navigation"))
                implementation("com.tencent.kuikly-open:core:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:core-annotations:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:compose:${Version.getKuiklyVersion()}")
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core-render-android:${Version.getKuiklyVersion()}")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

group = "com.example.kuikly"
version = System.getenv("kuiklyBizVersion") ?: "1.0.0"

ksp {
    arg(KEY_PAGE_NAME, (project.properties[KEY_PAGE_NAME] as? String) ?: "")
    arg("moduleId", "feature_feed")
    arg("isMainModule", "false")
    arg("enableMultiModule", "true")
}

dependencies {
    compileOnly("com.tencent.kuikly-open:core-ksp:${Version.getKuiklyVersion()}") {
        add("kspAndroid", this)
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
        add("kspJs", this)
    }
}

android {
    namespace = "com.example.kuikly.feature.feed"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
}
