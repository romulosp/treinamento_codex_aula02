package br.com.romulopenha.sistemaprototipoandroid

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Tokens visuais normativos compartilhados exclusivamente pela variante do menu. */
internal object MenuDesignTokens {
    /** Cor principal de marca, título e ações. */
    val primary = Color(0xFF005CA9)
    /** Cor principal sob ponteiro. */
    val primaryHover = Color(0xFF004A88)
    /** Cor principal durante pressão. */
    val primaryPressed = Color(0xFF003D70)
    /** Cor da saída. */
    val danger = Color(0xFFD32F2F)
    /** Cor da saída sob ponteiro. */
    val dangerHover = Color(0xFFB71C1C)
    /** Cor da saída durante pressão. */
    val dangerPressed = Color(0xFF8E0000)
    /** Início do gradiente vertical. */
    val backgroundStart = Color(0xFFEBF4FA)
    /** Final do gradiente vertical. */
    val backgroundEnd = Color(0xFFB3D4E6)
    /** Superfície opaca de cabeçalho e rodapé. */
    val surface = Color.White
    /** Fundo das informações da sessão. */
    val infoBackground = Color(0xFFF8F9FA)
    /** Borda tracejada das informações. */
    val infoBorder = Color(0xFFA0B0C0)
    /** Divisória de cabeçalho e rodapé. */
    val divider = Color(0xFFE1E8EE)
    /** Texto principal. */
    val text = Color(0xFF333333)
    /** Texto secundário da versão. */
    val muted = Color(0xFF666666)
    /** Contorno externo de foco. */
    val focus = Color(0xFFFFB300)
    /** Fundo de ação desabilitada. */
    val disabledBackground = Color(0xFFD5DCE2)
    /** Texto de ação desabilitada. */
    val disabledText = Color(0xFF6B737A)
    /** Largura nominal de uma funcionalidade. */
    val cardWidth = 240.dp
    /** Altura nominal de uma funcionalidade. */
    val cardHeight = 64.dp
    /** Espaço entre ações e seções. */
    val gap = 16.dp
    /** Altura fixa do rodapé. */
    val footerHeight = 48.dp
}
