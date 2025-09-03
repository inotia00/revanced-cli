plugins {
    kotlin("jvm") version "2.0.21"
    alias(libs.plugins.shadow)
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
    implementation(project(":revanced-lib"))
    implementation(libs.revanced.patcher)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.picocli)

    testImplementation(libs.kotlin.test)
}

kotlin { jvmToolchain(11) }

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }

    processResources {
        expand("projectVersion" to project.version)
    }

    shadowJar {
        exclude("/prebuilt/linux/aapt", "/prebuilt/windows/aapt.exe", "/prebuilt/*/aapt_*")
        manifest {
            attributes("Main-Class" to "app.revanced.cli.command.MainCommandKt")
        }
        minimize {
            exclude(dependency("org.bouncycastle:.*"))
            exclude(dependency("app.revanced:revanced-patcher"))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    // Dummy task to fix the Gradle semantic-release plugin.
    // Remove this if you forked it to support building only.
    // Tracking issue: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    register<DefaultTask>("publish") {
        group = "publish"
        description = "Dummy task"
        dependsOn(build)
    }
}
