import com.disspear574.swishy.convention.external.commonDependencies

plugins {
    alias(libs.plugins.swishy.kmp.library)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.decisions"
    }

    sourceSets {
        commonDependencies {
            api(projects.shared.core.media)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
