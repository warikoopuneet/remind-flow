plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.remindflow.app"
    compileSdk = 35

    val appVersion = (project.findProperty("version") as? String) ?: "1.0.0"

    defaultConfig {
        applicationId = "com.remindflow.app"
        minSdk = 26
        targetSdk = 35
        versionCode = (project.findProperty("versionCode") as? String)?.toIntOrNull() ?: 1
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val storePasswordProp = (project.findProperty("REMINDFLOW_STORE_PASSWORD") as? String)
        ?: System.getenv("REMINDFLOW_STORE_PASSWORD")
    val keyPasswordProp = (project.findProperty("REMINDFLOW_KEY_PASSWORD") as? String)
        ?: System.getenv("REMINDFLOW_KEY_PASSWORD")
    val keyAliasProp = (project.findProperty("REMINDFLOW_KEY_ALIAS") as? String)
        ?: System.getenv("REMINDFLOW_KEY_ALIAS") ?: "remindflow"
    val keystoreFile = file("release.keystore")

    signingConfigs {
        create("release") {
            storeFile = keystoreFile
            storePassword = storePasswordProp
            keyAlias = keyAliasProp
            keyPassword = keyPasswordProp
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            if (keystoreFile.exists() && !storePasswordProp.isNullOrBlank() && !keyPasswordProp.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val version = variant.versionName
                if (variant.buildType.name == "release") {
                    output.outputFileName = "remindflow-${version}.apk"
                } else {
                    output.outputFileName = "remindflow-${version}-debug.apk"
                }
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
