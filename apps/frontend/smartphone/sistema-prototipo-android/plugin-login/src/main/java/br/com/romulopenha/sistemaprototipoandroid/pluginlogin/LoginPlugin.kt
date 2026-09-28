package br.com.romulopenha.sistemaprototipoandroid.pluginlogin

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IAuthenticationPluginApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginEvent
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginScreenFactory
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.SharedApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private val TerminalBlue = Color(0xFF005CA9)
private val TerminalDeepBlue = Color(0xFF00509A)
private val TerminalKeySurface = Color.White
private val TerminalHeaderSurface = Color.White
private val TerminalDivider = Color(0xFFE1E8EE)
private val TerminalFooterText = Color(0xFF1F3B57)

/**
 * Ponto de entrada do plugin interno de identificação.
 *
 * O plugin registra exclusivamente a capacidade `startup-auth`; dados de senha
 * permanecem dentro da sua UI e nunca são publicados para o host.
 */
class PluginLoginApp : IAuthenticationPluginApp {
    override val manifest = PluginManifest(
        pluginId = "br.com.romulopenha.sistemaprototipoandroid.login",
        displayName = "Login",
        pluginVersion = "1.2.0",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 2,
        entryClass = PluginLoginApp::class.java.name,
        capabilities = setOf("startup-auth"),
    )

    override fun onLoad(host: PluginHostContext) = Unit

    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) {
        registry.registerStartupAuth(LoginScreenFactory)
    }

    override fun onActivate() = Unit

    override fun onDetach() = Unit

    override fun logout(sessionId: String): Boolean = BackendCredentialAuthenticator().logout(sessionId)
}

private object LoginScreenFactory : PluginScreenFactory {
    override fun create(
        context: Context,
        onSessionEstablished: (PluginEvent.SessionStateChanged) -> Unit,
    ): View = ComposeView(context).apply {
        setContent { LoginRoute(onSessionEstablished = onSessionEstablished) }
    }
}

/** Campo que receberá a próxima entrada no terminal de login. */
internal enum class LoginInputTarget { USER, PASSWORD }

/**
 * Estado imutável do terminal, mantido somente em memória pelo plugin.
 *
 * @property isAuthenticating impede confirmações concorrentes durante a rede
 * @property message instrução genérica que nunca contém credenciais
 */
@Immutable
internal data class LoginUiState(
    val user: String = "",
    val password: String = "",
    val target: LoginInputTarget = LoginInputTarget.USER,
    val uppercase: Boolean = true,
    val editRevision: Int = 0,
    val isAuthenticating: Boolean = false,
    val message: String = "INFORME USUÁRIO E SENHA E PRESSIONE CONFIRMAR PARA CONTINUAR.",
)

/** Intenções da interface processadas pelo redutor de login. */
internal sealed interface LoginEvent {
    data class UserChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data class TargetChanged(val value: LoginInputTarget) : LoginEvent
    data class KeyPressed(val value: String) : LoginEvent
    data object Backspace : LoginEvent
    data object Clear : LoginEvent
    data object ToggleCase : LoginEvent
    data object Confirm : LoginEvent
}

/**
 * Reduz as entradas do terminal sem persistir credenciais.
 *
 * A primeira posição do usuário é reservada para `L`; qualquer valor inicial
 * diferente é descartado para teclas físicas e virtuais.
 */
internal object LoginReducer {
    private const val MaxInput = 24

    /** Aplica [event] e devolve um novo estado com a revisão de edição atualizada. */
    internal fun reduce(state: LoginUiState, event: LoginEvent): LoginUiState = when (event) {
        is LoginEvent.UserChanged -> state.acceptDirectUserInput(event.value)
        is LoginEvent.PasswordChanged -> state.withPassword(event.value.take(MaxInput))
        is LoginEvent.TargetChanged -> state.copy(target = event.value, editRevision = state.editRevision + 1)
        is LoginEvent.KeyPressed -> state.editTarget(event.value)
        LoginEvent.Backspace -> state.editTarget("") { it.dropLast(1) }
        LoginEvent.Clear -> state.editTarget("") { "" }
        LoginEvent.ToggleCase -> state.copy(uppercase = !state.uppercase)
        LoginEvent.Confirm -> state
    }

    private fun LoginUiState.editTarget(
        value: String,
        transform: (String) -> String = { current -> current + normalizeKey(value) },
    ): LoginUiState = when (target) {
        LoginInputTarget.USER -> withUser(transform(user))
        LoginInputTarget.PASSWORD -> withPassword(transform(password))
    }

    private fun LoginUiState.normalizeKey(value: String): String =
        if (uppercase) value.uppercase() else value.lowercase()

    private fun LoginUiState.withUser(value: String): LoginUiState =
        copy(user = normalizeUser(value), editRevision = editRevision + 1)

    private fun LoginUiState.acceptDirectUserInput(value: String): LoginUiState = when {
        user.isEmpty() -> withUser(value)
        value.startsWith('L', ignoreCase = true) -> withUser(value)
        else -> this
    }

    private fun LoginUiState.withPassword(value: String): LoginUiState =
        copy(password = value.take(MaxInput), editRevision = editRevision + 1)

    private fun normalizeUser(value: String): String {
        if (value.isEmpty() || !value.first().equals('L', ignoreCase = true)) return ""
        return "L" + value.drop(1).take(MaxInput - 1)
    }
}

/**
 * Mantém o estado de apresentação e emite sessão somente após validação remota.
 *
 * A classe é pública porque [androidx.lifecycle.ViewModelProvider] a instancia
 * por reflexão; estado e eventos permanecem internos ao APK do plugin.
 */
class LoginViewModel internal constructor(
    private val authenticator: CredentialAuthenticator,
) : ViewModel() {
    /** Construtor usado pelo ViewModelProvider no APK carregado dinamicamente. */
    constructor() : this(BackendCredentialAuthenticator())

    private val mutableSessionEvents = MutableSharedFlow<PluginEvent.SessionStateChanged>(extraBufferCapacity = 1)

    /** Eventos de sessão sem replay, consumidos uma única vez pela rota. */
    internal val sessionEvents = mutableSessionEvents.asSharedFlow()

    internal var state by mutableStateOf(LoginUiState())
        private set

    /** Processa edição local ou inicia uma única tentativa de autenticação. */
    internal fun onEvent(event: LoginEvent) {
        if (event == LoginEvent.Confirm) {
            authenticate()
        } else {
            state = LoginReducer.reduce(state, event)
        }
    }

    /** Captura a credencial atual, executa rede no data layer e reduz o resultado. */
    private fun authenticate() {
        val credentials = state
        if (credentials.isAuthenticating) return
        if (!credentials.user.startsWith('L') || credentials.password.isEmpty()) {
            state = credentials.copy(message = "INFORME USUÁRIO INICIADO EM L E SENHA PARA CONTINUAR.")
            return
        }
        state = credentials.copy(isAuthenticating = true, message = "AUTENTICANDO...")
        viewModelScope.launch {
            when (val result = authenticator.authenticate(credentials.user, credentials.password)) {
                is AuthenticationResult.Success -> {
                    state = state.copy(
                        password = "",
                        isAuthenticating = false,
                        message = "AUTENTICAÇÃO CONFIRMADA.",
                    )
                    mutableSessionEvents.emit(
                        PluginEvent.SessionStateChanged(
                            pluginId = PluginLoginApp().manifest.pluginId,
                            contractVersion = SharedApi.version,
                            sessionId = result.sessionId,
                            expiresAtEpochMillis = result.expiresAtEpochMillis,
                            profile = result.profile,
                        ),
                    )
                }
                AuthenticationResult.InvalidCredentials -> {
                    state = state.copy(
                        isAuthenticating = false,
                        message = "USUÁRIO OU SENHA INVÁLIDOS.",
                    )
                }
                AuthenticationResult.Unavailable -> {
                    state = state.copy(
                        isAuthenticating = false,
                        message = "SERVIÇO DE AUTENTICAÇÃO INDISPONÍVEL.",
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginRoute(onSessionEstablished: (PluginEvent.SessionStateChanged) -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val state = viewModel.state
    val currentOnSessionEstablished by rememberUpdatedState(onSessionEstablished)
    LaunchedEffect(viewModel) {
        viewModel.sessionEvents.collect(currentOnSessionEstablished)
    }
    LoginScreen(state = state, onEvent = viewModel::onEvent)
}

/** Renderiza a tela de autenticação e restaura foco no fim do campo alterado. */
@Composable
internal fun LoginScreen(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val userFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    LaunchedEffect(state.target, state.editRevision) {
        if (state.target == LoginInputTarget.USER) userFocus.requestFocus() else passwordFocus.requestFocus()
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFFB3D4E6)) {
        Column(modifier = Modifier.fillMaxSize().testTag("login-screen")) {
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFFEBF4FA), Color(0xFFB3D4E6)))),
            ) {
                val compact = maxWidth < 1180.dp
                val keyWidth = if (compact) 47.dp else 62.dp
                val keyHeight = if (compact) 46.dp else 56.dp
                val gap = if (compact) 4.dp else 6.dp
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = if (compact) 12.dp else 24.dp, vertical = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "IDENTIFICAÇÃO",
                        color = TerminalBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Credentials(state, onEvent, userFocus, passwordFocus)
                    Spacer(Modifier.height(if (compact) 18.dp else 32.dp))
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                        AlphaKeyboard(state, onEvent, keyWidth, keyHeight, gap)
                        Spacer(Modifier.width(if (compact) 18.dp else 46.dp))
                        NumericPad(onEvent, keyWidth, keyHeight, gap)
                    }
                }
            }
            InstructionBar(state.message)
        }
    }
}

@Composable
private fun Credentials(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    userFocus: FocusRequester,
    passwordFocus: FocusRequester,
) {
    Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TerminalField("USUÁRIO", state.user, { onEvent(LoginEvent.UserChanged(it)) }, Modifier.fillMaxWidth().focusRequester(userFocus).testTag("user-field"), false) {
            onEvent(LoginEvent.TargetChanged(LoginInputTarget.USER))
        }
        TerminalField("SENHA", state.password, { onEvent(LoginEvent.PasswordChanged(it)) }, Modifier.fillMaxWidth().focusRequester(passwordFocus).testTag("password-field"), true) {
            onEvent(LoginEvent.TargetChanged(LoginInputTarget.PASSWORD))
        }
    }
}

@Composable
private fun TerminalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    password: Boolean,
    onFocus: () -> Unit,
) {
    OutlinedTextField(
        value = TextFieldValue(value, TextRange(value.length)),
        onValueChange = { onValueChange(it.text) },
        modifier = modifier.height(56.dp).onFocusChanged { if (it.isFocused) onFocus() },
        singleLine = true,
        label = { Text(label) },
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White.copy(alpha = 0.55f),
            focusedBorderColor = TerminalBlue,
            unfocusedBorderColor = TerminalBlue,
        ),
        shape = RoundedCornerShape(4.dp),
    )
}

@Composable
private fun AlphaKeyboard(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    keyWidth: Dp,
    keyHeight: Dp,
    gap: Dp,
) {
    val actionWidth = keyWidth * 2 + gap
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        KeyRow(listOf("1", "2", "3", "4", "5", "6", "7", "8", "0", "-", "="), onEvent, keyWidth, keyHeight, gap) {
            ActionKey("LIMPAR", { onEvent(LoginEvent.Clear) }, actionWidth, height = keyHeight)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                KeyRow(listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "P", "[", "]"), onEvent, keyWidth, keyHeight, gap, state.uppercase)
                KeyRow(listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "'", "\\"), onEvent, keyWidth, keyHeight, gap, state.uppercase)
            }
            ActionKey(
                label = if (state.isAuthenticating) "AGUARDE" else "CONFIRMAR",
                onClick = { onEvent(LoginEvent.Confirm) },
                width = actionWidth,
                height = keyHeight * 2 + gap,
                enabled = !state.isAuthenticating,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            ActionKey(if (state.uppercase) "FIXAR" else "SOLTAR", { onEvent(LoginEvent.ToggleCase) }, actionWidth, height = keyHeight)
            listOf("Z", "X", "C", "V", "B", "N", "M", ",", ".").forEach {
                Key(it.display(state.uppercase), { onEvent(LoginEvent.KeyPressed(it)) }, keyWidth, keyHeight)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(actionWidth + keyWidth + gap * 2))
            Key("ESPAÇO", { onEvent(LoginEvent.KeyPressed(" ")) }, keyWidth, keyHeight, width = keyWidth * 6 + gap * 5)
        }
    }
}

private fun String.display(uppercase: Boolean): String = if (uppercase) this else lowercase()

@Composable
private fun KeyRow(
    keys: List<String>,
    onEvent: (LoginEvent) -> Unit,
    width: Dp,
    height: Dp,
    gap: Dp,
    uppercase: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        keys.forEach { key -> Key(key.display(uppercase), { onEvent(LoginEvent.KeyPressed(key)) }, width, height) }
        trailing?.invoke()
    }
}

@Composable
private fun NumericPad(onEvent: (LoginEvent) -> Unit, width: Dp, height: Dp, gap: Dp) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { Key(it, { onEvent(LoginEvent.KeyPressed(it)) }, width, height) }
            }
        }
        Key("0", { onEvent(LoginEvent.KeyPressed("0")) }, width, height, width = width * 2 + gap)
    }
}

@Composable
private fun Key(
    label: String,
    onClick: () -> Unit,
    keyWidth: Dp,
    keyHeight: Dp,
    modifier: Modifier = Modifier,
    width: Dp = keyWidth,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(width).height(keyHeight).shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = TerminalKeySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalDivider),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = if (label.length > 2) 13.sp else 17.sp, fontWeight = if (label.length > 2) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun ActionKey(
    label: String,
    onClick: () -> Unit,
    width: Dp,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    enabled: Boolean = true,
) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.width(width).height(height).shadow(3.dp, RoundedCornerShape(8.dp)).semantics { role = Role.Button }, shape = RoundedCornerShape(8.dp), color = TerminalBlue, contentColor = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, TerminalDeepBlue)) {
        Box(contentAlignment = Alignment.Center) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp) }
    }
}

@Composable
private fun InstructionBar(message: String) {
    val displayMessage = if (message == "INFORME USUÁRIO E SENHA E PRESSIONE CONFIRMAR PARA CONTINUAR.") {
        "Digite seu usuário e senha para continuar"
    } else {
        message
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .shadow(3.dp, RoundedCornerShape(0.dp))
            .background(TerminalHeaderSurface)
            .border(width = 1.dp, color = TerminalDivider),
        contentAlignment = Alignment.Center,
    ) {
        Text(displayMessage, color = TerminalFooterText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp)
    }
}
