plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven {
        // A repository must be speficied for some reason. "registry" is a dummy.
        url = uri("https://maven.pkg.github.com/revanced/registry")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.kotlin.reflect)
    implementation(libs.jadb) // Fork with Shell v2 support.
    implementation(libs.jackson.module.kotlin)
    implementation(libs.apkzlib)
    implementation(libs.apksig)
    implementation(libs.bcpkix.jdk18on)
    implementation(libs.guava)
}

tasks {
}

kotlin { jvmToolchain(11) }

java {
    withSourcesJar()
}
