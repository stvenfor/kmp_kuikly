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
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release")
    }

    js(IR) {
        browser {
            webpackTask {
                outputFileName = "nativevue2.js"
            }
            commonWebpackConfig {
                output?.library = null
                devtool = "source-map"
            }
        }
        binaries.executable()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            freeCompilerArgs = freeCompilerArgs + getCommonCompilerArgs()
            isStatic = true
            license = "MIT"
        }
        extraSpecAttributes["resources"] = "['src/commonMain/assets/**']"
    }

    sourceSets {
        val commonMain by getting {
            // Feature pages live under feature-*/src/pages so KSP in this entry module still sees @Page.
            kotlin.srcDir(project(":feature-auth").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-feed").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-analytics").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-settings").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-chat").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-bfui").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-community").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-video").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-mall").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-pay").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-classroom").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-music").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-ai").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-wallet").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-live").file("src/pages/kotlin"))
            kotlin.srcDir(project(":feature-friend").file("src/pages/kotlin"))
            dependencies {
                api(project(":core-data"))
                api(project(":core-navigation"))
                api(project(":core-design"))
                api(project(":feature-auth"))
                api(project(":feature-feed"))
                api(project(":feature-analytics"))
                api(project(":feature-settings"))
                api(project(":feature-chat"))
                api(project(":feature-bfui"))
                api(project(":feature-community"))
                api(project(":feature-video"))
                api(project(":feature-mall"))
                api(project(":feature-pay"))
                api(project(":feature-classroom"))
                api(project(":feature-music"))
                api(project(":feature-ai"))
                api(project(":feature-wallet"))
                api(project(":feature-live"))
                api(project(":feature-friend"))
                api(project(":platform-chart"))
                api(project(":platform-im"))
                api(project(":platform-push"))
                api(project(":platform-realtime"))
                api(project(":platform-linking"))
                api(project(":platform-face-verify"))
                api(project(":platform-bluetooth"))
                api(project(":platform-login-share-pay"))
                api(project(":platform-dokit"))
                api(project(":platform-dokit-bootstrap"))
                api(project(":platform-permission"))
                api(project(":platform-share"))
                implementation("com.tencent.kuikly-open:core:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:core-annotations:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:compose:${Version.getKuiklyVersion()}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":core-data"))
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
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

group = "com.example.kuikly"
version = System.getenv("kuiklyBizVersion") ?: "1.0.0"

publishing {
    repositories {
        maven {
            credentials {
                username = System.getenv("mavenUserName") ?: ""
                password = System.getenv("mavenPassword") ?: ""
            }
            rootProject.properties["mavenUrl"]?.toString()?.let { url = uri(it) }
        }
    }
}

ksp {
    arg(KEY_PAGE_NAME, getPageName())
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
    namespace = "com.example.kuikly.app.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
    sourceSets {
        named("main") {
            assets.srcDirs("src/commonMain/assets")
        }
    }
}

fun getPageName(): String {
    return (project.properties[KEY_PAGE_NAME] as? String) ?: ""
}

fun getCommonCompilerArgs(): List<String> {
    return listOf("-Xallocator=std")
}

fun getLinkerArgs(): List<String> {
    return listOf()
}

configure<KuiklyConfig> {
    js {
        outputName("nativevue2")
    }
}
