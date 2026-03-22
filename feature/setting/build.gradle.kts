plugins {
    id("nofy.android.feature")
}

android {
    namespace = "com.segnities007.setting"
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(project(":feature:setting:api"))
    implementation(project(":feature:login:api"))
    implementation(project(":platform:biometric"))
    implementation(project(":platform:crypto"))
    implementation(project(":platform:designsystem"))
    implementation(project(":platform:navigation"))
    implementation(project(":shared:auth")) // 認証APIを利用
    implementation(project(":shared:settings"))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
