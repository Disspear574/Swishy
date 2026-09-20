import com.disspear574.swishy.convention.external.androidDependencies
import com.disspear574.swishy.convention.external.commonDependencies

plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.media"
    }

    sourceSets {
        commonDependencies {
            implementation(libs.bundles.compose.common)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
        androidDependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
