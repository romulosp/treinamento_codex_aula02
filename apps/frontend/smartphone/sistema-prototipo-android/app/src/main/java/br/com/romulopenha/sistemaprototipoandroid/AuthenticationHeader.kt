package br.com.romulopenha.sistemaprototipoandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.AuthenticatedProfile

private val HeaderBlue = Color(0xFF005CA9)
private val HeaderDivider = Color(0xFFE1E8EE)

/** Estado fechado que determina a variante visual do cabeçalho global. */
@Immutable
internal sealed interface AuthenticationHeaderState {
    /** Variante exibida quando não existe sessão válida. */
    data object LoggedOut : AuthenticationHeaderState

    /** Variante exibida com o perfil sanitizado da sessão atual. */
    data class LoggedIn(val profile: AuthenticatedProfile) : AuthenticationHeaderState
}

/** Mantém o cabeçalho global acima de qualquer conteúdo hospedado pelo host. */
@Composable
internal fun CoreShell(
    headerState: AuthenticationHeaderState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        AuthenticationHeader(headerState)
        Column(modifier = Modifier.fillMaxWidth().weight(1f), content = content)
    }
}

/** Renderiza o cabeçalho global sem estado próprio ou efeitos colaterais. */
@Composable
internal fun AuthenticationHeader(
    state: AuthenticationHeaderState,
    modifier: Modifier = Modifier,
) {
    val loggedIn = state as? AuthenticationHeaderState.LoggedIn
    val profileDescription = loggedIn?.profile?.let {
        "${it.displayRole()}: ${it.username} - ${it.displayName}. TERMINAL"
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(if (loggedIn == null) 64.dp else 120.dp)
            .background(Color.White)
            .border(1.dp, HeaderDivider)
            .then(
                if (profileDescription == null) Modifier else Modifier.semantics {
                    contentDescription = profileDescription
                },
            ),
    ) {
        Text(
            "Buy More",
            modifier = Modifier.align(Alignment.TopStart).padding(start = 26.dp, top = 28.dp),
            color = HeaderBlue,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            "POS - COMPRAS",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp),
            color = Color(0xFF333333),
            fontSize = 14.sp,
        )
        Text(
            "v1.0.0.0",
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 26.dp, top = 32.dp),
            color = Color(0xFF666666),
            fontSize = 14.sp,
        )
        if (loggedIn != null) {
            val wideLayout = maxWidth >= 1_000.dp
            Column(
                modifier = Modifier
                    .align(if (wideLayout) Alignment.TopEnd else Alignment.BottomCenter)
                    .padding(
                        top = if (wideLayout) 22.dp else 0.dp,
                        end = if (wideLayout) 150.dp else 0.dp,
                        bottom = if (wideLayout) 0.dp else 8.dp,
                    )
                    .widthIn(max = 380.dp)
                    .then(if (wideLayout) Modifier.width(380.dp) else Modifier.fillMaxWidth(0.58f))
                    .height(76.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0xFFA0B0C0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    "${loggedIn.profile.displayRole()}: ${loggedIn.profile.username} - ${loggedIn.profile.displayName}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                )
                Text("TERMINAL", fontSize = 12.sp)
            }
        }
    }
}

private fun AuthenticatedProfile.displayRole(): String =
    if (roleLabel == "PROPRIETARIO") "PROPRIETÁRIO" else roleLabel
