package br.com.romulopenha.sistemaprototipoandroid.sharedapi

import android.content.Context
import android.view.View

/** Versão do contrato binário que o host compartilha com plugins internos. */
data class SharedApiVersion(val major: Int, val minor: Int, val patch: Int)

/** Metadados validados antes de instanciar o ponto de entrada de um plugin. */
data class PluginManifest(
    val pluginId: String,
    val displayName: String,
    val pluginVersion: String,
    val requiredSharedApiMajor: Int,
    val requiredSharedApiMinor: Int,
    val entryClass: String,
    val capabilities: Set<String>,
)

/** Contexto mínimo entregue pelo host ao ciclo de vida do plugin. */
interface PluginHostContext {
    /** Publica um evento de plataforma sem dados de credencial. */
    fun publish(event: PluginEvent)
}

/** Evento seguro compartilhado entre plugin e host. */
sealed interface PluginEvent {
    /** Indica sessão local estabelecida sem transportar senha ou token. */
    data class SessionStateChanged(val sessionId: String, val expiresAtEpochMillis: Long) : PluginEvent
}

/** Registro no qual o plugin declara a tela da capacidade inicial. */
interface IUIRegistry {
    /** Registra a fábrica que atenderá à capacidade `startup-auth`. */
    fun registerStartupAuth(factory: PluginScreenFactory)
}

/** Roteador reservado para rotas futuras sem acoplar regras de negócio ao host. */
interface IPluginRouter

/** Cria a View raiz controlada pelo plugin para o contêiner do host. */
fun interface PluginScreenFactory {
    /** Cria uma nova View para o contexto visual recebido e notifica sessão local. */
    fun create(context: Context, onSessionEstablished: (PluginEvent.SessionStateChanged) -> Unit): View
}

/** Ponto de entrada de um APK de plugin carregado pelo host. */
interface IPluginApp {
    /** Metadados de identidade do plugin. */
    val manifest: PluginManifest

    /** Inicializa referências permitidas do host, sem apresentar UI. */
    fun onLoad(host: PluginHostContext)

    /** Registra capacidades e rotas do plugin. */
    fun onAttach(registry: IUIRegistry, router: IPluginRouter)

    /** Marca o plugin como ativo após a UI ser montada. */
    fun onActivate()

    /** Libera referências cooperativas quando o host encerra o plugin. */
    fun onDetach()
}
