@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Swishy"

include(":apps:androidApp")
include(":shared:app")
include(":shared:design-system")
include(":shared:core:strings")
include(":shared:core:media")
include(":shared:core:decisions")
include(":shared:features:gallery")
