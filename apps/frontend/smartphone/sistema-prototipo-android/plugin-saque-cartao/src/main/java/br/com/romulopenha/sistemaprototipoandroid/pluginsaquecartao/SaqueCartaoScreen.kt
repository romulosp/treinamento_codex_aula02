package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF003366)
private val Ink = Color(0xFF0F172A)
private val Muted = Color(0xFF64748B)
private val Border = Color(0xFFE2E8F0)
private val Pale = Color(0xFFF8FAFC)
private val Blue = Color(0xFF3B82F6)
private val Green = Color(0xFF078457)

/**
 * Conteúdo puro da preparação e cinco etapas, conforme SVGs Gemini. Estado e callbacks são
 * fornecidos pelo proprietário; transições e spinner nunca produzem comandos.
 * A etapa define a identidade da animação, preservando o snapshot de saída;
 * a troca combina deslocamento, desvanecimento e escala para ser percebida.
 */
@Composable
internal fun SaqueCartaoScreen(state: SaqueState, onEvent: (SaqueEvent) -> Unit, modifier: Modifier = Modifier) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Navy, onPrimary = Color.White,
        surface = Color.White, onSurface = Ink, secondary = Blue, outline = Border)) {
        BoxWithConstraints(modifier.fillMaxSize().background(Brush.linearGradient(
            listOf(Color(0xFFF4F7F9), Border)))) {
            val expanded = maxWidth >= 880.dp && LocalDensity.current.fontScale <= 1.3f
            val gutter = if (expanded) 64.dp else 20.dp
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = gutter, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Pagamento com cartão", Modifier.fillMaxWidth(), color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                AnimatedContent(targetState = state, contentKey = { it.step }, label = "etapa-pagamento",
                    transitionSpec = {
                        ((fadeIn(tween(320)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 8 }
                            + scaleIn(initialScale = 0.90f, animationSpec = tween(320, easing = FastOutSlowInEasing)))
                            togetherWith (fadeOut(tween(200)) + slideOutVertically(tween(240)) { -it / 10 }
                                + scaleOut(targetScale = 0.94f, animationSpec = tween(240))))
                            .using(SizeTransform(clip = false))
                    }) { target ->
                    if (target.step == 1) {
                        if (expanded) Row(Modifier.fillMaxWidth().widthIn(max = 1120.dp),
                            horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                            ValuesPanel(target, onEvent, Modifier.weight(1f))
                            SummaryPanel(target, onEvent, Modifier.weight(1f))
                        } else Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            ValuesPanel(target, onEvent, Modifier.fillMaxWidth())
                            SummaryPanel(target, onEvent, Modifier.fillMaxWidth())
                        }
                    } else StagePanel(target, onEvent, Modifier.widthIn(max = if (target.step == 3) 700.dp else 600.dp).fillMaxWidth())
                }
            }
        }
    }
}

/** Painel Gemini editável sem hardware, com compra monetária e parcelamento em combobox. */
@Composable
private fun ValuesPanel(state: SaqueState, onEvent: (SaqueEvent) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 5.dp) {
        Column(Modifier.padding(32.dp).heightIn(min = 450.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FieldCaption("MODALIDADE")
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF1F5F9)).padding(4.dp).selectableGroup()) {
                SaqueModalidade.entries.forEach { mode ->
                    val selected = mode == state.modalidade
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(6.dp))
                        .background(if (selected) Color.White else Color.Transparent)
                        .selectable(selected, enabled = !state.busy, role = Role.RadioButton,
                            onClick = { onEvent(SaqueEvent.Modalidade(mode)) }).heightIn(min = 48.dp).padding(12.dp),
                        contentAlignment = Alignment.Center) {
                        Text(if (mode == SaqueModalidade.CREDITO) "Crédito" else "Débito", color = if (selected) Ink else Muted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            FieldCaption("VALOR DA COMPRA")
            OutlinedTextField(formatPurchaseDigits(state.valueInput), { onEvent(SaqueEvent.Value(it)) }, Modifier.fillMaxWidth(),
                enabled = !state.busy, label = { Text("Valor da compra") }, prefix = { Text("R$ ") },
                singleLine = true, shape = RoundedCornerShape(8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            FieldCaption("PARCELAMENTO")
            InstallmentsPicker(state, onEvent)
            FieldCaption("TAXA APLICADA")
            Text(state.feeLabel.ifEmpty { "Não configurada" }, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }
    }
}

/**
 * Combobox de parcelas: crédito oferece 1x a 12x; débito permanece fixo em 1x
 * (à vista) e não abre lista. A seleção só é emitida por toque explícito.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstallmentsPicker(state: SaqueState, onEvent: (SaqueEvent) -> Unit) {
    val credit = state.modalidade == SaqueModalidade.CREDITO
    val open = credit && !state.busy
    val options = if (credit) (1..12).map(Int::toString) else listOf("1")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded && open, onExpandedChange = { expanded = open && it },
        modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(if (credit) state.installmentsInput else "1x (À vista)", {},
            Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            enabled = open, readOnly = true, singleLine = true, shape = RoundedCornerShape(8.dp),
            label = { Text(if (credit) "Parcelas (1 a 12)" else "Parcelas (débito)") },
            suffix = if (credit) ({ Text("x") }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && open) })
        ExposedDropdownMenu(expanded = expanded && open, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(if (option == "1") "1x (À vista)" else "${option}x") },
                    onClick = { onEvent(SaqueEvent.Installments(option)); expanded = false })
            }
        }
    }
}

/**
 * Painel de resumo com total não editável e avanço condicionado ao cálculo.
 *
 * AVANÇAR exige sessão preparada/carga EMV confirmada, resumo e ausência de operação em curso; o
 * grupo de ações entra com deslocamento, desvanecimento e escala percebíveis.
 */
@Composable
private fun SummaryPanel(state: SaqueState, onEvent: (SaqueEvent) -> Unit, modifier: Modifier = Modifier) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = Pale, border = androidx.compose.foundation.BorderStroke(1.dp, Border), shadowElevation = 4.dp) {
        Column(Modifier.padding(32.dp).heightIn(min = 450.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("Resumo da transação", color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SummaryLine("Valor da compra", money(state.summary?.baseCents))
                    SummaryLine("Taxa aplicada", money(state.summary?.feeCents))
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEBF4FF)) {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(24.dp)) {
                    if (maxWidth >= 390.dp && LocalDensity.current.fontScale <= 1.3f) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("TOTAL A PAGAR", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(money(state.summary?.totalCents), color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                        }
                    } else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TOTAL A PAGAR", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(money(state.summary?.totalCents), color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                    }
                }
            }
            Text(state.message, color = Muted, fontSize = 14.sp)
            FailureNote(state)
            AnimatedVisibility(visible = appeared,
                enter = fadeIn(tween(320)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 3 }
                    + scaleIn(initialScale = 0.92f, animationSpec = tween(320, easing = FastOutSlowInEasing)),
                label = "acoes-primeira-tela") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PrimaryAction("AVANÇAR", { onEvent(SaqueEvent.Begin) },
                        state.configReady && state.summary != null && !state.busy)
                    PrimaryAction("SAIR", { onEvent(SaqueEvent.Return) }, !state.busy, Color(0xFFB42318))
                }
            }
        }
    }
}

/**
 * Cartão da preparação e etapas físicas; resultado/instruções refletem estado real.
 *
 * A ação CANCELAR pertence somente às etapas transacionais 2..4, onde existe
 * operação confirmada no pinpad. A preparação (etapa 0) enquanto `busy` oferece
 * apenas instrução, progresso e spinner, sem CANCELAR, CANCELANDO ou SAIR; o
 * encerramento de sessão dessa fase é cleanup interno de lifecycle, e SAIR
 * aparece apenas na falha de preparação.
 */
@Composable
private fun StagePanel(state: SaqueState, onEvent: (SaqueEvent) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 5.dp) {
        Column(Modifier.padding(32.dp).heightIn(min = 430.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            when (state.step) {
                0 -> {
                    StageHeading(if (state.busy) "Aguarde, processando" else "Não foi possível preparar o terminal",
                        if (state.busy) "Preparando o pinpad para pagamento com cartão." else "Saia e tente novamente após verificar o terminal.")
                    WaitingState(state.message, state.busy)
                    FailureNote(state)
                }
                2 -> {
                    StageHeading("Confirmação no terminal", "Peça ao cliente que confira os valores e confirme no pinpad.")
                    Receipt(state.summary)
                    WaitingState(state.message, state.busy)
                }
                3 -> {
                    StageHeading("Inserir, passar ou aproximar o cartão", "Peça ao cliente que apresente o cartão no pinpad.")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf("CHIP", "TARJA", "APROXIMAÇÃO").forEachIndexed { index, title ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CardIllustration(index, Modifier.fillMaxWidth().aspectRatio(1.45f))
                                Text(title, color = if (index == 2) Blue else Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                    WaitingState(state.message, state.busy)
                }
                4 -> {
                    val explicitPin = state.message == "Peça ao cliente que digite a senha no pinpad."
                    StageHeading(if (explicitPin) "Digitação de senha" else "Processamento no terminal",
                        if (explicitPin) "A senha deve ser digitada somente no pinpad." else "Siga as instruções no pinpad; digite a senha se solicitada.")
                    TerminalIllustration(Modifier.widthIn(max = 360.dp).fillMaxWidth().height(150.dp))
                    WaitingState(state.message, state.busy)
                }
                5 -> {
                    val success = state.result in setOf(SaqueResultado.APPROVED, SaqueResultado.CAPTURED)
                    val color = if (success) Green else if (state.result == SaqueResultado.DENIED || state.result == SaqueResultado.ERROR)
                        Color(0xFFB42318) else Navy
                    Box(Modifier.size(88.dp).clip(CircleShape).background(color.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                        Text(if (success) "✓" else if (state.result == SaqueResultado.CANCELED) "×" else "!", color = color,
                            fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(state.result?.title ?: "Resultado indisponível", color = color, fontSize = 28.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(state.message, color = Muted, textAlign = TextAlign.Center)
                    FailureNote(state)
                    Receipt(state.summary)
                }
            }
            if (state.step in 2..4) PrimaryAction(if (state.cancelRequested) "CANCELANDO..." else "CANCELAR",
                { onEvent(SaqueEvent.Cancel) }, state.busy && !state.cancelRequested)
            if (state.step == 0 && !state.busy) PrimaryAction("SAIR", { onEvent(SaqueEvent.Return) }, true, Color(0xFFB42318))
            if (state.step == 5) PrimaryAction("VOLTAR AO MENU INICIAL", { onEvent(SaqueEvent.Return) }, !state.busy)
        }
    }
}

/** Título e instrução centralizados conforme cartões dos SVGs, sem estado simulado. */
@Composable
private fun StageHeading(title: String, subtitle: String) {
    Text(title, fontSize = 26.sp, color = Ink, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Text(subtitle, color = Muted, fontSize = 16.sp, textAlign = TextAlign.Center)
}

/** Resumo congelado; não exibe autorização, NSU ou campos fabricados. */
@Composable
private fun Receipt(summary: SaqueResumo?) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Pale,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SummaryLine("Valor total", money(summary?.totalCents))
            SummaryLine("Parcelas", summary?.installments?.let { "${it}x" } ?: "—")
            SummaryLine("Tarifa", money(summary?.feeCents))
        }
    }
}

/** Linha flexível com leitura textual completa, inclusive em fonte ampliada. */
@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, Modifier.weight(1f), color = Muted, fontSize = 16.sp)
        Text(value, Modifier.weight(1f), color = Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}

/** Spinner Material indeterminado usa a escala de movimento do sistema e texto real. */
@Composable
private fun WaitingState(message: String, waiting: Boolean) {
    if (waiting) CircularProgressIndicator(Modifier.size(40.dp), color = Blue, strokeWidth = 3.dp)
    Text(message, color = Blue, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
}

/** Ação sólida, textual e sem ícone com alvo de toque mínimo de 60 dp. */
@Composable
private fun PrimaryAction(text: String, onClick: () -> Unit, enabled: Boolean, containerColor: Color = Navy) {
    Button(onClick, Modifier.fillMaxWidth().heightIn(min = 60.dp), enabled = enabled, shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = Color.White)) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FieldCaption(text: String) { Text(text, color = Muted, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

/**
 * Código e fase sanitizados da última falha, no padrão do diagnóstico 075.
 * Exibe somente categorias da whitelist do core ou códigos locais de
 * provisionamento; nenhum texto livre do driver chega à tela.
 */
@Composable
private fun FailureNote(state: SaqueState) {
    val code = state.failureCode ?: return
    Text("Falha: $code · fase ${state.failurePhase ?: "binding"}", color = Color(0xFFB42318),
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
}

/** Placeholder indica ausência de cálculo; não transforma configuração ausente em zero. */
private fun money(cents: Long?): String = cents?.let { "R$ ${formatCents(it)}" } ?: "—"

/** Ilustração decorativa dos três modos; não declara capacidades nem seleciona leitura. */
@Composable
private fun CardIllustration(mode: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val rect = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx())
        val origin = Offset(2.dp.toPx(), 2.dp.toPx())
        drawRoundRect(if (mode == 2) Color(0xFFEBF4FF) else Pale, origin, rect, CornerRadius(12.dp.toPx()))
        drawRoundRect(if (mode == 2) Blue else Color(0xFFCBD5E1), origin, rect, CornerRadius(12.dp.toPx()), style = Stroke(2.dp.toPx()))
        when (mode) {
            0 -> drawRoundRect(Color(0xFFEAB308), Offset(size.width * .36f, size.height * .33f),
                Size(size.width * .28f, size.height * .34f), CornerRadius(6.dp.toPx()))
            1 -> drawRect(Color(0xFF334155), Offset(origin.x, size.height * .34f), Size(rect.width, size.height * .25f))
            2 -> for (scale in listOf(.22f, .34f, .46f)) drawArc(Blue, 220f, 100f, false,
                Offset(size.width * (.5f - scale / 2), size.height * (.64f - scale)),
                Size(size.width * scale, size.height * scale), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

/** Ilustração Gemini estática; os cinco pontos não correspondem a dígitos coletados. */
@Composable
private fun TerminalIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val origin = Offset(2.dp.toPx(), 2.dp.toPx())
        val rect = Size(size.width - 4.dp.toPx(), 80.dp.toPx())
        drawRoundRect(Pale, origin, rect, CornerRadius(12.dp.toPx()))
        drawRoundRect(Color(0xFFCBD5E1), origin, rect, CornerRadius(12.dp.toPx()), style = Stroke(2.dp.toPx()))
        for (index in 0..4) drawCircle(Color(0xFF94A3B8), 8.dp.toPx(),
            Offset(size.width * (.2f + index * .15f), 42.dp.toPx()))
        val center = size.width / 2
        val top = 116.dp.toPx()
        drawRoundRect(Navy, Offset(center - 20.dp.toPx(), top), Size(40.dp.toPx(), 28.dp.toPx()), CornerRadius(4.dp.toPx()))
        drawArc(Navy, 180f, 180f, false, Offset(center - 12.dp.toPx(), top - 18.dp.toPx()),
            Size(24.dp.toPx(), 32.dp.toPx()), style = Stroke(4.dp.toPx()))
        drawCircle(Color.White, 3.dp.toPx(), Offset(center, top + 14.dp.toPx()))
    }
}
