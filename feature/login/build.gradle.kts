plugins {
    id("nofy.android.feature")
}

android {
    namespace = "com.segnities007.login"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:biometric"))
    implementation(project(":core:navigation"))
    implementation(project(":core:auth"))
    implementation(project(":core:crypto"))
    
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
