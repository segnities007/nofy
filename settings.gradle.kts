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

include(":feature:login:api")
include(":feature:login:impl")
project(":feature:login").projectDir = file("feature/login-group")
project(":feature:login:api").projectDir = file("feature/login/api")
project(":feature:login:impl").projectDir = file("feature/login")
include(":feature:note:api")
include(":feature:note:impl")
project(":feature:note").projectDir = file("feature/note-group")
project(":feature:note:api").projectDir = file("feature/note/api")
project(":feature:note:impl").projectDir = file("feature/note")
include(":feature:setting:api")
include(":feature:setting:impl")
project(":feature:setting").projectDir = file("feature/setting-group")
project(":feature:setting:api").projectDir = file("feature/setting/api")
project(":feature:setting:impl").projectDir = file("feature/setting")

include(":platform:navigation")
include(":platform:database")
include(":platform:storage")
include(":platform:crypto")
include(":platform:biometric")
include(":platform:designsystem")
project(":platform").projectDir = file("platform-group")
project(":platform:navigation").projectDir = file("core/navigation")
project(":platform:database").projectDir = file("core/database")
project(":platform:storage").projectDir = file("core/datastore")
project(":platform:crypto").projectDir = file("core/crypto")
project(":platform:biometric").projectDir = file("core/biometric")
project(":platform:designsystem").projectDir = file("core/designsystem")

include(":shared:settings")
include(":shared:auth")
project(":shared").projectDir = file("shared-group")
project(":shared:settings").projectDir = file("core/settings")
project(":shared:auth").projectDir = file("core/auth")
