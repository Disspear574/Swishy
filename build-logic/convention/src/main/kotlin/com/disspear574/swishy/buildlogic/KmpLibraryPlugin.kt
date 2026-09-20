package com.disspear574.swishy.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Kotlin Multiplatform module for Android and iOS (device and Apple silicon simulator). */
class KmpLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(versionInt("java"))
            compilerOptions { freeCompilerArgs.addAll(compilerFlags) }
            iosArm64()
            iosSimulatorArm64()

            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                compileSdk = versionInt("compile-sdk")
                minSdk = versionInt("min-sdk")
                compilerOptions { jvmTarget.set(JvmTarget.fromTarget(versionInt("java").toString())) }
                withHostTest { isReturnDefaultValues = true }
            }
        }
    }
}
