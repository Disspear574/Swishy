import com.disspear574.swishy.convention.external.commonDependencies

plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.probe"
    }

    sourceSets {
        commonDependencies {
            implementation(libs.bundles.compose.common)
            implementation(libs.compose.material3)
            implementation(projects.shared.core.strings)
            api(projects.shared.core.media)
            api(projects.shared.core.decisions)
        }
    }
}
