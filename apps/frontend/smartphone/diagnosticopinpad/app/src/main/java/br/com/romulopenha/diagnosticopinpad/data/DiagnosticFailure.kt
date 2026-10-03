package br.com.romulopenha.diagnosticopinpad.data

/** Erro público com categoria/fase do Go e orientação local sem texto do driver. */
data class DiagnosticFailure(val code: String, val phase: String, val message: String) {
    companion object {
        private val knownCodes = setOf(
            "BRIDGE_UNREACHABLE", "TIMEOUT", "PROTOCOL_ERROR", "BUSY", "OWNERSHIP_ERROR",
            "SERIAL_UNAVAILABLE", "CANCELED", "DISCONNECTED", "PINPAD_ERROR", "PINPAD_CLOSED",
        )
        private val knownPhases = setOf(
            "preflight", "open", "hello", "acquire", "ownership", "serial_open", "serial_read", "serial_write",
            "session", "ping", "receive", "forward", "command", "bridge", "binding",
        )

        /** Cria o erro a partir dos campos tipados pelo binding, aplicando fallback seguro. */
        fun fromCodes(code: String?, phase: String?): DiagnosticFailure {
            val safeCode = code?.takeIf(knownCodes::contains) ?: "BINDING_ERROR"
            val safePhase = phase?.takeIf(knownPhases::contains) ?: "binding"
            return DiagnosticFailure(safeCode, safePhase, messageFor(safeCode))
        }

        /** Extrai somente campos reconhecidos na fachada; não ecoa Throwable.message. */
        fun from(failure: Throwable): DiagnosticFailure {
            // Go emite somente CODE:phase; nunca extrair mensagem arbitrária.
            val parts = failure.message.orEmpty().split(':', limit = 2)
            return fromCodes(parts.firstOrNull(), parts.getOrNull(1))
        }

        /** Mensagens estáveis não incluem payload, PAN, caminhos ou stack trace. */
        fun messageFor(code: String): String = when (code) {
            "BRIDGE_UNREACHABLE" -> "Bridge inacessível. Confira o BAT aberto, host/porta e adb reverse."
            "TIMEOUT" -> "Prazo expirado; resultado não confirmado. Confira conexão e log Windows."
            "PROTOCOL_ERROR" -> "Resposta inválida. Confira o processo e a versão do Bridge."
            "BUSY" -> "Pinpad ocupado. Feche a sessão concorrente antes de abrir novamente."
            "OWNERSHIP_ERROR" -> "Falha no lock da COM. Consulte a operação no log Windows."
            "SERIAL_UNAVAILABLE" -> "Serial indisponível. Confira PORTA_PINPAD, driver e log Windows."
            "CANCELED" -> "Operação cancelada; confira o estado da sessão."
            "DISCONNECTED" -> "Conexão perdida. Abra uma nova sessão explicitamente."
            "PINPAD_ERROR" -> "Pinpad rejeitou a operação. Consulte o status seguro no diagnóstico Go."
            "PINPAD_CLOSED" -> "Pinpad fechado. Clique em Abrir antes de executar esta ação."
            else -> "Falha controlada do binding. Consulte os metadados da operação."
        }
    }
}
