
plugins {
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("detekt.yml"))
    parallel = true
    source.setFrom(
        files(
            fileTree(rootDir) {
                include("shared/**/src/*Main/kotlin/**/*.kt")
                include("shared/**/src/*Test/kotlin/**/*.kt")
                include("apps/**/src/**/*.kt")
                exclude("**/build/**")
            },
        ),
    )
}
