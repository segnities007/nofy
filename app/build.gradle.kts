import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "com.segnities007.nofy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.segnities007.nofy"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    val hasKeystoreProperties = keystorePropertiesFile.exists().also { exists ->
        if (exists) keystorePropertiesFile.reader().use { keystoreProperties.load(it) }
    }
    val envKeystorePath = System.getenv("RELEASE_KEYSTORE_FILE")
    val releaseStoreFile = when {
        hasKeystoreProperties ->
            keystoreProperties.getProperty("storeFile")?.let { rootProject.file(it) }
        !envKeystorePath.isNullOrBlank() -> file(envKeystorePath)
        else -> null
    }
    val hasReleaseSigning = releaseStoreFile?.exists() == true && when {
        hasKeystoreProperties -> listOf(
            keystoreProperties.getProperty("keyAlias"),
            keystoreProperties.getProperty("keyPassword"),
            keystoreProperties.getProperty("storePassword"),
        ).all { !it.isNullOrBlank() }
        else -> listOf(
            System.getenv("RELEASE_KEY_ALIAS"),
            System.getenv("RELEASE_KEY_PASSWORD"),
            System.getenv("RELEASE_STORE_PASSWORD"),
        ).all { !it.isNullOrBlank() }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile
                if (hasKeystoreProperties) {
                    keyAlias = keystoreProperties.getProperty("keyAlias")!!
                    keyPassword = keystoreProperties.getProperty("keyPassword")!!
                    storePassword = keystoreProperties.getProperty("storePassword")!!
                } else {
                    keyAlias = System.getenv("RELEASE_KEY_ALIAS")!!
                    keyPassword = System.getenv("RELEASE_KEY_PASSWORD")!!
                    storePassword = System.getenv("RELEASE_STORE_PASSWORD")!!
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(project(":feature:login:api"))
    implementation(project(":feature:login:impl"))
    implementation(project(":feature:note:api"))
    implementation(project(":feature:note:impl"))
    implementation(project(":feature:setting:api"))
    implementation(project(":feature:setting:impl"))
    
    implementation(project(":platform:designsystem"))
    implementation(project(":platform:navigation"))
    implementation(project(":platform:biometric"))
    implementation(project(":platform:crypto"))
    implementation(project(":platform:database"))
    implementation(project(":platform:storage"))
    implementation(project(":shared:settings"))
    implementation(project(":shared:auth"))
    implementation(libs.koin.android)
    
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.google.play.services.oss.licenses)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
