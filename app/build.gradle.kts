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
        versionCode = 3
        versionName = "1.2.0"

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

tasks.matching { it.name.startsWith("assemble") }.configureEach {
    doLast {
        // 1. Definisci qui la lista dei percorsi di destinazione
        val cartelleDestinazione = listOf(
            File("C:\\Users\\gianb\\Desktop\\Projects\\LegmaMiteo\\BombaRepo\\LegmaMiteo\\apks"),
            File("C:\\Users\\gianb\\Desktop\\Projects\\LegmaMiteo\\BombaApp\\LegmaSky\\apks") // Sostituisci con il tuo secondo percorso
        )

        // 2. Crea le cartelle se non esistono
        cartelleDestinazione.forEach { cartella ->
            if (!cartella.exists()) {
                cartella.mkdirs()
            }
        }

        // 3. Cerca gli APK generati e copiali in tutti i percorsi della lista
        val cartellaBuildApk = File(layout.buildDirectory.asFile.get(), "outputs/apk")
        if (cartellaBuildApk.exists()) {
            cartellaBuildApk.walkTopDown().forEach { file ->
                if (file.extension == "apk") {
                    val buildType = file.parentFile.name // debug o release
                    val nuovoNomeApk = "app-$buildType-v${android.defaultConfig.versionName}.apk"

                    // Copia l'APK in ogni cartella specificata
                    cartelleDestinazione.forEach { cartella ->
                        val fileDestinazione = File(cartella, nuovoNomeApk)
                        file.copyTo(fileDestinazione, overwrite = true)
                        println("APK copiato con successo in: ${fileDestinazione.absolutePath}")
                    }
                }
            }
        }
    }
}