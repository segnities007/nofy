pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.android.gms.oss-licenses-plugin") {
                useModule("com.google.android.gms:oss-licenses-plugin:0.11.0")
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "nofy"

include(
    ":app",

    // app
    ":feature:login:api",
    ":feature:login:impl",
    ":feature:note:api",
    ":feature:note:impl",
    ":feature:setting:api",
    ":feature:setting:impl",

    // platform
    ":platform:navigation",
    ":platform:localtransfer",
    ":platform:database",
    ":platform:storage",
    ":platform:crypto",
    ":platform:biometric",
    ":platform:designsystem",

    // shared
    ":shared:settings",
    ":shared:auth",
)
