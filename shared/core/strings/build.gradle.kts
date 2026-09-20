plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.strings"
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.disspear574.swishy.strings"
}
