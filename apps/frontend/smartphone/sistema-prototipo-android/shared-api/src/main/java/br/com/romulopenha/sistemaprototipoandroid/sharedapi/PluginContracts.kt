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
    /** Indica sessão SSO estabelecida sem transportar senha ou token. */
    data class SessionStateChanged(val sessionId: String, val expiresAtEpochMillis: Long) : PluginEvent
}

/** Item declarativo que um plugin de negócio oferece ao menu do host. */
data class BusinessMenuItem(
    val id: String,
    val titulo: String,
    val rota: String,
    val ordem: Int,
)

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

/**
 * Especialização do plugin de autenticação que encerra uma sessão opaca.
 *
 * O host invoca [logout] fora da main thread. A implementação não recebe nem
 * devolve tokens do provedor.
 */
interface IAuthenticationPluginApp : IPluginApp {
    /** Solicita logout remoto e devolve se a API respondeu com sucesso. */
    fun logout(sessionId: String): Boolean
}

/** Plugin de negócio elegível para compor a tela inicial do host. */
interface IPluginNegocioApp : IPluginApp {
    /** Itens imutáveis publicados pelo plugin depois da validação do APK. */
    val businessMenuItems: List<BusinessMenuItem>
}
