import com.disspear574.swishy.convention.external.commonDependencies

plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.strings"
    }

    sourceSets {
        commonDependencies {
            api(libs.compose.resources)
            implementation(libs.compose.runtime)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.disspear574.swishy.strings"
}
