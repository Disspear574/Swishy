@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.disspear574.swishy.settings")
}

rootProject.name = "Swishy"

include(":apps:androidApp")

include(":shared:app")

include(":shared:design-system")
include(":shared:core:strings")

include(":shared:core:media")
include(":shared:core:decisions")

include(":shared:features:gallery")
