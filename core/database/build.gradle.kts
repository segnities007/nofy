plugins {
    id("nofy.android.library")
}

android {
    namespace = "com.segnities007.database"
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
