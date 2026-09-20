plugins {
    alias(libs.plugins.swishy.android.application)
}

android {
    namespace = "com.disspear574.swishy"

    defaultConfig {
        applicationId = "com.disspear574.swishy"
    }
}

dependencies {
    implementation(projects.shared.app)
    implementation(libs.androidx.activity.compose)
}
