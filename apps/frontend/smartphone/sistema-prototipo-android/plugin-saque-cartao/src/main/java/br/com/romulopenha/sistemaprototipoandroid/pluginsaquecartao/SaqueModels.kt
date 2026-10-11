package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import java.math.BigDecimal
import java.math.RoundingMode

/** Modalidade da tela e tipo T1_APPTYPE da tabela DES aprovada; não é modo de leitura. */
internal enum class SaqueModalidade(val applicationType: String) { CREDITO("01"), DEBITO("02") }

/** Cenário explícito de resposta simulada, restrito à homologação DES. */
internal enum class SaqueCenario(val options: String, val arc: String) {
    APPROVE("0000", "00"), DENY("1000", "05"), COMMUNICATION_ERROR("2000", "")
}

/** Configuração não secreta do laboratório, validada antes de qualquer comando. */
internal data class SaquePerfil(
    val environment: String,
    val host: String,
    val port: Int,
    val timeoutMillis: Long,
    val timeoutSeconds: Int,
    val tableAcquirer: String,
    val tableVersion: String,
    val creditApplicationTypes: String,
    val debitApplicationTypes: String,
    val maxBaseCents: Long,
    val gtkDataMethod: String,
    val gtkKeyIndex: Int,
    val scenario: SaqueCenario,
) {
    /** Rejeita perfil incompleto ou produção sem reproduzir conteúdo da configuração. */
    fun validate() {
        require(environment == "DES")
        require(host in setOf("localhost", "127.0.0.1", "10.0.2.2"))
        require(port in 1..65535 && timeoutMillis in 100..120000 && timeoutSeconds in 1..120)
        require(tableAcquirer.matches(Regex("[0-9]{2}")) && tableAcquirer != "00")
        require(tableVersion.matches(Regex("[A-Za-z0-9]{10}")))
        require(validApplicationTypes(creditApplicationTypes) && validApplicationTypes(debitApplicationTypes))
        require(creditApplicationTypes.chunked(2).toSet() != debitApplicationTypes.chunked(2).toSet())
        require(maxBaseCents in 1..MaxAmountCents)
        require(gtkDataMethod == "30" && gtkKeyIndex in 0..99)
    }

    /** Seleciona filtro DES gerado do domínio; não deduz modalidade pela bandeira. */
    fun applicationTypes(modalidade: SaqueModalidade): String =
        if (modalidade == SaqueModalidade.CREDITO) creditApplicationTypes else debitApplicationTypes
}

/** Limite de representação monetária N12 da biblioteca. */
internal const val MaxAmountCents: Long = 999999999999L

/** Valida a lista concatenada de 1 a 10 tipos N2 distintos entre 01 e 98. */
internal fun validApplicationTypes(value: String): Boolean {
    if (value.length !in 2..20 || value.length % 2 != 0 || !value.all(Char::isDigit)) return false
    val parts = value.chunked(2)
    return parts.all { it.toInt() in 1..98 } && parts.distinct().size == parts.size
}

/** Resumo imutável confirmado, compartilhado por todas as fases físicas. */
internal data class SaqueResumo(
    val modalidade: SaqueModalidade,
    val baseCents: Long,
    val feeCents: Long,
    val totalCents: Long,
    val installments: Int,
    /** Percentual do crédito; `null` no débito, cuja tarifa é fixa em reais. */
    val feePercent: BigDecimal?,
) {
    /** Valor exato em centavos N12, sem separadores ou conversão por ponto flutuante. */
    fun amountN12(): String = totalCents.toString().padStart(12, '0')
}

/** Tarifa fixa aprovada do débito, independente do valor da compra. */
internal const val DebitFeeCents: Long = 100

/** Taxa aprovada do crédito: 5,34% em uma parcela, mais 0,80 ponto por adicional. */
internal fun installmentFeePercent(count: Int): BigDecimal {
    require(count in 1..12)
    return BigDecimal("5.34").add(BigDecimal("0.80").multiply(BigDecimal(count - 1)))
}

/** Converte dígitos monetários em centavos exatos; entrada não numérica é rejeitada. */
internal fun purchaseCents(digits: String): Long {
    require(digits.length in 1..12 && digits.all(Char::isDigit))
    val cents = digits.toLongOrNull() ?: throw IllegalArgumentException()
    require(cents in 1..MaxAmountCents)
    return cents
}

/** Remove só separadores da máscara; mantém inválidos/excesso para bloquear o cálculo. */
internal fun normalizePurchaseInput(value: String): String {
    if (value.any { it !in '0'..'9' && it != '.' && it != ',' }) return value
    val digits = value.filter { it in '0'..'9' }
    return digits.trimStart('0').ifEmpty { if (digits.isEmpty()) "" else "0" }
}

/** Formata centavos válidos; mantém entrada inválida visível sem conversão/truncamento. */
internal fun formatPurchaseDigits(digits: String): String =
    digits.takeIf { it.length in 1..12 && it.all { char -> char in '0'..'9' } }
        ?.toLongOrNull()?.let(::formatCents) ?: digits

/** Texto da taxa aplicada: débito exibe o valor fixo, crédito o percentual da parcela. */
internal fun feeLabelFor(modalidade: SaqueModalidade, installments: Int): String {
    if (modalidade == SaqueModalidade.DEBITO) return "R$ 1,00"
    val percent = runCatching { installmentFeePercent(installments) }.getOrNull() ?: return "—"
    return percent.toPlainString().replace('.', ',') + "%"
}

/** Calcula tarifa HALF_UP a partir de centavos; sem perfil simula sem autorizar hardware. */
internal fun calculateSaque(baseCents: Long, modalidade: SaqueModalidade, installments: Int,
                            perfil: SaquePerfil? = null): SaqueResumo {
    require(installments in 1..12)
    require(modalidade != SaqueModalidade.DEBITO || installments == 1)
    require(baseCents in 1..(perfil?.maxBaseCents ?: MaxAmountCents))
    val percent = if (modalidade == SaqueModalidade.CREDITO) installmentFeePercent(installments) else null
    val fee = percent?.let {
        BigDecimal.valueOf(baseCents).multiply(it).divide(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP).longValueExact()
    } ?: DebitFeeCents
    val total = Math.addExact(baseCents, fee)
    require(total in 1..MaxAmountCents)
    return SaqueResumo(modalidade, baseCents, fee, total, installments, percent)
}

/** Formata moeda brasileira a partir de centavos, mantendo precisão exata. */
internal fun formatCents(value: Long): String {
    val units = (value / 100).toString().reversed().chunked(3).joinToString(".").reversed()
    return "$units,${(value % 100).toString().padStart(2, '0')}"
}

/** Resultado seguro para o operador; nenhuma opção representa autorização real. */
internal enum class SaqueResultado(val title: String) {
    APPROVED("Homologação aprovada"), DENIED("Homologação negada"),
    CAPTURED("Captura de homologação concluída"), ERROR("Erro na homologação"),
    CANCELED("Homologação cancelada"), UNKNOWN("Resultado da homologação indeterminado")
}

/** Estado público da tela sem parâmetros de cartão, dados EMV ou conteúdo da tabela. */
internal data class SaqueState(
    /** Etapa 0 prepara sessão/EMV antes das cinco telas visíveis da transação. */
    val step: Int = 1,
    val modalidade: SaqueModalidade = SaqueModalidade.CREDITO,
    /** Dígitos monetários da compra em centavos, como digitados pelo operador. */
    val valueInput: String = "",
    val installmentsInput: String = "1",
    val feeLabel: String = "5,34%",
    /** Prontidão publicada somente após sessão aberta e carga EMV confirmada. */
    val configReady: Boolean = false,
    /** Código sanitizado da última falha, quando reconhecido pela fachada. */
    val failureCode: String? = null,
    /** Fase sanitizada da última falha, quando reconhecida pela fachada. */
    val failurePhase: String? = null,
    val busy: Boolean = false,
    val cancelRequested: Boolean = false,
    val summary: SaqueResumo? = null,
    val result: SaqueResultado? = null,
    val message: String = "Carregando configuração de homologação...",
    val cleanupFailed: Boolean = false,
)

/** Intenções da tela; editar formulário nunca produz eventos artificiais de hardware. */
internal sealed interface SaqueEvent {
    data class Value(val value: String) : SaqueEvent
    data class Installments(val value: String) : SaqueEvent
    data class Modalidade(val value: SaqueModalidade) : SaqueEvent
    data object Begin : SaqueEvent
    data object Cancel : SaqueEvent
    data object Return : SaqueEvent
}
