package br.com.romulopenha.sistemaprototipoandroid.sharedapi

import android.content.Context
import android.view.View

/** Resultado local de homologação; nenhum estado autoriza entrega de dinheiro. */
enum class CardWithdrawalStatus {
    /** EMV local aprovado, sem autorização financeira. */
    APPROVED,
    /** EMV local negado. */
    DENIED,
    /** Captura local terminada, sem autorizador. */
    CAPTURED,
    /** Jornada encerrada por solicitação do operador. */
    CANCELED,
    /** Falha local antes de resultado conclusivo. */
    ERROR,
    /** Estado da captura ou encerramento não confirmado. */
    UNKNOWN,
    /** O host não dispõe de componente elegível. */
    UNAVAILABLE,
    /** Existe sessão ativa ou bloqueio de cleanup até reinício do host. */
    BUSY,
}

/**
 * Resultado sanitizado, sem dados de cartão ou criptografia.
 * @property status classificação local da jornada
 * @property cleanupConfirmed indica encerramento confirmado; falso impede nova sessão
 */
data class CardWithdrawalResult(
    val status: CardWithdrawalStatus,
    val cleanupConfirmed: Boolean,
)

/** Faceta aditiva entregue somente ao consumidor de negócio carregado pelo host. */
interface IComponentHostContext : PluginHostContext {
    /**
     * Solicita a jornada na main thread para a tela chamadora anexada.
     * O callback recebe no máximo um resultado, após cleanup, enquanto o chamador
     * permanecer ativo. Indisponibilidade e concorrência não executam hardware.
     */
    fun launchCardWithdrawal(onResult: (CardWithdrawalResult) -> Unit)
}

/** Sessão visual retida pelo host até encerramento cooperativo da operação. */
interface CardWithdrawalSession {
    /** View pertencente a esta sessão; sua criação não inicia hardware. */
    val view: View

    /** Solicita saída na main thread; o resultado será entregue após cleanup. */
    fun cancel()
}

/** Ponto de entrada do APK de infraestrutura, sem inscrição em menu. */
interface ICardWithdrawalComponent : IPluginApp {
    /**
     * Cria sessão na main thread; attach inicia preparação, detach solicita limpeza.
     * O callback ocorre na main thread uma única vez após pedido explícito de saída,
     * com encerramento confirmado ou falho, sem reproduzir a transação.
     */
    fun createWithdrawal(
        context: Context,
        onComplete: (CardWithdrawalResult) -> Unit,
    ): CardWithdrawalSession
}

/**
 * Pedido imutável de pagamento, sem dados pessoais ou de cartão.
 * @property orderId identificador opaco de correlação da venda
 * @property amountCents valor base exato da venda em centavos, antes da tarifa
 */
data class CardPaymentRequest(val orderId: String, val amountCents: Long) {
    init {
        require(orderId.isNotBlank() && orderId.length <= 128)
        require(amountCents in 1..999999999999L)
    }
}

/** Faceta 1.4 para abrir cartão com o valor base imutável do pedido. */
interface ICardPaymentHostContext : IComponentHostContext {
    /** Solicita na main thread pagamento vinculado ao pedido, sem fallback editável. */
    fun launchCardPayment(request: CardPaymentRequest, onResult: (CardWithdrawalResult) -> Unit)
}

/** Provedor 1.4 que aceita valor de venda sem permitir edição pelo cliente. */
interface ICardPaymentComponent : ICardWithdrawalComponent {
    /** Cria a sessão com base fixa; hardware só inicia após attach da View. */
    fun createPayment(context: Context, request: CardPaymentRequest,
                      onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession
}

/** Quem apresenta o resultado local da captura, sem alterar seu status ou validade financeira. */
enum class CardPaymentResultPresentation {
    /** Preserva tela terminal e ação VOLTAR do componente. */
    COMPONENT,
    /** Devolve APPROVED/CAPTURED ao consumidor após cleanup confirmado, sem VOLTAR. */
    CALLER,
}

/**
 * Opções visuais aditivas 1.5, separadas do DTO monetário para preservar sua ABI.
 * @property resultPresentation proprietário da apresentação após conclusão local/cleanup
 */
data class CardPaymentOptions(val resultPresentation: CardPaymentResultPresentation = CardPaymentResultPresentation.COMPONENT)

/** Faceta 1.5 opcional de apresentação; preserva chamadas 1.3/1.4 herdadas. */
interface ICardPaymentPresentationHostContext : ICardPaymentHostContext {
    /** Solicita na main thread pedido/opções sem fallback; resultado original é entregue após cleanup. */
    fun launchCardPayment(request: CardPaymentRequest, options: CardPaymentOptions,
                          onResult: (CardWithdrawalResult) -> Unit)
}

/** Provedor 1.5 capaz de devolver conclusão local automaticamente ao consumidor. */
interface ICardPaymentPresentationComponent : ICardPaymentComponent {
    /** Cria na main thread sessão cujo attach prepara a operação; CALLER retorna status original após conclusão/cleanup. */
    fun createPayment(context: Context, request: CardPaymentRequest, options: CardPaymentOptions,
                      onComplete: (CardWithdrawalResult) -> Unit): CardWithdrawalSession
}
