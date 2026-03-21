plugins {
    id("nofy.android.library")
}

android {
    namespace = "com.segnities007.datastore"
}

dependencies {
    implementation(project(":core:settings"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.security.crypto)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
