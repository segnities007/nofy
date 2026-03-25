plugins {
    id("nofy.android.library")
}

android {
    namespace = "com.segnities007.auth"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(platform(libs.koin.bom))
    implementation(project(":platform:crypto"))
    implementation(project(":platform:database"))
    implementation(project(":platform:storage"))
    implementation(project(":platform:biometric"))
    implementation(libs.koin.core)
    
    implementation(libs.androidx.biometric)
    
    testImplementation(libs.junit)
}
