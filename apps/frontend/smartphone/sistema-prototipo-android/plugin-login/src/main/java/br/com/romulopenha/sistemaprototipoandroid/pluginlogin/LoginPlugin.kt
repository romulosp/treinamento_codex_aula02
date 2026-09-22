package br.com.romulopenha.sistemaprototipoandroid.pluginlogin

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginApp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IPluginRouter
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.IUIRegistry
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginEvent
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginHostContext
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginManifest
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.PluginScreenFactory

private val TerminalBlue = Color(0xFF075792)
private val TerminalDeepBlue = Color(0xFF064C83)
private val TerminalKeySurface = Color(0xFFF5F5F4)
private val TerminalHeaderSurface = Color(0xFFF2F5F7)
private val TerminalGreen = Color(0xFF1EB15A)

/**
 * Ponto de entrada do plugin interno de identificação.
 *
 * O plugin registra exclusivamente a capacidade `startup-auth`; dados de senha
 * permanecem dentro da sua UI e nunca são publicados para o host.
 */
class PluginLoginApp : IPluginApp {
    override val manifest = PluginManifest(
        pluginId = "br.com.romulopenha.sistemaprototipoandroid.login",
        displayName = "Login",
        pluginVersion = "1.0.1",
        requiredSharedApiMajor = 1,
        requiredSharedApiMinor = 0,
        entryClass = PluginLoginApp::class.java.name,
        capabilities = setOf("startup-auth"),
    )

    override fun onLoad(host: PluginHostContext) = Unit

    override fun onAttach(registry: IUIRegistry, router: IPluginRouter) {
        registry.registerStartupAuth(LoginScreenFactory)
    }

    override fun onActivate() = Unit

    override fun onDetach() = Unit
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

@Immutable
/** Estado imutável do terminal, mantido somente em memória pelo plugin. */
internal data class LoginUiState(
    val user: String = "",
    val password: String = "",
    val target: LoginInputTarget = LoginInputTarget.USER,
    val uppercase: Boolean = true,
    val editRevision: Int = 0,
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
        LoginEvent.Confirm -> if (state.user.startsWith('L') && state.password.isNotEmpty()) {
            state.copy(message = "AUTENTICAÇÃO LOCAL CONFIRMADA.")
        } else {
            state.copy(message = "INFORME USUÁRIO INICIADO EM L E SENHA PARA CONTINUAR.")
        }
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
 * Mantém o estado de apresentação e emite sessão somente após validação local.
 *
 * A classe é pública porque [androidx.lifecycle.ViewModelProvider] a instancia
 * por reflexão; estado e eventos permanecem internos ao APK do plugin.
 */
class LoginViewModel : ViewModel() {
    internal var state by mutableStateOf(LoginUiState())
        private set

    internal fun onEvent(event: LoginEvent) {
        state = LoginReducer.reduce(state, event)
    }
}

@Composable
private fun LoginRoute(onSessionEstablished: (PluginEvent.SessionStateChanged) -> Unit) {
    val viewModel: LoginViewModel = viewModel()
    val state = viewModel.state
    LoginScreen(state = state, onEvent = viewModel::onEvent, onSessionEstablished = onSessionEstablished)
}

/** Renderiza a tela de autenticação e restaura foco no fim do campo alterado. */
@Composable
internal fun LoginScreen(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    onSessionEstablished: (PluginEvent.SessionStateChanged) -> Unit,
    modifier: Modifier = Modifier,
) {
    val userFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    LaunchedEffect(state.target, state.editRevision) {
        if (state.target == LoginInputTarget.USER) userFocus.requestFocus() else passwordFocus.requestFocus()
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFFCAE5F6)) {
        Column(modifier = Modifier.fillMaxSize().testTag("login-screen")) {
            TerminalHeader()
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Brush.radialGradient(listOf(Color(0xFFF7FBFE), Color(0xFFD1E8F6), Color(0xFF9DCCE9)), radius = 1200f)),
            ) {
                val compact = maxWidth < 900.dp
                val keySize = if (compact) 43.dp else 50.dp
                val gap = if (compact) 4.dp else 6.dp
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = if (compact) 12.dp else 42.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("IDENTIFICAÇÃO", fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Credentials(state, onEvent, userFocus, passwordFocus)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                        AlphaKeyboard(state, onEvent, keySize, gap, Modifier.width(keySize * 14.8f))
                        Spacer(Modifier.width(if (compact) 12.dp else 28.dp))
                        NumericPad(onEvent, keySize, gap, Modifier.width(keySize * 3 + gap * 2))
                    }
                }
            }
            InstructionBar(state.message)
        }
    }
    if (state.message == "AUTENTICAÇÃO LOCAL CONFIRMADA.") {
        LaunchedEffect(state.message) {
            onSessionEstablished(PluginEvent.SessionStateChanged("local-login-session", Long.MAX_VALUE))
        }
    }
}

@Composable
private fun TerminalHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp).background(TerminalHeaderSurface).padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Loterias CAIXA", color = TerminalBlue, fontSize = 28.sp)
        Spacer(Modifier.weight(1f))
        Text("SISPL - Caixa Econômica Federal", fontSize = 17.sp)
        Spacer(Modifier.weight(1f))
        Column { listOf("Gateway", "DNS", "HTTP").forEach { StatusLine(it) } }
    }
}

@Composable
private fun StatusLine(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).background(TerminalGreen, RoundedCornerShape(50)))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 10.sp)
    }
}

@Composable
private fun Credentials(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    userFocus: FocusRequester,
    passwordFocus: FocusRequester,
) {
    Column(Modifier.widthIn(max = 430.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        modifier = modifier.height(52.dp).onFocusChanged { if (it.isFocused) onFocus() },
        singleLine = true,
        label = { Text(label) },
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = TerminalDeepBlue,
            unfocusedBorderColor = TerminalDeepBlue,
        ),
    )
}

@Composable
private fun AlphaKeyboard(state: LoginUiState, onEvent: (LoginEvent) -> Unit, keySize: Dp, gap: Dp, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        KeyRow(listOf("1", "2", "3", "4", "5", "6", "7", "8", "0", "-", "="), onEvent, keySize, gap) {
            ActionKey("LIMPAR", { onEvent(LoginEvent.Clear) }, keySize * 2)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                KeyRow(listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "P", "[", "]"), onEvent, keySize, gap, state.uppercase)
                KeyRow(listOf("A", "S", "D", "F", "G", "H", "J", "K", "L", "'", "\\"), onEvent, keySize, gap, state.uppercase)
            }
            ActionKey("ENTER", { onEvent(LoginEvent.Backspace) }, keySize * 1.7f, keySize * 2 + gap, Modifier.testTag("enter-button"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            ActionKey(if (state.uppercase) "FIXAR" else "SOLTAR", { onEvent(LoginEvent.ToggleCase) }, keySize * 1.8f)
            listOf("Z", "X", "C", "V", "B", "N", "M", ",", ".").forEach { Key(it.display(state.uppercase), { onEvent(LoginEvent.KeyPressed(it)) }, keySize) }
            ActionKey("CONFIRMAR", { onEvent(LoginEvent.Confirm) }, keySize * 2.8f)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            Spacer(Modifier.width(keySize * 2.3f))
            Key("ESPAÇO", { onEvent(LoginEvent.KeyPressed(" ")) }, keySize, width = keySize * 6.6f)
        }
    }
}

private fun String.display(uppercase: Boolean): String = if (uppercase) this else lowercase()

@Composable
private fun KeyRow(keys: List<String>, onEvent: (LoginEvent) -> Unit, size: Dp, gap: Dp, uppercase: Boolean = true, trailing: (@Composable () -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        keys.forEach { key -> Key(key.display(uppercase), { onEvent(LoginEvent.KeyPressed(key)) }, size) }
        trailing?.invoke()
    }
}

@Composable
private fun NumericPad(onEvent: (LoginEvent) -> Unit, size: Dp, gap: Dp, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) { row.forEach { Key(it, { onEvent(LoginEvent.KeyPressed(it)) }, size) } }
        }
        Key("0", { onEvent(LoginEvent.KeyPressed("0")) }, size, width = size * 2 + gap)
    }
}

@Composable
private fun Key(label: String, onClick: () -> Unit, size: Dp, modifier: Modifier = Modifier, width: Dp = size) {
    Surface(onClick = onClick, modifier = modifier.width(width).height(size).shadow(3.dp, RoundedCornerShape(8.dp)), shape = RoundedCornerShape(8.dp), color = TerminalKeySurface) {
        Box(contentAlignment = Alignment.Center) { Text(label, fontSize = if (label.length > 2) 13.sp else 20.sp) }
    }
}

@Composable
private fun ActionKey(label: String, onClick: () -> Unit, width: Dp, height: Dp = 50.dp, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.width(width).height(height).shadow(3.dp, RoundedCornerShape(8.dp)).semantics { role = Role.Button }, shape = RoundedCornerShape(8.dp), color = TerminalBlue, contentColor = Color.White) {
        Box(contentAlignment = Alignment.Center) { Text(label, fontSize = 13.sp) }
    }
}

@Composable
private fun InstructionBar(message: String) {
    Box(Modifier.fillMaxWidth().height(34.dp).background(TerminalHeaderSurface), contentAlignment = Alignment.Center) { Text(message, fontSize = 11.sp) }
}
