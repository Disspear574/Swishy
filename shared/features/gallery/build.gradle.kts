plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.gallery"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.compose.common)
            implementation(libs.lifecycle.runtime.compose)
            // api: ComponentContext is part of the component's public signature.
            api(libs.bundles.decompose.full)
            implementation(projects.shared.designSystem)
            implementation(projects.shared.core.strings)
            api(projects.shared.core.media)
            api(projects.shared.core.decisions)
        }
    }
}
