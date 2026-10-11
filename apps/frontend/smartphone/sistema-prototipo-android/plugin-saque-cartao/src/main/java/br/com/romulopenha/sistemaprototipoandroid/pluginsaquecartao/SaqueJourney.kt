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
    private val mutableState = MutableStateFlow(SaqueState(step = 0, busy = true,
        message = "Preparando o terminal..."))
    /** Snapshot somente de dados permitidos; não contém respostas brutas do pinpad. */
    val state = mutableState.asStateFlow()
    private var profile: SaquePerfil? = null
    /** Capacidades obtidas na preparação, reutilizadas sem nova consulta em Begin. */
    private var display: SaqueDisplay? = null
    /** Impede repetição da abertura/carga por nova chamada ou reattach desta View. */
    private var initializationStarted = false
    private var operation: Job? = null
    private var activeId: String? = null
    private var disposed = false
    private var cancelSent = false

    /**
     * Prepara uma vez por proprietário: valida perfil/tabela, abre sessão,
     * consulta display e carrega EMV antes de expor o formulário. A sessão
     * permanece aberta para a confirmação; falha parcial fecha o cliente.
     */
    fun load() {
        if (disposed || initializationStarted) return
        initializationStarted = true
        val id = UUID.randomUUID().toString()
        activeId = id
        operation = scope.launch {
            var openingAttempted = false
            var prepared = false
            try {
                val loaded = repository.loadProfile()
                checkpoint()
                profile = loaded
                openingAttempted = true
                update(state.value.copy(message = "Abrindo comunicação com o pinpad..."))
                repository.open(loaded, id)
                checkpoint()
                val capabilities = repository.displayCapabilities(id)
                checkpoint()
                update(state.value.copy(message = "Carregando tabela EMV..."))
                repository.loadTable(loaded, id)
                checkpoint()
                display = capabilities
                prepared = true
                update(state.value.copy(step = 1, busy = false, configReady = true,
                    failureCode = null, failurePhase = null))
                edit { this }
            } catch (failure: Exception) {
                val failed = if (openingAttempted) cleanup(id) else false
                val code = (failure as? SaqueFailure)?.code
                    ?: if (openingAttempted) "BINDING_ERROR" else "CONFIGURATION"
                val phase = (failure as? SaqueFailure)?.phase
                    ?: if (openingAttempted) "command" else "preflight"
                profile = null
                display = null
                update(state.value.copy(busy = false, configReady = false,
                    failureCode = code, failurePhase = phase, cleanupFailed = failed,
                    message = if (!openingAttempted) configurationMessage(code)
                    else if (failed) "Não foi possível confirmar o encerramento da sessão. Verifique o terminal."
                    else "Não foi possível preparar o pinpad. Saia e tente novamente."))
            } finally {
                if (!prepared) activeId = null
                if (disposed) scope.cancel()
            }
        }
    }

    /** Formulário não faz I/O; Begin congela resumo e rejeita cliques repetidos. */
    fun onEvent(event: SaqueEvent) {
        if (disposed) return
        when (event) {
            is SaqueEvent.Value -> edit { copy(valueInput = normalizePurchaseInput(event.value)) }
            is SaqueEvent.Installments -> edit { copy(installmentsInput = event.value.filter(Char::isDigit).take(2)) }
            is SaqueEvent.Modalidade -> edit { copy(modalidade = event.value,
                installmentsInput = if (event.value == SaqueModalidade.DEBITO) "1" else installmentsInput) }
            SaqueEvent.Begin -> begin()
            SaqueEvent.Cancel -> cancel()
            SaqueEvent.Return -> if (!state.value.busy) { dispose(); onReturn() }
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
        val installments = edited.installmentsInput.toIntOrNull() ?: 0
        val summary = runCatching {
            calculateSaque(purchaseCents(edited.valueInput), edited.modalidade, installments, profile)
        }.getOrNull()
        update(edited.copy(summary = summary,
            feeLabel = feeLabelFor(edited.modalidade, installments),
            message = when {
                !edited.configReady -> configurationMessage(edited.failureCode)
                summary == null -> "Informe valores válidos para continuar."
                else -> "Confira o resumo antes de avançar."
            }))
    }

    /** Mensagem objetiva de provisionamento; não expõe caminho nem conteúdo de arquivo. */
    private fun configurationMessage(code: String?): String = when (code) {
        "MISSING" -> "Perfil de homologação não provisionado. Execute o launcher para provisionar perfil e tabela."
        "TABLE_MISSING" -> "Tabela EMV não provisionada para esta homologação."
        else -> "Configuração de homologação inválida. Revise o perfil provisionado."
    }

    /** Encadeia a sequência aprovada, sem avanço por animação ou replay automático. */
    private fun begin() {
        if (state.value.step != 1 || state.value.busy || !state.value.configReady || operation?.isActive == true) return
        val config = profile ?: return
        val capabilities = display ?: return
        val summary = state.value.summary ?: return
        val id = UUID.randomUUID().toString()
        activeId = id
        update(state.value.copy(step = 2, busy = true,
            message = "Preparando confirmação no pinpad..."))
        operation = scope.launch {
            var sentCapture = false
            var result = SaqueResultado.ERROR
            var failureCode: String? = null
            var failurePhase: String? = null
            try {
                checkpoint()
                update(state.value.copy(message = "Aguardando confirmação do cliente..."))
                confirm(capabilities, config, summary, id)
                checkpoint()
                update(state.value.copy(step = 3, message = "Aguardando leitura do cartão..."))
                sentCapture = true
                val type = repository.capture(config, summary, id)
                checkpoint()
                update(state.value.copy(step = 4,
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
                if (failure is SaqueFailure) {
                    failureCode = failure.code
                    failurePhase = failure.phase
                }
                result = when {
                    failure is SaqueFailure && failure.code == "CANCELED" -> SaqueResultado.CANCELED
                    sentCapture -> SaqueResultado.UNKNOWN
                    else -> SaqueResultado.ERROR
                }
            } finally {
                val failed = cleanup(id)
                update(state.value.copy(step = 5, busy = false, result = result,
                    failureCode = failureCode, failurePhase = failurePhase,
                    cleanupFailed = failed, message = if (failed)
                        "Não foi possível confirmar o encerramento da sessão. Verifique o terminal antes de nova operação."
                    else "Homologação de teste. Não autoriza entrega de dinheiro. Retire o cartão, se inserido."))
                activeId = null
                if (disposed) scope.cancel()
            }
        }
    }

    /**
     * Confirma no pinpad com DSP de duas linhas S16 e GKY por página, conforme
     * seções 3.3.5 e 3.3.10 do manual ABECS 2.12. Exibe valor total, parcelas,
     * tarifa e a pergunta de confirmação; OK avança, CANCELA cancela e qualquer
     * outra tecla encerra com falha de confirmação. Valores que não couberem em
     * 16 colunas falham sem truncamento silencioso.
     */
    private suspend fun confirm(display: SaqueDisplay, config: SaquePerfil, summary: SaqueResumo, id: String) {
        require(display.lines >= 2 && display.columns >= 16)
        val pages = listOf(
            "VALOR TOTAL" to "R$ ${formatCents(summary.totalCents)}",
            "PARCELAS" to "${summary.installments}x",
            "TARIFA" to "R$ ${formatCents(summary.feeCents)}",
            "CONFIRMA?" to "CANCELA?",
        )
        require(pages.all { (label, value) -> label.length <= 16 && value.length <= 16 })
        for ((label, value) in pages) {
            checkpoint()
            repository.displayPage(id, label, value)
            confirmed(repository.waitKey(id, config.timeoutSeconds))
        }
    }

    /** Não trata tecla inesperada como confirmação nem omite a categoria local. */
    private fun confirmed(key: Int) {
        if (key == 13) throw SaqueFailure("CANCELED")
        if (key != 0) throw SaqueFailure("CONFIRMATION_ERROR", "command")
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
