plugins {
    id("nofy.android.library")
}

android {
    namespace = "com.segnities007.settings"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
}
