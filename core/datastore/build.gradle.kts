plugins {
    id("nofy.android.library")
}

android {
    namespace = "com.segnities007.datastore"
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(project(":shared:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.koin.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
