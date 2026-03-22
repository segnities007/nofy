plugins {
    id("nofy.android.feature")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.segnities007.note"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(project(":feature:note:api"))
    implementation(project(":feature:login:api"))
    implementation(project(":feature:setting:api"))
    implementation(project(":platform:designsystem"))
    implementation(project(":platform:navigation"))
    implementation(project(":platform:database"))
    implementation(project(":platform:crypto"))
    implementation(project(":shared:auth"))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)
    ksp(libs.room.compiler)
    
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
