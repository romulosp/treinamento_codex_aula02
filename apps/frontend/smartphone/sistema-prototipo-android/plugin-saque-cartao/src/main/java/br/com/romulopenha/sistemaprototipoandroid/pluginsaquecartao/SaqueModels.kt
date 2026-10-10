package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import java.math.BigDecimal
import java.math.RoundingMode

/** Modalidade escolhida pelo operador; não identifica o modo físico de leitura. */
internal enum class SaqueModalidade { CREDITO, DEBITO }

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
    val feePercent: BigDecimal,
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
        require(feePercent >= BigDecimal.ZERO && feePercent <= BigDecimal(100))
        require(maxBaseCents in 1..MaxAmountCents)
        require(gtkDataMethod == "30" && gtkKeyIndex in 0..99)
    }

    /** Seleciona apenas o filtro configurado; não deduz modalidade a partir de bandeira. */
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
    val feePercent: BigDecimal,
) {
    /** Valor exato em centavos N12, sem separadores ou conversão por ponto flutuante. */
    fun amountN12(): String = totalCents.toString().padStart(12, '0')
}

/** Calcula tarifa HALF_UP em centavos e impede inválidos, limite e overflow. */
internal fun calculateSaque(input: String, modalidade: SaqueModalidade, installments: String, perfil: SaquePerfil): SaqueResumo {
    require(input.length <= 16 && input.matches(Regex("[0-9]+([,.][0-9]{1,2})?")))
    val base = BigDecimal(input.replace(',', '.')).movePointRight(2).longValueExact()
    val count = installments.toIntOrNull() ?: throw IllegalArgumentException()
    require(base in 1..perfil.maxBaseCents && count in 1..12)
    require(modalidade != SaqueModalidade.DEBITO || count == 1)
    val fee = BigDecimal.valueOf(base).multiply(perfil.feePercent).divide(BigDecimal(100))
        .setScale(0, RoundingMode.HALF_UP).longValueExact()
    val total = Math.addExact(base, fee)
    require(total in 1..MaxAmountCents)
    return SaqueResumo(modalidade, base, fee, total, count, perfil.feePercent)
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
    val step: Int = 1,
    val completedSteps: Set<Int> = emptySet(),
    val modalidade: SaqueModalidade = SaqueModalidade.CREDITO,
    val valueInput: String = "",
    val installmentsInput: String = "1",
    val feeLabel: String = "",
    val configReady: Boolean = false,
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
