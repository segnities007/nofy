plugins {
    id("nofy.android.library")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.segnities007.database"
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // SQLCipher for database encryption
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
