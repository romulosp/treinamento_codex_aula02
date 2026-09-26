package br.com.romulopenha.sistemaprototipoandroid.sharedapi

import android.content.Context
import android.view.View

/** Versão do contrato binário que o host compartilha com plugins internos. */
data class SharedApiVersion(val major: Int, val minor: Int, val patch: Int) {
    init {
        require(major >= 0 && minor >= 0 && patch >= 0) { "A versão da API não pode ser negativa." }
    }
}

/** Versão publicada pelo AAR e usada para identificar eventos e rotas. */
object SharedApi {
    val version = SharedApiVersion(major = 1, minor = 1, patch = 0)
}

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
    data class SessionStateChanged(
        val pluginId: String,
        val contractVersion: SharedApiVersion,
        val sessionId: String,
        val expiresAtEpochMillis: Long,
    ) : PluginEvent
}

/** Rota pertencente a um plugin, identificada sem transportar argumentos sensíveis. */
data class PluginRoute(
    val pluginId: String,
    val path: String,
    val contractVersion: SharedApiVersion,
) {
    init {
        require(pluginId.isNotBlank()) { "pluginId é obrigatório." }
        require(path.isNotBlank() && !path.startsWith('/')) { "O caminho da rota é inválido." }
    }

    /** URI canônica usada pelo host para encaminhar a rota declarada. */
    fun asUri(): String = "plugin://$pluginId/$path"
}

/** Item declarativo que um plugin de negócio oferece ao menu do host. */
data class BusinessMenuItem(
    val id: String,
    val titulo: String,
    val rota: String,
    val ordem: Int,
)

/** Fonte declarativa de itens de menu pertencentes a um plugin de negócio. */
interface IMenuProvider {
    /** Devolve somente descritores sem regras de negócio ou dados sensíveis. */
    fun menuItems(): List<BusinessMenuItem>
}

/** Faceta mínima do host para descoberta e consulta segura de estado do plugin. */
interface IPluginManager {
    /** Agenda uma descoberta assíncrona sem executar código no chamador. */
    fun requestDiscovery()

    /** Informa o estado observável mais recente de um plugin identificado. */
    fun stateOf(pluginId: String): PluginLifecycleState?
}

/** Estados públicos que podem ser observados sem expor detalhes do APK. */
enum class PluginLifecycleState {
    DISCOVERED,
    STAGED,
    VERIFIED,
    LOADED,
    ATTACHED,
    ACTIVE,
    DETACHED,
    REJECTED,
    ERROR,
    PENDING_RESTART,
}

/** Registro no qual o plugin declara a tela da capacidade inicial. */
interface IUIRegistry {
    /** Registra a fábrica que atenderá à capacidade `startup-auth`. */
    fun registerStartupAuth(factory: PluginScreenFactory)
}

/** Roteador de URIs de plugin que não aceita argumentos de credencial. */
interface IPluginRouter {
    /** Solicita ao host a navegação para uma rota previamente declarada. */
    fun navigate(route: PluginRoute)
}

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
interface IPluginNegocioApp : IPluginApp, IMenuProvider {
    /** Itens imutáveis publicados pelo plugin depois da validação do APK. */
    val businessMenuItems: List<BusinessMenuItem>

    /** Mantém compatibilidade com o descritor de menu inicialmente publicado. */
    override fun menuItems(): List<BusinessMenuItem> = businessMenuItems

    /** Cria a tela neutra de demonstração sem receber credenciais ou tokens. */
    fun createBusinessScreen(context: Context): View
}
