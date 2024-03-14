plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.auag0.hidden_api"
    defaultConfig {
        compileSdk = 34
    }
}

dependencies {
    annotationProcessor(libs.refine.annotation.processor)
    compileOnly(libs.refine.annotation)
}