plugins {
    kotlin("multiplatform")
}

kotlin {
    ohosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
    }
}
