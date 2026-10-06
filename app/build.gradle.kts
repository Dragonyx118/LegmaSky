plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.legmasky"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.legmasky"
        minSdk = 24
        targetSdk = 37
        versionCode = 140
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    implementation("androidx.palette:palette:1.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}

val appVersionName = android.defaultConfig.versionName ?: "1.0"
val apkOutputDir = layout.buildDirectory.dir("outputs/apk")

val copyApksTask = tasks.register("copyGeneratedApks") {
    val version = appVersionName
    val buildDirFile = apkOutputDir

    doLast {
        val outputFolder = buildDirFile.get().asFile
        val cartelleDestinazione = listOf(
            File("C:\\Users\\gianb\\Desktop\\Projects\\LegmaMiteo\\BombaRepo\\LegmaMiteo\\apks"),
            File("C:\\Users\\gianb\\Desktop\\Projects\\LegmaMiteo\\BombaApp\\LegmaSky\\apks")
        )

        cartelleDestinazione.forEach { cartella ->
            if (!cartella.exists()) {
                cartella.mkdirs()
            }
        }

        if (outputFolder.exists()) {
            outputFolder.walkTopDown().forEach { file ->
                // Filtro: deve essere APK e pesare più di 2 MB (2 * 1024 * 1024 byte)
                if (file.extension == "apk" && file.length() > 2 * 1024 * 1024) {

                    // Nome personalizzato: legmasky-v1.2.0.apk (o legmasky-release-v1.2.0.apk se vuoi specificare il buildType)
                    val buildType = file.parentFile.name // release / debug
                    val nuovoNomeApk = "legmasky-$buildType-v$version.apk"

                    cartelleDestinazione.forEach { cartella ->
                        val fileDestinazione = File(cartella, nuovoNomeApk)
                        file.copyTo(fileDestinazione, overwrite = true)
                        println("APK valido copiato in: ${fileDestinazione.absolutePath} (${file.length() / 1024 / 1024} MB)")
                    }
                }
            }
        }
    }
}

tasks.matching { it.name.startsWith("assemble") }.configureEach {
    finalizedBy(copyApksTask)
}

tasks.register("printVersionName") { doLast { println(android.defaultConfig.versionName) } }
tasks.register("printVersionCode") { doLast { println(android.defaultConfig.versionCode) } }