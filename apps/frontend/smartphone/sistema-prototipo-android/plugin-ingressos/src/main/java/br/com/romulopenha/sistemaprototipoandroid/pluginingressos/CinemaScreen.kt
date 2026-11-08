package br.com.romulopenha.sistemaprototipoandroid.pluginingressos

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.romulopenha.sistemaprototipoandroid.sharedapi.CardWithdrawalStatus
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val CinemaBlue = Color(0xFF006BAA)
private val CinemaInk = Color(0xFF171C20)
private val CinemaMuted = Color(0xFF4C5962)
private val CinemaLine = Color(0xFFC2CDD3)
private val CinemaSold = Color(0xFF8A9094)

/**
 * Mantém catálogo/mapa na coluna esquerda e resumo ancorado no topo do tablet.
 * O resumo possui corpo rolável e rodapé fixo; a programação continua somente informativa.
 * Abaixo de 840dp, preserva sequência vertical com resumo e ações acessíveis por rolagem.
 */
@Composable
internal fun CinemaScreen(
    state: CinemaState, catalog: CinemaCatalog,
    onMovie: (String) -> Unit, onSelect: (String) -> Unit,
    onOrder: () -> Unit, onPayment: () -> Unit, onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onStanding: (String, Int) -> Unit = { _, _ -> },
) {
    val canChooseMovie = state.pending == null && !state.paymentInProgress
    MaterialTheme(colorScheme = lightColorScheme(
        primary = CinemaBlue, onPrimary = Color.White,
        surface = Color.White, onSurface = CinemaInk, outline = CinemaLine,
    )) {
        if (state.completedOrderId != null) {
            SuccessPanel(modifier)
        } else {
            BoxWithConstraints(modifier.fillMaxSize().background(Brush.verticalGradient(
                listOf(Color(0xFFE7F3FB), Color(0xFFB9D9E9)),
            ))) {
                val carousel: @Composable () -> Unit = {
                    PosterCarousel(catalog, state.movieId, canChooseMovie, onMovie)
                }
                val seats: @Composable () -> Unit = {
                    SessionDetails(state.session)
                    SeatsPanel(state, onSelect)
                    StandingPanel(state, onStanding)
                    Text("DEMONSTRAÇÃO • Ingressos sem validade. Dados temporários.",
                        color = CinemaMuted, fontSize = 11.sp)
                    if (state.message.isNotEmpty()) Text(state.message, color = CinemaMuted, fontSize = 12.sp)
                }
                if (maxWidth >= 840.dp) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            carousel()
                            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)) { seats() }
                        }
                        OrderPanel(state, onOrder, onPayment, onCancel, Modifier.width(320.dp).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        carousel()
                        seats()
                        OrderPanel(state, onOrder, onPayment, onCancel, Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

/** Apresenta confirmação local acessível enquanto a rota aguarda o próximo atendimento. */
@Composable
private fun SuccessPanel(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color(0xFFE7F3FB)), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Surface(color = Color(0xFFDCFCE7), shape = CircleShape) {
                Text("✓", Modifier.padding(horizontal = 30.dp, vertical = 12.dp),
                    color = Color(0xFF15803D), fontSize = 68.sp)
            }
            Text(TRANSACTION_SUCCESS_MESSAGE,
                Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = CinemaInk, fontWeight = FontWeight.Bold, fontSize = 30.sp,
                lineHeight = 38.sp, textAlign = TextAlign.Center)
            Text("Preparando um novo atendimento...", color = CinemaMuted,
                fontSize = 18.sp, textAlign = TextAlign.Center)
            Text("DEMONSTRAÇÃO • Resultado local, sem comprovação financeira. Ingresso sem validade.",
                color = CinemaMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Exibe eventos com apresentação fixa; cartazes ficam menores após seleção para liberar o mapa.
 * A borda marca o filme ativo e troca permanece bloqueada durante pedido/pagamento.
 */
@Composable
private fun PosterCarousel(
    catalog: CinemaCatalog, currentMovieId: String?, canChoose: Boolean,
    onChoose: (String) -> Unit, modifier: Modifier = Modifier,
) {
    var filter by remember { mutableStateOf<EventKind?>(null) }
    val events = catalog.movies.filter { filter == null || it.kind == filter }
    val compact = currentMovieId != null
    val scroll = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Eventos", color = CinemaInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(catalog.cinema, color = CinemaMuted, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (listOf<EventKind?>(null) + EventKind.entries).forEach { kind ->
                OutlinedButton(onClick = { filter = kind; scope.launch { scroll.scrollToItem(0) } }, enabled = canChoose,
                    modifier = Modifier.semantics { selected = kind == filter }) {
                    Text(kind?.label ?: "Todos")
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cardWidth = if (compact) 176.dp else if (maxWidth >= 780.dp) (maxWidth - 48.dp) / 3.2f else 248.dp
            LazyRow(state = scroll, flingBehavior = rememberSnapFlingBehavior(scroll),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                itemsIndexed(events, key = { _, movie -> movie.id }) { index, movie ->
                    val title = movie.title
                    val isCurrent = movie.id == currentMovieId
                    val scale by animateFloatAsState(if (isCurrent) 1f else .98f,
                        animationSpec = tween(180), label = "Realce do cartaz selecionado")
                    Surface(onClick = {
                        onChoose(movie.id)
                        scope.launch { scroll.animateScrollToItem(index) }
                    }, enabled = canChoose,
                        modifier = Modifier.width(cardWidth).aspectRatio(16f / 9f).graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }.semantics {
                            selected = isCurrent
                            contentDescription = "Cartaz: $title"
                        },
                        color = Color.Transparent, shape = RoundedCornerShape(12.dp),
                        border = if (isCurrent) BorderStroke(3.dp, CinemaBlue) else null) {
                        Box(Modifier.padding(if (isCurrent) 5.dp else 2.dp).clip(RoundedCornerShape(8.dp))) {
                            PosterImage(title, Modifier.fillMaxSize(), ContentScale.Crop, Alignment.TopCenter)
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = .85f)))))
                            Text(title, Modifier.align(Alignment.BottomStart).padding(14.dp),
                                color = Color.White, fontSize = if (compact) 16.sp else 23.sp, fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            IconButton(onClick = { scope.launch { scroll.animateScrollToItem((scroll.firstVisibleItemIndex - 1).coerceAtLeast(0)) } },
                enabled = scroll.canScrollBackward && !scroll.isScrollInProgress,
                modifier = Modifier.align(Alignment.CenterStart).size(42.dp)
                    .background(Color.White.copy(alpha = .94f), CircleShape)
                    .semantics { contentDescription = "Cartazes anteriores" }) {
                Text("‹", color = CinemaMuted, fontSize = 30.sp)
            }
            IconButton(onClick = { scope.launch { scroll.animateScrollToItem((scroll.firstVisibleItemIndex + 1).coerceAtMost(events.lastIndex.coerceAtLeast(0))) } },
                enabled = scroll.canScrollForward && !scroll.isScrollInProgress,
                modifier = Modifier.align(Alignment.CenterEnd).size(42.dp)
                    .background(Color.White.copy(alpha = .94f), CircleShape)
                    .semantics { contentDescription = "Próximos cartazes" }) {
                Text("›", color = CinemaMuted, fontSize = 30.sp)
            }
        }
    }
}

/** Confere programação e preço fixos do filme, sem ação de escolha ou edição de horário. */
@Composable
private fun SessionDetails(session: CinemaSession?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically) {
        if (session == null) {
            Text("Selecione um evento para ver sua apresentação programada.", color = CinemaMuted, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SESSÃO PROGRAMADA", color = CinemaMuted, fontSize = 10.sp, letterSpacing = 1.sp)
                Surface(color = CinemaBlue, shape = RoundedCornerShape(28.dp)) {
                    Text("${session.time} - ${session.room}", Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                        color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Text("${session.date} · ${session.venue}", color = CinemaMuted, fontSize = 13.sp)
                Text("Ingresso: ${formatPrice(session.price)}", color = CinemaMuted, fontSize = 13.sp)
            }
        }
    }
}

/** Mapa configurável com rolagem independente, posições especiais e ampliação em tela cheia. */
@Composable
private fun SeatsPanel(state: CinemaState, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    if (state.session == null) {
        Text("Selecione um evento para habilitar os lugares.", modifier, color = CinemaMuted)
        return
    }
    if (state.session.layout.places.isEmpty()) return
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { expanded = true }) { Text("Ampliar mapa") }
        if (!expanded) {
            StageLabel(requireNotNull(state.session).kind)
            SeatRows(state, onSelect)
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SeatLegend("Livre", Color.White)
            SeatLegend("Selecionado", CinemaBlue)
            SeatLegend("Vendido", CinemaSold)
        }
        Text("♿ PCD · AC Acompanhante · ♥ Namoro (par)", color = CinemaMuted, fontSize = 12.sp)
    }
    if (expanded) Dialog(onDismissRequest = { expanded = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFE7F3FB)) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mapa de lugares", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { expanded = false }) { Text("Fechar mapa") }
                }
                SeatMap(state, onSelect, Modifier.weight(1f).fillMaxWidth())
                Text("${state.lines().sumOf { it.quantity }} ingressos · ${formatPrice(state.lines().sumOf { it.total })}")
            }
        }
    }
}

/** Mapa ampliado com viewport próprio; o mapa embutido usa a rolagem vertical da coluna esquerda. */
@Composable
private fun SeatMap(state: CinemaState, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        StageLabel(requireNotNull(state.session).kind)
        Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            SeatRows(state, onSelect)
        }
    }
}

/** Referência de orientação compartilhada pelos mapas embutido e ampliado. */
@Composable
private fun StageLabel(kind: EventKind, modifier: Modifier = Modifier) {
    Text(if (kind == EventKind.CINEMA) "TELA" else "PALCO",
        modifier.fillMaxWidth().background(Color(0xFFADBFCA)).padding(8.dp), textAlign = TextAlign.Center)
}

/** Fileiras com um único eixo horizontal e numeração ordenada; a fileira A fica próxima à referência. */
@Composable
private fun SeatRows(state: CinemaState, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val session = requireNotNull(state.session)
    Column(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        session.layout.places.groupBy { it.row }.toSortedMap().forEach { (row, places) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(row.toString(), Modifier.width(20.dp))
                places.sortedBy { it.number }.forEach { place ->
                    SeatButton(place, state, onSelect)
                    if (place.number in session.layout.aisleAfter) Spacer(Modifier.width(24.dp))
                }
            }
        }
    }
}

/** Poltrona com identificação textual e ícone especial; o controle nativo mantém toque e foco. */
@Composable
private fun SeatButton(place: VenuePlace, state: CinemaState, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val seat = place.id
    val sold = seat in state.sold || seat in state.occupied
    val chosen = seat in (state.order?.seats ?: state.selectedSeats)
    val fill = if (sold) CinemaSold else if (chosen) CinemaBlue else Color.White
    Surface(onClick = { onSelect(seat) }, enabled = state.session != null && !sold && state.pending == null,
        color = Color.Transparent, shape = RoundedCornerShape(6.dp),
        modifier = modifier.width(48.dp).height(74.dp).semantics {
            contentDescription = "Assento $seat"
            stateDescription = "${place.kind.label}${place.partner?.let { ", vinculado a $it" } ?: ""}, " +
                if (sold) "Vendido" else if (chosen) "Selecionado" else "Livre"
            selected = chosen
            role = Role.Button
        }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Armchair(fill, Modifier.size(46.dp))
                val symbol = when (place.kind) {
                    PlaceKind.WHEELCHAIR -> "♿"; PlaceKind.COMPANION -> "AC"; PlaceKind.COUPLE -> "♥"; else -> ""
                }
                Text(symbol, color = if (chosen && !sold) Color.White else CinemaBlue, fontSize = 16.sp)
            }
            Text(seat, color = CinemaInk, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

/** Quantidades de setores sem numeração, limitadas pelo estoque disponível da apresentação. */
@Composable
private fun StandingPanel(state: CinemaState, onQuantity: (String, Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.session?.sectors?.forEach { sector ->
            val quantity = (state.order?.standing ?: state.standing).getOrDefault(sector.id, 0)
            val available = sector.capacity - sector.occupied - state.soldStanding.getOrDefault(sector.id, 0)
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("${sector.name} · ${formatPrice(sector.price)}", fontWeight = FontWeight.Bold)
                    Text("$available disponíveis · Sem numeração", color = CinemaMuted, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { onQuantity(sector.id, quantity - 1) }, enabled = state.pending == null && quantity > 0,
                            modifier = Modifier.semantics { contentDescription = "Diminuir ${sector.name}" }) { Text("−") }
                        Text(quantity.toString(), Modifier.semantics { contentDescription = "Quantidade ${sector.name}: $quantity" })
                        OutlinedButton(onClick = { onQuantity(sector.id, quantity + 1) }, enabled = state.pending == null && quantity < available,
                            modifier = Modifier.semantics { contentDescription = "Adicionar ${sector.name}" }) { Text("+") }
                    }
                }
            }
        }
    }
}

/** Desenha encosto, braços e assento de uma poltrona; toda interação fica no controle nativo pai. */
@Composable
private fun Armchair(fill: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.width / 46f
        val outline = Color(0xFF555D61)
        fun part(x: Float, y: Float, w: Float, h: Float, radius: Float) {
            val origin = Offset(x * unit, y * unit)
            val extent = Size(w * unit, h * unit)
            val corner = CornerRadius(radius * unit)
            drawRoundRect(fill, origin, extent, corner)
            drawRoundRect(outline, origin, extent, corner, style = Stroke(1.3f * unit))
        }
        part(8f, 4f, 30f, 33f, 5f)
        part(3f, 20f, 7f, 22f, 3f)
        part(36f, 20f, 7f, 22f, 3f)
        part(9f, 31f, 28f, 11f, 3f)
    }
}

/** Identifica as cores por texto; não atribui estados inexistentes ao estoque local. */
@Composable
private fun SeatLegend(label: String, fill: Color, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Armchair(fill, Modifier.size(22.dp))
        Text(label, color = CinemaMuted, fontSize = 11.sp)
    }
}

/** Resumo adaptativo: corpo rolável e rodapé estável na altura limitada do tablet. */
@Composable
private fun OrderPanel(
    state: CinemaState, onOrder: () -> Unit, onPayment: () -> Unit,
    onCancel: () -> Unit, modifier: Modifier = Modifier,
) {
    Surface(modifier, color = Color.White, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CinemaLine)) {
        BoxWithConstraints(Modifier.padding(16.dp)) {
            val bounded = maxHeight != androidx.compose.ui.unit.Dp.Infinity
            val lines = state.lines()
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Resumo da compra", color = CinemaInk, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                val bodyModifier = if (bounded) Modifier.weight(1f) else Modifier.heightIn(max = 240.dp)
                Column(bodyModifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val session = state.session
                    if (session == null) {
                        Text("Selecione um evento para começar.", color = CinemaMuted, fontSize = 14.sp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PosterImage(session.film, Modifier.width(48.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(6.dp)))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(session.film, color = CinemaInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("${session.time} - ${session.room}", color = CinemaInk, fontSize = 14.sp)
                                Text("${session.date} · ${session.venue}", color = CinemaMuted, fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(color = CinemaLine)
                        if (lines.isEmpty()) Text("Escolha lugares ou ingressos de pista.", color = CinemaMuted)
                        lines.forEach { line ->
                            Text("${line.label} · ${line.quantity} × ${formatPrice(line.unitPrice)} = ${formatPrice(line.total)}",
                                color = CinemaInk, fontSize = 13.sp)
                        }
                    }
                }
                if (state.session != null) {
                    HorizontalDivider(color = CinemaLine)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total dos ingressos", Modifier.weight(1f).padding(end = 8.dp), color = CinemaMuted, fontSize = 12.sp)
                        Text(formatPrice(lines.sumOf { it.total }), color = CinemaInk, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    if (state.pending == null) {
                        Button(onClick = onOrder, enabled = lines.isNotEmpty(), modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)) { Text("Criar pedido", Modifier.padding(vertical = 6.dp)) }
                    } else {
                        val canLaunch = !state.paymentInProgress && (state.paymentStatus == null ||
                            state.paymentStatus == CardWithdrawalStatus.UNAVAILABLE || state.paymentStatus == CardWithdrawalStatus.BUSY)
                        Button(onClick = onPayment, enabled = canLaunch, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
                            Text("ABRIR PAGAMENTO NO TERMINAL POS", textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        OutlinedButton(onClick = onCancel, enabled = !state.paymentInProgress,
                            modifier = Modifier.fillMaxWidth()) { Text("Cancelar pedido") }
                    }
                    Text("Demonstração • Sem comprovação financeira. Tarifa e total no pagamento com cartão.",
                        color = CinemaMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

/** Resolve somente a arte local; não consulta filmes, metadados ou disponibilidade. */
private fun posterResource(title: String): String {
    val normalized = title.lowercase(Locale.ROOT)
    return when {
        "batman" in normalized -> "cinema/posters/batman.webp"
        "avatar" in normalized -> "cinema/posters/avatar.webp"
        "coringa" in normalized || "joker" in normalized -> "cinema/posters/coringa.webp"
        else -> "cinema/posters/generic.webp"
    }
}

/**
 * Carrega arte pelo classloader deste APK, sem consultar os Resources do host.
 * Decodifica em IO e fecha o stream; produceState cancela a publicação ao sair da composição.
 * Fundo neutro permanece enquanto a imagem local não está disponível.
 */
@Composable
private fun PosterImage(
    title: String, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
) {
    val path = posterResource(title)
    val bitmap by produceState<ImageBitmap?>(null, path) {
        value = withContext(Dispatchers.IO) {
            PluginIngressosApp::class.java.classLoader?.getResourceAsStream(path)?.use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        }
    }
    Box(modifier.background(Color(0xFF17384C))) {
        bitmap?.let {
            Image(it, contentDescription = "Ilustração de $title", modifier = Modifier.fillMaxSize(),
                contentScale = scale, alignment = alignment)
        }
    }
}

/** Formata centavos positivos em reais sem ponto flutuante. */
internal fun formatPrice(cents: Int): String = "R$ ${cents / 100},${(cents % 100).toString().padStart(2, '0')}"

/** Formata o total agregado sem conversão para ponto flutuante. */
internal fun formatPrice(cents: Long): String = "R$ ${cents / 100},${(cents % 100).toString().padStart(2, '0')}"
