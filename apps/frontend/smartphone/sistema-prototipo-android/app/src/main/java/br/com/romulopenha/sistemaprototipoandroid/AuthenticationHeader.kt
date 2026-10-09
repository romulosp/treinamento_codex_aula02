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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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

/** Mantém o cabeçalho global; [menuAppearance] restringe os tokens POS à tela inicial autenticada. */
@Composable
internal fun CoreShell(
    headerState: AuthenticationHeaderState,
    modifier: Modifier = Modifier,
    menuAppearance: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        AuthenticationHeader(headerState, menuAppearance = menuAppearance)
        Column(modifier = Modifier.fillMaxWidth().weight(1f), content = content)
    }
}

/** Renderiza o cabeçalho global; variante POS opcional preserva login e telas hospedadas. */
@Composable
internal fun AuthenticationHeader(
    state: AuthenticationHeaderState,
    modifier: Modifier = Modifier,
    menuAppearance: Boolean = false,
) {
    val loggedIn = state as? AuthenticationHeaderState.LoggedIn
    if (menuAppearance && loggedIn != null) {
        MenuAuthenticationHeader(loggedIn.profile, modifier)
        return
    }
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

/** Organiza identidade e perfil sem fontes fictícias, usando largura útil em dp e sem anúncio duplicado. */
@Composable
private fun MenuAuthenticationHeader(profile: AuthenticatedProfile, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth().height(120.dp).background(MenuDesignTokens.surface)
        .drawBehind {
            drawRect(MenuDesignTokens.divider, topLeft = Offset(0f, size.height - 1.dp.toPx()),
                size = Size(size.width, 1.dp.toPx()))
        }) {
        val compact = maxWidth < 600.dp
        val side = if (maxWidth < 1024.dp) 16.dp else 24.dp
        val gap = if (maxWidth < 1024.dp) 8.dp else 24.dp
        val infoWidth = when {
            maxWidth >= 1200.dp -> 380.dp
            maxWidth >= 1024.dp -> 320.dp
            else -> minOf(320.dp, maxWidth * 0.45f)
        }
        if (maxWidth >= 1200.dp) {
            Text("POS - COMPRAS", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                color = MenuDesignTokens.text, modifier = Modifier.align(Alignment.Center))
        }
        Row(Modifier.fillMaxWidth().then(if (compact) Modifier.height(44.dp) else Modifier.fillMaxSize())
            .padding(horizontal = side), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(gap)) {
            Text("Buy More", color = MenuDesignTokens.primary, fontSize = 22.sp,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f))
            if (!compact) MenuProfileInfo(profile, Modifier.width(infoWidth))
            Text("v1.0.0.0", color = MenuDesignTokens.muted, fontSize = 14.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (compact) {
            MenuProfileInfo(profile, Modifier.align(Alignment.BottomEnd).padding(end = side)
                .width(infoWidth))
        }
    }
}

/** Exibe dados completos na semântica dos próprios textos, truncando somente sua apresentação visual. */
@Composable
private fun MenuProfileInfo(profile: AuthenticatedProfile, modifier: Modifier = Modifier) {
    Column(modifier.height(76.dp).background(MenuDesignTokens.infoBackground, RoundedCornerShape(4.dp))
        .drawBehind {
            val stroke = 1.5.dp.toPx()
            drawRoundRect(MenuDesignTokens.infoBorder,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))))
        }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center) {
        Text("${profile.displayRole()}: ${profile.username} - ${profile.displayName}",
            maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp,
            color = MenuDesignTokens.text)
        Text("TERMINAL", maxLines = 1, overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp, color = MenuDesignTokens.text)
    }
}

/** Preserva o rótulo visual contratado do proprietário sem alterar os dados de sessão. */
private fun AuthenticatedProfile.displayRole(): String =
    if (roleLabel == "PROPRIETARIO") "PROPRIETÁRIO" else roleLabel
