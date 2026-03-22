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
                useModule("com.google.android.gms:oss-licenses-plugin:0.10.10")
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
include(":app")

include(":feature:login:api")
include(":feature:login:impl")
project(":feature:login").projectDir = file("feature/login")
project(":feature:login:api").projectDir = file("feature/login/api")
project(":feature:login:impl").projectDir = file("feature/login/impl")
include(":feature:note:api")
include(":feature:note:impl")
project(":feature:note").projectDir = file("feature/note")
project(":feature:note:api").projectDir = file("feature/note/api")
project(":feature:note:impl").projectDir = file("feature/note/impl")
include(":feature:setting:api")
include(":feature:setting:impl")
project(":feature:setting").projectDir = file("feature/setting")
project(":feature:setting:api").projectDir = file("feature/setting/api")
project(":feature:setting:impl").projectDir = file("feature/setting/impl")

include(":platform:navigation")
include(":platform:database")
include(":platform:storage")
include(":platform:crypto")
include(":platform:biometric")
include(":platform:designsystem")
project(":platform").projectDir = file("platform")
project(":platform:navigation").projectDir = file("platform/navigation")
project(":platform:database").projectDir = file("platform/database")
project(":platform:storage").projectDir = file("platform/storage")
project(":platform:crypto").projectDir = file("platform/crypto")
project(":platform:biometric").projectDir = file("platform/biometric")
project(":platform:designsystem").projectDir = file("platform/designsystem")

include(":shared:settings")
include(":shared:auth")
project(":shared").projectDir = file("shared")
project(":shared:settings").projectDir = file("shared/settings")
project(":shared:auth").projectDir = file("shared/auth")
