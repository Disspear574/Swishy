plugins {
    alias(libs.plugins.swishy.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

// KSP has no common task, so the Room compiler is added per target.
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

kotlin {
    android {
        namespace = "com.disspear574.swishy.decisions"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core.media)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
