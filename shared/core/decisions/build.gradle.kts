import com.disspear574.swishy.convention.external.commonDependencies

plugins {
    alias(libs.plugins.swishy.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

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
        commonDependencies {
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
