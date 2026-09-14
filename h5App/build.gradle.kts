import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser {
            webpackTask {
                outputFileName = "h5App.js"
            }
            commonWebpackConfig {
                output?.library = null
            }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                // Published Maven coords (KuiklyUI monorepo uses project(":core-render-web:*"))
                implementation("com.tencent.kuikly-open.core-render-web:base:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open.core-render-web:h5:${Version.getKuiklyVersion()}")
            }
        }
    }
}

// Business KMP module that packs nativevue2.js
val businessPathName = "app-shared"

fun copyLocalJSBundle(buildSubPath: String) {
    val destDir = Paths.get(project.buildDir.absolutePath, buildSubPath, "page").toFile()
    if (!destDir.exists()) {
        destDir.mkdirs()
    } else {
        destDir.deleteRecursively()
    }

    val sourceDir = Paths.get(project.buildDir.absolutePath, buildSubPath, "kotlin2js").toFile()
    val zipFile = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "build", "outputs", "kuikly", "js", "release", "local", "nativevue2.zip"
    ).toFile()
    val zipDir = Paths.get(project.buildDir.absolutePath, buildSubPath, "kotlin2js").toFile()
    if (!zipDir.exists()) {
        zipDir.mkdirs()
    } else {
        zipDir.deleteRecursively()
    }
    project.copy {
        from(zipTree(zipFile))
        into(zipDir)
    }
    project.copy {
        from(sourceDir) {
            include("nativevue2.js")
        }
        into(destDir)
    }
    delete(sourceDir)
}

fun copySplitJSBundle(buildSubPath: String) {
    val destDir = Paths.get(project.buildDir.absolutePath, buildSubPath, "page").toFile()
    if (!destDir.exists()) {
        destDir.mkdirs()
    } else {
        destDir.deleteRecursively()
    }
    val sourceDir = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "build", "outputs", "kuikly", "js", "release", "split", "page"
    ).toFile()
    project.copy {
        from(sourceDir) {
            include("*.js")
        }
        into(destDir)
    }
}

fun generateLocalHtml(buildSubPath: String) {
    val filePath = Paths.get(project.buildDir.absolutePath, buildSubPath, "index.html")
    if (Files.exists(filePath)) {
        val fileContent = Files.readString(filePath)
        val placeText = "http://127.0.0.1:8083/nativevue2.js"
        val updatedContent = fileContent.replace(placeText, "page/nativevue2.js")
        Files.writeString(filePath, updatedContent, StandardCharsets.UTF_8)
        println("generate local html file success.")
    }
}

fun generateSplitHtml(buildSubPath: String) {
    val htmlFilePath = Paths.get(project.buildDir.absolutePath, buildSubPath, "index.html")
    if (Files.exists(htmlFilePath)) {
        val fileContent = Files.readString(htmlFilePath)
        val placeText = "http://127.0.0.1:8083/nativevue2.js"
        val pagePath = Paths.get(project.buildDir.absolutePath, buildSubPath, "page")
        val pageDir = file(pagePath)
        if (pageDir.exists()) {
            val files = pageDir.listFiles()
            files?.forEach { f ->
                if (f.isFile) {
                    val fileName = f.name
                    val updatedContent = fileContent.replace(placeText, "page/$fileName")
                    val filePath = Paths.get(
                        project.buildDir.absolutePath,
                        buildSubPath,
                        "${f.nameWithoutExtension}.html"
                    )
                    Files.writeString(filePath, updatedContent, StandardCharsets.UTF_8)
                }
            }
            htmlFilePath.toFile().delete()
            println("generate local html file success.")
        } else {
            println("generate local html file failure, no such files.")
        }
    }
}

fun copyAssetsResource(buildSubPath: String) {
    val sourceDir = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "build",
        "outputs",
        "kuikly",
        "assets"
    )
    if (sourceDir.toFile().exists()) {
        val destDir = Paths.get(
            project.rootDir.absolutePath,
            "h5App",
            "build",
            buildSubPath,
            "assets"
        )
        project.copy {
            from(sourceDir)
            into(destDir)
        }
    } else {
        print("dest directory not exist")
    }
}

fun copyAssetsFileToWebpackDevServer() {
    val sourceDir = Paths.get(
        project.rootDir.absolutePath,
        businessPathName,
        "src",
        "commonMain",
        "assets"
    )
    if (sourceDir.toFile().exists()) {
        val destDir = Paths.get(
            project.rootDir.absolutePath,
            "h5App",
            "build", "processedResources", "js", "main", "assets"
        )
        project.copy {
            from(sourceDir)
            into(destDir)
        }
    } else {
        print("dest directory not exist")
    }
}

project.afterEvaluate {
    tasks.register("publishLocalJSBundle") {
        group = "kuikly"
        dependsOn("jsBrowserDistribution")
        doFirst {
            copyLocalJSBundle("dist/js/productionExecutable")
            copyAssetsResource("dist/js/productionExecutable")
        }
        doLast {
            generateLocalHtml("dist/js/productionExecutable")
        }
    }

    tasks.register("publishSplitJSBundle") {
        group = "kuikly"
        dependsOn("jsBrowserDistribution")
        doFirst {
            copySplitJSBundle("dist/js/productionExecutable")
            copyAssetsResource("dist/js/productionExecutable")
        }
        doLast {
            generateSplitHtml("dist/js/productionExecutable")
        }
    }

    tasks.register("copyAssetsToWebpackDevServer") {
        group = "kuikly"
        doLast {
            copyAssetsFileToWebpackDevServer()
        }
    }
}
