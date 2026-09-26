plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.designsystem"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.bundles.compose.common)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

compose.resources {
    packageOfResClass = "com.disspear574.swishy.designsystem.resources"
}
