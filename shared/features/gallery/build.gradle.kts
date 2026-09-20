import com.disspear574.swishy.convention.external.commonDependencies

plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.gallery"
    }

    sourceSets {
        commonDependencies {
            implementation(libs.bundles.compose.common)
            api(libs.bundles.decompose.full)
            implementation(projects.shared.designSystem)
            implementation(projects.shared.core.strings)
            api(projects.shared.core.media)
            api(projects.shared.core.decisions)
        }
    }
}
