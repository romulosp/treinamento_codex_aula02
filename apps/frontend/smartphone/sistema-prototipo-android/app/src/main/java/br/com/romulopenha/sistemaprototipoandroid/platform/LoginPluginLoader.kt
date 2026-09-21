package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginEvent
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginScreenFactory
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.SharedApiVersion
import dalvik.system.DexClassLoader
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile

/** Estados auditáveis do pipeline e do ciclo de vida do plugin. */
internal enum class PluginRuntimeStatus {
    WAITING,
    DISCOVERED,
    STAGED,
    VERIFIED,
    LOADED,
    ATTACHED,
    ACTIVE,
    REJECTED,
    ERROR,
    PENDING_RESTART,
}

/** Registro sem credenciais de uma transição do pipeline. */
internal data class PluginTransition(
    val status: PluginRuntimeStatus,
    val pluginId: String? = null,
    val version: String? = null,
    val digestSha256: String? = null,
    val reason: String? = null,
)

/** Metadados extraídos do manifesto interno e validados antes da carga. */
internal data class PluginDescriptor(
    val pluginId: String,
    val pluginVersion: String,
    val requiredMajor: Int,
    val requiredMinor: Int,
    val entryClass: String,
    val declaredPackageName: String,
    val capabilities: Set<String>,
)

/** APK promovido ao repositório privado e apto a chegar ao classloader. */
internal data class VerifiedLoginPlugin(
    val apk: File,
    val descriptor: PluginDescriptor,
    val digestSha256: String,
)

/** Instância ativa e sua fábrica de tela, mantidas pelo manager do microkernel. */
internal data class LoadedLoginPlugin(
    val screenFactory: PluginScreenFactory,
    val digestSha256: String,
    val pluginVersion: String,
    private val app: IPluginApp,
) {
    /** Solicita limpeza cooperativa; não promete descarregar classes do processo. */
    fun detach() = app.onDetach()
}

/** Valida, promove e instancia exclusivamente o plugin interno de autenticação. */
internal object LoginPluginLoader {
    private const val PluginId = "br.com.romulopenha.sistemaprototipoandroid.login"
    private const val StartupAuthCapability = "startup-auth"
    private const val MaxPluginBytes = 64L * 1024L * 1024L
    private val HostApi = SharedApiVersion(major = 1, minor = 0, patch = 0)

    /**
     * Move semanticamente um candidato para quarentena, valida-o e o promove.
     *
     * O staging nunca chega ao classloader. Em falha, a quarentena é excluída,
     * `REJECTED` é emitido e a exceção volta ao manager.
     */
    fun stageAndVerify(
        context: Context,
        stagingRoot: File,
        candidate: File,
        onTransition: (PluginTransition) -> Unit,
    ): VerifiedLoginPlugin {
        onTransition(PluginTransition(PluginRuntimeStatus.DISCOVERED))
        val canonicalRoot = stagingRoot.canonicalFile
        val canonicalCandidate = candidate.canonicalFile
        require(canonicalCandidate.parentFile == canonicalRoot) { "Candidato fora da pasta observada" }
        require(canonicalCandidate.extension.equals("apk", ignoreCase = true)) { "Extensão inválida" }
        require(canonicalCandidate.length() in 1..MaxPluginBytes) { "Tamanho de APK inválido" }

        val quarantineDirectory = File(context.filesDir, "plugins/quarantine").apply { mkdirs() }
        val quarantined = File.createTempFile("plugin-", ".apk", quarantineDirectory)
        return try {
            canonicalCandidate.inputStream().use { input ->
                quarantined.outputStream().use(input::copyTo)
            }
            check(canonicalCandidate.delete()) { "Não foi possível concluir a movimentação do staging" }
            onTransition(PluginTransition(PluginRuntimeStatus.STAGED))

            val digest = sha256(quarantined)
            val descriptor = readDescriptor(quarantined)
            validateDescriptor(descriptor)
            validateArchive(context, quarantined, descriptor)
            val verified = promote(context, quarantined, descriptor, digest)
            onTransition(verified.transition(PluginRuntimeStatus.VERIFIED))
            verified
        } catch (error: Exception) {
            quarantined.delete()
            onTransition(
                PluginTransition(
                    status = PluginRuntimeStatus.REJECTED,
                    reason = error.javaClass.simpleName,
                ),
            )
            throw error
        }
    }

    /**
     * Lista revisões privadas e remove artefatos que perderam invariantes de
     * integridade, assinatura, manifesto ou imutabilidade.
     */
    fun findVerified(
        context: Context,
        onTransition: (PluginTransition) -> Unit,
    ): List<VerifiedLoginPlugin> {
        val repository = File(context.filesDir, "plugins/verified").apply { mkdirs() }
        return repository.listFiles { file -> file.isFile && file.extension == "apk" }
            .orEmpty()
            .sortedByDescending(File::lastModified)
            .mapNotNull { apk -> verifyRepositoryEntry(context, apk, onTransition) }
    }

    /**
     * Cria o classloader, instancia [IPluginApp] e executa o ciclo de vida.
     *
     * Em exceção, tenta [IPluginApp.onDetach], emite `ERROR` e propaga a falha
     * para o manager remover a UI e publicar indisponibilidade.
     */
    fun load(
        context: Context,
        verified: VerifiedLoginPlugin,
        onTransition: (PluginTransition) -> Unit,
    ): LoadedLoginPlugin {
        var app: IPluginApp? = null
        return try {
            val optimized = File(context.codeCacheDir, "plugins/${verified.digestSha256}").apply { mkdirs() }
            val classLoader = DexClassLoader(
                verified.apk.absolutePath,
                optimized.absolutePath,
                null,
                context.classLoader,
            )
            app = classLoader.loadClass(verified.descriptor.entryClass)
                .getDeclaredConstructor()
                .newInstance() as IPluginApp
            onTransition(verified.transition(PluginRuntimeStatus.LOADED))
            validateRuntimeManifest(app.manifest, verified.descriptor)

            val registry = StartupAuthRegistry()
            app.onLoad(HostContext)
            app.onAttach(registry, EmptyRouter)
            onTransition(verified.transition(PluginRuntimeStatus.ATTACHED))
            val loaded = LoadedLoginPlugin(
                screenFactory = requireNotNull(registry.factory),
                digestSha256 = verified.digestSha256,
                pluginVersion = verified.descriptor.pluginVersion,
                app = app,
            )
            app.onActivate()
            onTransition(verified.transition(PluginRuntimeStatus.ACTIVE))
            loaded
        } catch (error: Exception) {
            runCatching { app?.onDetach() }
            onTransition(
                verified.transition(
                    status = PluginRuntimeStatus.ERROR,
                    reason = error.javaClass.simpleName,
                ),
            )
            throw error
        }
    }

    /** Revalida uma entrada privada no boot e a exclui se qualquer invariante falhar. */
    private fun verifyRepositoryEntry(
        context: Context,
        apk: File,
        onTransition: (PluginTransition) -> Unit,
    ): VerifiedLoginPlugin? = try {
        check(!apk.canWrite()) { "APK verificado tornou-se gravável" }
        val digest = sha256(apk)
        check(apk.name == "$digest.apk") { "Digest do repositório divergente" }
        val descriptor = readDescriptor(apk)
        validateDescriptor(descriptor)
        validateArchive(context, apk, descriptor)
        VerifiedLoginPlugin(apk, descriptor, digest).also {
            onTransition(it.transition(PluginRuntimeStatus.VERIFIED))
        }
    } catch (error: Exception) {
        apk.delete()
        onTransition(
            PluginTransition(
                status = PluginRuntimeStatus.REJECTED,
                reason = error.javaClass.simpleName,
            ),
        )
        null
    }

    /** Promove uma quarentena validada para arquivo privado somente leitura. */
    private fun promote(
        context: Context,
        quarantined: File,
        descriptor: PluginDescriptor,
        digest: String,
    ): VerifiedLoginPlugin {
        val repository = File(context.filesDir, "plugins/verified").apply { mkdirs() }
        val target = File(repository, "$digest.apk")
        if (target.exists()) {
            check(!target.canWrite()) { "APK existente no repositório está gravável" }
            quarantined.delete()
        } else {
            check(quarantined.setReadOnly()) { "Não foi possível tornar o APK somente leitura" }
            check(quarantined.renameTo(target)) { "Não foi possível promover o APK verificado" }
            check(!target.canWrite()) { "APK promovido continua gravável" }
        }
        return VerifiedLoginPlugin(target, descriptor, digest)
    }

    /** Confere identidade, API, entry class e capacidade antes da inspeção do APK. */
    private fun validateDescriptor(descriptor: PluginDescriptor) {
        require(descriptor.pluginId == PluginId) { "Identidade de plugin incompatível" }
        require(descriptor.requiredMajor == HostApi.major) { "Major da API incompatível" }
        require(descriptor.requiredMinor <= HostApi.minor) { "Minor da API incompatível" }
        require(StartupAuthCapability in descriptor.capabilities) { "Capacidade startup-auth ausente" }
        require(descriptor.entryClass.startsWith("${descriptor.declaredPackageName}.")) {
            "Entry class fora do pacote declarado"
        }
    }

    /** Confere pacote e certificado do arquivo APK sem executar suas classes. */
    @Suppress("DEPRECATION")
    private fun validateArchive(context: Context, apk: File, descriptor: PluginDescriptor) {
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        val archive = requireNotNull(context.packageManager.getPackageArchiveInfo(apk.absolutePath, flags)) {
            "APK não reconhecido pelo PackageManager"
        }
        require(archive.packageName == descriptor.declaredPackageName) { "Pacote do APK divergente" }
        val host = context.packageManager.getPackageInfo(context.packageName, flags)
        val archiveCertificates = archive.certificateDigests()
        val hostCertificates = host.certificateDigests()
        require(archiveCertificates.isNotEmpty() && archiveCertificates == hostCertificates) {
            "Certificado do plugin não permitido"
        }
    }

    /** Normaliza o histórico/certificados atuais como conjunto SHA-256 comparável. */
    private fun PackageInfo.certificateDigests(): Set<String> {
        val details = requireNotNull(signingInfo) { "Informação de assinatura ausente" }
        val signatures = if (details.hasMultipleSigners()) {
            details.apkContentsSigners
        } else {
            details.signingCertificateHistory
        }
        return signatures.orEmpty().map { sha256(it.toByteArray()) }.toSet()
    }

    /** Lê somente os campos necessários do manifesto versionado dentro do APK. */
    private fun readDescriptor(apk: File): PluginDescriptor = ZipFile(apk).use { zip ->
        val entry = requireNotNull(zip.getEntry("assets/plugin-manifest.json")) {
            "Manifesto de plugin ausente"
        }
        val content = zip.getInputStream(entry).bufferedReader().use { it.readText() }
        PluginDescriptor(
            pluginId = content.string("pluginId"),
            pluginVersion = content.string("pluginVersion"),
            requiredMajor = content.number("requiredSharedApiMajor"),
            requiredMinor = content.number("requiredSharedApiMinor"),
            entryClass = content.string("entryClass"),
            declaredPackageName = content.string("declaredPackageName"),
            capabilities = content.array("capabilities"),
        )
    }

    /** Impede que a instância carregada declare identidade diferente do arquivo validado. */
    private fun validateRuntimeManifest(runtime: PluginManifest, descriptor: PluginDescriptor) {
        require(runtime.pluginId == descriptor.pluginId)
        require(runtime.pluginVersion == descriptor.pluginVersion)
        require(runtime.requiredSharedApiMajor == descriptor.requiredMajor)
        require(runtime.requiredSharedApiMinor == descriptor.requiredMinor)
        require(runtime.entryClass == descriptor.entryClass)
        require(runtime.capabilities == descriptor.capabilities)
    }

    private fun VerifiedLoginPlugin.transition(
        status: PluginRuntimeStatus,
        reason: String? = null,
    ) = PluginTransition(
        status = status,
        pluginId = descriptor.pluginId,
        version = descriptor.pluginVersion,
        digestSha256 = digestSha256,
        reason = reason,
    )

    /** Calcula integridade do APK sem carregar todo o arquivo em memória. */
    private fun sha256(file: File): String = file.inputStream().use { input ->
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
        digest.digest().toHex()
    }

    /** Calcula o pin de um certificado codificado. */
    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

    private fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }

    private fun String.string(name: String): String =
        Regex("\\\"$name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(this)?.groupValues?.get(1)
            ?: error("Manifesto sem $name")

    private fun String.number(name: String): Int =
        Regex("\\\"$name\\\"\\s*:\\s*(\\d+)").find(this)?.groupValues?.get(1)?.toInt()
            ?: error("Manifesto sem $name")

    private fun String.array(name: String): Set<String> =
        Regex("\\\"$name\\\"\\s*:\\s*\\[([^]]*)]").find(this)?.groupValues?.get(1)
            ?.split(',')
            ?.map { it.trim().trim('"') }
            ?.filter(String::isNotEmpty)
            ?.toSet()
            ?: emptySet()

    private object HostContext : PluginHostContext {
        override fun publish(event: PluginEvent) = Unit
    }

    private object EmptyRouter : IPluginRouter

    /** Aceita exatamente uma fábrica `startup-auth` durante o attach. */
    private class StartupAuthRegistry : IUIRegistry {
        var factory: PluginScreenFactory? = null
            private set

        override fun registerStartupAuth(factory: PluginScreenFactory) {
            check(this.factory == null) { "Capacidade startup-auth duplicada" }
            this.factory = factory
        }
    }
}
