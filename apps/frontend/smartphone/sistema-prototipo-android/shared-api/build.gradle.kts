import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "br.com.romulopenha.sistemaprototipoandroid.sharedapi"
    compileSdk = 37

    defaultConfig { minSdk = 29 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = "br.com.romulopenha.sistemaprototipoandroid"
                artifactId = "shared-api"
                version = "1.1.0"
            }
        }
        repositories {
            maven {
                name = "projectLocal"
                url = layout.buildDirectory.dir("local-maven").get().asFile.toURI()
            }
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
