package br.com.romulopenha.sistemaprototipoandroid.pluginsaquecartao

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Proprietário de uma jornada por View, com eventos serializados na main thread.
 * Não salva nem reenvia transações em recriação. Cancelamento alcança o Go;
 * detach invalida a UI e aguarda cleanup antes de liberar o proprietário.
 */
internal class SaqueJourney(
    private val repository: SaqueRepository,
    private val scope: CoroutineScope,
    private val onReturn: () -> Unit,
) {
    private val mutableState = MutableStateFlow(SaqueState())
    /** Snapshot somente de dados permitidos; não contém respostas brutas do pinpad. */
    val state = mutableState.asStateFlow()
    private var profile: SaquePerfil? = null
    private var operation: Job? = null
    private var activeId: String? = null
    private var disposed = false
    private var cancelSent = false

    /** Lê configuração sem hardware; falha impede início e não cria defaults. */
    fun load() {
        scope.launch {
            try {
                val loaded = repository.loadProfile()
                if (!disposed) {
                    profile = loaded
                    update(state.value.copy(configReady = true))
                    edit { this }
                }
            } catch (_: Exception) {
                update(state.value.copy(message = "Configuração de homologação incompleta."))
            }
        }
    }

    /** Formulário não faz I/O; Begin congela resumo e rejeita cliques repetidos. */
    fun onEvent(event: SaqueEvent) {
        if (disposed) return
        when (event) {
            is SaqueEvent.Value -> edit { copy(valueInput = event.value.take(32)) }
            is SaqueEvent.Installments -> edit { copy(installmentsInput = event.value.take(3)) }
            is SaqueEvent.Modalidade -> edit { copy(modalidade = event.value,
                installmentsInput = if (event.value == SaqueModalidade.DEBITO) "1" else installmentsInput) }
            SaqueEvent.Begin -> begin()
            SaqueEvent.Cancel -> cancel()
            SaqueEvent.Return -> if (!state.value.busy) onReturn()
        }
    }

    /** Invalida respostas tardias e cancela/fecha mesmo quando a View já saiu. */
    fun dispose() {
        if (disposed) return
        disposed = true
        if (operation?.isActive == true) cancel() else scope.launch {
            cleanup(activeId ?: UUID.randomUUID().toString())
            scope.cancel()
        }
    }

    /** Recalcula com dinheiro exato; entrada inválida remove a possibilidade de avançar. */
    private fun edit(change: SaqueState.() -> SaqueState) {
        if (state.value.step != 1 || state.value.busy) return
        val edited = state.value.change()
        val summary = runCatching {
            calculateSaque(edited.valueInput, edited.modalidade, edited.installmentsInput, profile)
        }.getOrNull()
        val feeLabel = runCatching { installmentFeePercent(edited.installmentsInput.toInt())
            .toPlainString().replace('.', ',') + "%" }.getOrDefault("—")
        update(edited.copy(summary = summary, feeLabel = feeLabel, message = when {
            !edited.configReady -> "Configuração de homologação incompleta. Você pode editar a compra."
            summary == null -> "Informe valores válidos para continuar."
            else -> "Confira o resumo antes de avançar."
        }))
    }

    /** Encadeia a sequência aprovada, sem avanço por animação ou replay automático. */
    private fun begin() {
        val config = profile ?: return
        val summary = state.value.summary ?: return
        if (state.value.step != 1 || state.value.busy || operation?.isActive == true) return
        val id = UUID.randomUUID().toString()
        activeId = id
        update(state.value.copy(step = 2, completedSteps = setOf(1), busy = true,
            message = "Preparando confirmação no pinpad..."))
        operation = scope.launch {
            var sentCapture = false
            var result = SaqueResultado.ERROR
            try {
                repository.open(config, id)
                checkpoint()
                val display = repository.displayCapabilities(id)
                repository.loadTable(config, id)
                checkpoint()
                update(state.value.copy(message = "Aguardando confirmação do cliente..."))
                confirm(display, config, summary, id)
                checkpoint()
                update(state.value.copy(step = 3, completedSteps = setOf(1, 2), message = "Aguardando leitura do cartão..."))
                sentCapture = true
                val type = repository.capture(config, summary, id)
                checkpoint()
                update(state.value.copy(step = 4, completedSteps = setOf(1, 2, 3),
                    message = "Siga as instruções no pinpad; digite a senha se solicitada."))
                if (type == "00") {
                    update(state.value.copy(message = "Peça ao cliente que digite a senha no pinpad."))
                    repository.capturePin(id)
                    checkpoint()
                }
                repository.tracks(config, id)
                checkpoint()
                result = if (type in setOf("03", "05")) {
                    when (repository.continueEmv(config, summary, id)) {
                        '0' -> SaqueResultado.APPROVED
                        '1' -> SaqueResultado.DENIED
                        '2' -> {
                            checkpoint()
                            if (repository.finalizeEmv(config, id) == '0') SaqueResultado.APPROVED else SaqueResultado.DENIED
                        }
                        else -> SaqueResultado.UNKNOWN
                    }
                } else SaqueResultado.CAPTURED
                checkpoint()
            } catch (failure: Exception) {
                result = when {
                    failure is SaqueFailure && failure.code == "CANCELED" -> SaqueResultado.CANCELED
                    sentCapture -> SaqueResultado.UNKNOWN
                    else -> SaqueResultado.ERROR
                }
            } finally {
                val failed = cleanup(id)
                update(state.value.copy(step = 5, busy = false, result = result,
                    completedSteps = if (result in setOf(SaqueResultado.APPROVED, SaqueResultado.DENIED, SaqueResultado.CAPTURED))
                        state.value.completedSteps + 4 else state.value.completedSteps,
                    cleanupFailed = failed, message = if (failed)
                        "Não foi possível confirmar o encerramento da sessão. Verifique o terminal antes de nova operação."
                    else "Homologação de teste. Não autoriza entrega de dinheiro. Retire o cartão, se inserido."))
                activeId = null
                if (disposed) scope.cancel()
            }
        }
    }

    /** Confirmar exige tecla 0 real em todas as páginas; 13 cancela, demais falham. */
    private suspend fun confirm(display: SaqueDisplay, config: SaquePerfil, summary: SaqueResumo, id: String) {
        val lines = listOf("TOTAL R$ ${formatCents(summary.totalCents)}", "PARCELAS ${summary.installments}",
            "TARIFA R$ ${formatCents(summary.feeCents)}", "OK / CANCELA")
        if (display.lines >= 4 && lines.all { it.length <= display.columns }) {
            repository.displayText(id, lines.joinToString("\n"))
            confirmed(repository.waitKey(id, config.timeoutSeconds))
        } else {
            require(display.lines >= 2 && display.columns >= 16)
            val pages = listOf("TOTAL" to "R$ ${formatCents(summary.totalCents)}",
                "PARCELAS" to "${summary.installments}", "TARIFA" to "R$ ${formatCents(summary.feeCents)}")
            require(pages.all { it.second.length <= 16 })
            for ((label, value) in pages) {
                checkpoint()
                repository.displayPage(id, label, value)
                confirmed(repository.waitKey(id, config.timeoutSeconds))
            }
        }
    }

    /** Não trata tecla inesperada como confirmação. */
    private fun confirmed(key: Int) {
        if (key == 13) throw SaqueFailure("CANCELED")
        require(key == 0)
    }

    /** Interrompe entre fases após pedido; confirmação de cancelamento vem do core. */
    private fun checkpoint() {
        if (cancelSent || disposed) throw SaqueFailure("CANCELED")
        if (state.value.cancelRequested) throw SaqueFailure("CANCEL_UNCONFIRMED")
    }

    /** Cancel usa o mesmo operationID ativo; não depende da coroutine bloqueante terminar. */
    private fun cancel() {
        val id = activeId ?: return
        if (state.value.cancelRequested) return
        update(state.value.copy(cancelRequested = true, message = "Cancelando operação no pinpad..."))
        scope.launch {
            try {
                repository.cancel(id)
                cancelSent = true
            } catch (_: Exception) {
                update(state.value.copy(message = "Aguardando encerramento da operação..."))
            }
        }
    }

    /** Prazo limitado inclui cancel e close; falha não substitui resultado principal. */
    private suspend fun cleanup(id: String): Boolean = withContext(NonCancellable) {
        runCatching {
            withTimeout((profile?.timeoutMillis ?: 5000L) * 2) {
                try { repository.cancel(id) } finally { repository.close(id) }
            }
        }.isFailure
    }

    /** Respostas de proprietário desanexado nunca publicam estado. */
    private fun update(value: SaqueState) { if (!disposed) mutableState.value = value }
}
