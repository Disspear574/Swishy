// One classloader for the Android, Kotlin and Compose plugins across every module.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

// Applied to the root only: detekt 1.23 is built against Kotlin 2.0 and fails on a KMP module.
detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    parallel = true
    source.setFrom(
        fileTree(rootDir) {
            include("shared/**/src/*Main/kotlin/**/*.kt")
            include("shared/**/src/*Test/kotlin/**/*.kt")
            include("apps/**/src/**/*.kt")
            exclude("**/build/**")
        },
    )
}
