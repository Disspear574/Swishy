package com.disspear574.swishy.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.impl.VariantOutputImpl
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.util.Properties

/** Android app host: SDK levels, Compose, R8 for release, version from `config/version.properties`. */
class AndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val java = versionInt("java")
        val versionName = versionName()
        val versionCode = commitCount()

        extensions.configure<ApplicationExtension> {
            compileSdk = versionInt("compile-sdk")
            defaultConfig {
                minSdk = versionInt("min-sdk")
                targetSdk = versionInt("target-sdk")
                this.versionName = versionName
                this.versionCode = versionCode
            }
            buildFeatures { compose = true }
            packaging { resources.excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1") }
            buildTypes {
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.toVersion(java)
                targetCompatibility = JavaVersion.toVersion(java)
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(java)
            compilerOptions { freeCompilerArgs.addAll(compilerFlags) }
        }

        extensions.configure<ApplicationAndroidComponentsExtension> {
            onVariants { variant ->
                variant.outputs.forEach { output ->
                    (output as VariantOutputImpl).outputFileName.set(
                        "${rootProject.name}-$versionName-$versionCode-${variant.name}.apk",
                    )
                }
            }
        }
    }

    private fun Project.versionName(): String {
        val text = providers.fileContents(rootProject.layout.projectDirectory.file("config/version.properties"))
            .asText.get()
        val properties = Properties().apply { load(text.reader()) }
        return listOf("MAJOR", "MINOR", "PATCH").joinToString(".") { properties.getProperty(it).trim() }
    }

    private fun Project.commitCount(): Int = runCatching {
        providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            workingDir = rootProject.projectDir
            isIgnoreExitValue = true
        }.standardOutput.asText.get().trim().toInt()
    }.getOrDefault(1)
}
