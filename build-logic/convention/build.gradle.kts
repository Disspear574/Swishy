plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "com.disspear574.swishy.kmplibrary"
            implementationClass = "com.disspear574.swishy.buildlogic.KmpLibraryPlugin"
        }
        register("composeMultiplatform") {
            id = "com.disspear574.swishy.compose-multiplatform"
            implementationClass = "com.disspear574.swishy.buildlogic.ComposeMultiplatformPlugin"
        }
        register("androidApplication") {
            id = "com.disspear574.swishy.androidApplication"
            implementationClass = "com.disspear574.swishy.buildlogic.AndroidApplicationPlugin"
        }
    }
}
