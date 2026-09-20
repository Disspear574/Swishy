import com.disspear574.swishy.convention.external.commonDependencies

plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.designsystem"
    }

    sourceSets {
        commonDependencies {
            api(libs.bundles.compose.common)
        }
    }
}
