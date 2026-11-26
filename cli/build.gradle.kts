plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("kfd.MainKt")
}

dependencies {
    implementation(project(":core"))
}