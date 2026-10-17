package br.com.romulopenha.sistemaprototipoandroid.platform

import android.content.Context
import java.io.File
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * Entrega o APK de bootstrap da variante debug ao inbox do microkernel.
 *
 * A classe apenas move o artefato para a fronteira não confiável. Assinatura,
 * manifesto, integridade e compatibilidade continuam sob responsabilidade de
 * [LoginPluginLoader.stageAndVerify]. Quando o asset não existe, como em
 * `release`, a operação é um no-op.
 */
internal object BundledLoginPluginInstaller {
    private const val AssetDirectory = "plugins"
    private const val AssetName = "plugin-login.apk"

    /**
     * Copia o asset opcional para [stagingRoot] e retorna o candidato entregue.
     *
     * O arquivo temporário não possui extensão `.apk`; somente após a escrita
     * completa ele é renomeado no mesmo diretório. Um candidato com o mesmo
     * digest não é substituído.
     */
    fun installIfPresent(context: Context, stagingRoot: File): File? {
        val assetNames = context.assets.list(AssetDirectory).orEmpty()
        if (AssetName !in assetNames) return null
        return context.assets.open("$AssetDirectory/$AssetName").use { input ->
            install(input, stagingRoot)
        }
    }

    /** Instala um fluxo completo de APK com nome derivado do SHA-256. */
    internal fun install(input: InputStream, stagingRoot: File): File {
        check(stagingRoot.mkdirs() || stagingRoot.isDirectory) {
            "Não foi possível preparar a pasta dinâmica"
        }
        val temporary = File.createTempFile("plugin-login-", ".upload", stagingRoot)
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            DigestInputStream(input, digest).use { source ->
                temporary.outputStream().buffered().use(source::copyTo)
            }
            val target = File(stagingRoot, "plugin-login-${digest.digest().toHex()}.apk")
            if (target.exists()) {
                check(target.isFile) { "Destino do plugin não é arquivo" }
                check(temporary.delete()) { "Não foi possível remover entrega duplicada" }
            } else {
                check(temporary.renameTo(target)) { "Não foi possível publicar o plugin no inbox" }
            }
            target
        } catch (error: Exception) {
            temporary.delete()
            throw error
        }
    }

    /**
     * Coloca o candidato incorporado antes dos demais APKs encontrados no inbox.
     *
     * A ordenação permite validar a revisão do build debug atual antes de uma
     * recuperação privada anterior e elimina a mesma entrada da lista restante.
     */
    internal fun prioritize(
        bundledCandidate: File?,
        inboxCandidates: Collection<File>,
    ): List<File> {
        val bundledPath = bundledCandidate?.absoluteFile
        val remaining = inboxCandidates
            .asSequence()
            .filterNot { candidate -> candidate.absoluteFile == bundledPath }
            .sortedBy(File::lastModified)
            .toList()
        return listOfNotNull(bundledCandidate) + remaining
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }
}
