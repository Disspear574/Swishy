plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.app"
    }

    // Not "Swishy": the app target has that name and `import Swishy` would import itself.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SwishyKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.compose.common)
            api(libs.bundles.decompose.full)
            api(projects.shared.core.media)
            api(projects.shared.core.decisions)
            api(projects.shared.features.gallery)
            implementation(projects.shared.designSystem)
        }
    }
}
