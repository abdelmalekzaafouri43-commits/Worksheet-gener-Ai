import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.worksheetai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.worksheetai"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Dynamically load the GEMINI_API_KEY from .env or System Environment
        val envFile = file("${project.rootDir}/.env")
        var geminiApiKey = ""
        if (envFile.exists()) {
            val properties = Properties()
            val fis = FileInputStream(envFile)
            properties.load(fis)
            fis.close()
            geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
        }
        if (geminiApiKey.isEmpty()) {
            geminiApiKey = System.getenv("GEMINI_API_KEY") ?: "mock_or_empty_api_key"
        }

        buildConfigField("String", "GEMINI_API_KEY", "\"${geminiApiKey}\"")
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
