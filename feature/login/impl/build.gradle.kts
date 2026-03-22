plugins {
    id("nofy.android.feature")
}

android {
    namespace = "com.segnities007.login"
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(project(":feature:login:api"))
    implementation(project(":feature:note:api"))
    implementation(project(":platform:designsystem"))
    implementation(project(":platform:biometric"))
    implementation(project(":platform:navigation"))
    implementation(project(":shared:auth"))
    implementation(project(":platform:crypto"))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
}
