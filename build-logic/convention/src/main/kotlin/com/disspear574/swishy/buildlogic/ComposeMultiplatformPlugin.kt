package com.disspear574.swishy.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Multiplatform module with Compose UI and Compose resources. */
class ComposeMultiplatformPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(KmpLibraryPlugin::class.java)
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("org.jetbrains.compose")

        extensions.configure<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                androidResources.enable = true
            }
            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.findLibrary("compose-ui-tooling-preview").get())
            }
        }
        dependencies.add("androidRuntimeClasspath", libs.findLibrary("compose-ui-tooling").get())
    }
}
