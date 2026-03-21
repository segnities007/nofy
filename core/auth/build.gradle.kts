plugins {
    id("nofy.android.library")
}

android {
    namespace = "com.segnities007.auth"
}

dependencies {
    implementation(project(":core:crypto"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:biometric"))
    
    implementation(libs.androidx.biometric)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
