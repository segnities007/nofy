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
include(":app")

include(":feature:login")
include(":feature:note")
include(":feature:setting")

include(":core:navigation")
include(":core:database")
include(":core:datastore")
include(":core:settings")
include(":core:crypto")
include(":core:biometric")
include(":core:designsystem")
include(":core:auth")
