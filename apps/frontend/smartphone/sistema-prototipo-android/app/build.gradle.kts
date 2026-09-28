import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

/** Gera um diretório de assets debug contendo um APK de plugin independente. */
abstract class StagePluginApkTask : DefaultTask() {
    /** APK produzido pelo módulo do plugin. */
    @get:InputFile
    abstract val pluginApk: RegularFileProperty

    /** Diretório conectado à variante debug pela Variant API do AGP. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val assetName: org.gradle.api.provider.Property<String>

    /** Copia o APK para o caminho estável esperado pelo bootstrap do host. */
    @TaskAction
    fun stage() {
        val target = outputDirectory.file("plugins/${assetName.get()}").get().asFile
        check(target.parentFile.mkdirs() || target.parentFile.isDirectory)
        pluginApk.get().asFile.copyTo(target, overwrite = true)
    }
}

val stageDebugLoginPlugin = tasks.register<StagePluginApkTask>("stageDebugLoginPlugin") {
    dependsOn(":plugin-login:assembleDebug")
    pluginApk.set(project(":plugin-login").layout.buildDirectory.file("outputs/apk/debug/plugin-login-debug.apk"))
    outputDirectory.set(layout.buildDirectory.dir("generated/loginPluginAssets/debug"))
    assetName.set("plugin-login.apk")
}

val stageDebugBusinessPlugin = tasks.register<StagePluginApkTask>("stageDebugBusinessPlugin") {
    dependsOn(":plugin-negocio:assembleDebug")
    pluginApk.set(project(":plugin-negocio").layout.buildDirectory.file("outputs/apk/debug/plugin-negocio-debug.apk"))
    outputDirectory.set(layout.buildDirectory.dir("generated/businessPluginAssets/debug"))
    assetName.set("business/plugin-negocio.apk")
}

val stageDebugSaqueCartaoPlugin = tasks.register<StagePluginApkTask>("stageDebugSaqueCartaoPlugin") {
    dependsOn(":plugin-saque-cartao:assembleDebug")
    pluginApk.set(project(":plugin-saque-cartao").layout.buildDirectory.file("outputs/apk/debug/plugin-saque-cartao-debug.apk"))
    outputDirectory.set(layout.buildDirectory.dir("generated/saqueCartaoPluginAssets/debug"))
    assetName.set("business/plugin-saque-cartao.apk")
}

android {
    namespace = "br.com.romulopenha.sistemaprototipoandroid"
    compileSdk = 37

    defaultConfig {
        applicationId = "br.com.romulopenha.sistemaprototipoandroid"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            stageDebugLoginPlugin,
            StagePluginApkTask::outputDirectory,
        )
        variant.sources.assets?.addGeneratedSourceDirectory(
            stageDebugBusinessPlugin,
            StagePluginApkTask::outputDirectory,
        )
        variant.sources.assets?.addGeneratedSourceDirectory(
            stageDebugSaqueCartaoPlugin,
            StagePluginApkTask::outputDirectory,
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared-api"))
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.test:core-ktx:1.6.1")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
