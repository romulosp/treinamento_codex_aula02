package br.com.romulopenha.sistemaprototipoandroid

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.BusinessMenuItem

/**
 * Apresenta o menu autenticado sem possuir sessão, navegação ou dados de negócio.
 * Somente folhas emitem [onItemSelected]; a saída preserva o bloqueio durante logout.
 * Cabeçalho pertence ao CoreShell, conteúdo rola e rodapé/saída permanecem fixos.
 */
@Composable
internal fun BusinessMenuScreen(
    items: List<BusinessMenuItem>,
    fatalMessage: String?,
    logoutInProgress: Boolean,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit,
    onFatalAcknowledged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionAllowed = rememberMenuMotionAllowed()
    BoxWithConstraints(modifier.fillMaxSize().background(Brush.verticalGradient(
        listOf(MenuDesignTokens.backgroundStart, MenuDesignTokens.backgroundEnd),
    ))) {
        val sidePadding = when {
            maxWidth >= 1200.dp -> 80.dp
            maxWidth >= 1024.dp -> 48.dp
            else -> 24.dp
        }
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(start = sidePadding, end = sidePadding, top = 60.dp, bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "SELECIONE A FUNCIONALIDADE DESEJADA",
                    color = MenuDesignTokens.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(32.dp))
                if (items.isEmpty()) {
                    Text("Nenhum plugin de negócio carregado.", color = MenuDesignTokens.text)
                } else {
                    BoxWithConstraints(Modifier.widthIn(max = 1200.dp).fillMaxWidth()) {
                        val columns = ((maxWidth + MenuDesignTokens.gap) /
                            (MenuDesignTokens.cardWidth + MenuDesignTokens.gap)).toInt().coerceAtLeast(1)
                        val cardWidth = minOf(MenuDesignTokens.cardWidth, maxWidth)
                        Column(verticalArrangement = Arrangement.spacedBy(MenuDesignTokens.gap)) {
                            BusinessMenuEntries(items, onItemSelected, columns, cardWidth, motionAllowed)
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(MenuDesignTokens.footerHeight)
                .shadow(2.dp, ambientColor = Color(0x2E0F2A40), spotColor = Color(0x2E0F2A40))
                .background(MenuDesignTokens.surface).drawBehind {
                    drawRect(MenuDesignTokens.divider, size = Size(size.width, 1.dp.toPx()))
                })
        }
        MenuActionButton(
            text = if (logoutInProgress) "Saindo..." else "SAIR",
            onClick = onLogout,
            enabled = !logoutInProgress,
            danger = true,
            motionAllowed = motionAllowed,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = if (maxWidth < 1024.dp) 16.dp else 24.dp,
                    bottom = MenuDesignTokens.footerHeight + 24.dp)
                .widthIn(min = 140.dp).height(48.dp),
        )
    }
    fatalMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onFatalAcknowledged,
            title = { Text("Erro crítico no menu") },
            text = { Text(message) },
            confirmButton = { Button(onClick = onFatalAcknowledged) { Text("Fechar") } },
        )
    }
}

/** Mantém ordem recursiva e seções não acionáveis, distribuindo folhas consecutivas em linhas. */
@Composable
private fun BusinessMenuEntries(
    items: List<BusinessMenuItem>,
    onItemSelected: (String) -> Unit,
    columns: Int,
    cardWidth: Dp,
    motionAllowed: Boolean,
    level: Int = 0,
) {
    val ordered = items.sortedWith(compareBy(BusinessMenuItem::ordem, BusinessMenuItem::id))
    var index = 0
    while (index < ordered.size) {
        val item = ordered[index]
        if (item.filhos.isNotEmpty()) {
            Text(item.titulo,
                color = MenuDesignTokens.primary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(start = (level * 16).dp)
                    .semantics { heading() })
            BusinessMenuEntries(item.filhos, onItemSelected, columns, cardWidth, motionAllowed, level + 1)
            index++
        } else {
            val leaves = ordered.drop(index).takeWhile { it.filhos.isEmpty() }
            leaves.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(
                    MenuDesignTokens.gap, Alignment.CenterHorizontally,
                )) {
                    row.forEach { leaf ->
                        key(leaf.id) {
                            MenuActionButton(leaf.titulo, { onItemSelected(leaf.id) },
                                motionAllowed = motionAllowed,
                                modifier = Modifier.width(cardWidth).height(MenuDesignTokens.cardHeight))
                        }
                    }
                }
            }
            index += leaves.size
        }
    }
}

/**
 * Botão semântico nativo com foco externo, cores prioritárias de disabled/press/hover
 * e movimento decorativo condicionado à preferência do sistema recebida.
 * [interactionSource] permite verificar estados visuais sem mudar regras de negócio.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MenuActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
    motionAllowed: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val targetColor = when {
        !enabled -> MenuDesignTokens.disabledBackground
        pressed -> if (danger) MenuDesignTokens.dangerPressed else MenuDesignTokens.primaryPressed
        hovered -> if (danger) MenuDesignTokens.dangerHover else MenuDesignTokens.primaryHover
        else -> if (danger) MenuDesignTokens.danger else MenuDesignTokens.primary
    }
    val color by animateColorAsState(targetColor,
        animationSpec = if (motionAllowed && enabled) tween(150) else snap(), label = "menuActionColor")
    val elevation by animateDpAsState(if (!enabled) 0.dp else if (hovered) 4.dp else 2.dp,
        animationSpec = if (motionAllowed && enabled) tween(150) else snap(), label = "menuActionElevation")
    val scale by animateFloatAsState(
        if (enabled && pressed && motionAllowed) 0.98f else 1f,
        animationSpec = if (motionAllowed && enabled) tween(100) else snap(), label = "menuPressScale",
    )
    val radius = if (danger) 24.dp else 8.dp
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
    Button(
        onClick = onClick, enabled = enabled, interactionSource = interactionSource,
        shape = RoundedCornerShape(radius),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White,
            disabledContainerColor = MenuDesignTokens.disabledBackground,
            disabledContentColor = MenuDesignTokens.disabledText),
        contentPadding = PaddingValues(horizontal = 24.dp),
        elevation = null,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation, RoundedCornerShape(radius), clip = false,
                ambientColor = if (danger) Color(0x73005CA9) else Color(0x3814324F),
                spotColor = if (danger) Color(0x73005CA9) else Color(0x3814324F)).drawBehind {
            if (enabled && focused) {
                val outset = 3.5.dp.toPx()
                drawRoundRect(MenuDesignTokens.focus, topLeft = Offset(-outset, -outset),
                    size = Size(size.width + outset * 2, size.height + outset * 2),
                    cornerRadius = CornerRadius(radius.toPx() + outset), style = Stroke(3.dp.toPx()))
            }
        },
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    }
}

/** Observa somente a escala de animadores enquanto o menu está composto; nunca altera a preferência. */
@Composable
private fun rememberMenuMotionAllowed(): Boolean {
    val resolver = LocalContext.current.contentResolver
    fun readAllowed() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    var allowed by remember(resolver) { mutableStateOf(readAllowed()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { allowed = readAllowed() }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        allowed = readAllowed()
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return allowed
}
