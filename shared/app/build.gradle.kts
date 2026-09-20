import com.disspear574.swishy.convention.external.commonDependencies
import com.disspear574.swishy.convention.external.setupIosStaticFramework

plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.app"
    }

    setupIosStaticFramework(name = "SwishyKit") {}

    sourceSets {
        commonDependencies {
            implementation(libs.bundles.compose.common)
            api(projects.shared.core.media)
            api(projects.shared.core.decisions)
            implementation(projects.shared.features.gallery)
            implementation(projects.shared.designSystem)
        }
    }
}
