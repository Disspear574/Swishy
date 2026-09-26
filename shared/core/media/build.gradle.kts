plugins {
    alias(libs.plugins.swishy.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.media"
    }

    sourceSets {
        commonMain.dependencies {
            // Compose for ImageBitmap in signatures and for the platform views of PhotoCard and VideoCard.
            implementation(libs.bundles.compose.common)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
