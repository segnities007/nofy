plugins {
    id("nofy.android.feature")
}

android {
    namespace = "com.segnities007.note"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":core:database"))
    implementation(project(":core:crypto"))
    implementation(project(":core:auth"))
    
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
