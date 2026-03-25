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
    implementation(project(":feature:note:api"))
    implementation(project(":platform:localtransfer"))
    implementation(project(":platform:biometric"))
    implementation(project(":platform:crypto"))
    implementation(project(":platform:designsystem"))
    implementation(project(":platform:navigation"))
    implementation(project(":shared:auth")) // 認証APIを利用
    implementation(project(":shared:settings"))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.google.play.services.oss.licenses)
    implementation(libs.zxing.core)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    testImplementation(libs.junit)
}
