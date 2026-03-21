plugins {
    id("nofy.android.feature")
}

android {
    namespace = "com.segnities007.setting"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:auth")) // 認証APIを利用
    implementation(project(":core:settings"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
