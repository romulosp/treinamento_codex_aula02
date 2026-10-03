package br.com.romulopenha.diagnosticopinpad

import android.app.Application
import br.com.romulopenha.diagnosticopinpad.data.GoMobileRepository
import br.com.romulopenha.diagnosticopinpad.data.JsonLineLogger

/** Composição manual das dependências do laboratório Android. */
class DiagnosticApplication : Application() {
    /** Logger estruturado privado do aplicativo. */
    lateinit var logger: JsonLineLogger
        private set

    /** Repository que encapsula o AAR Go. */
    lateinit var repository: GoMobileRepository
        private set

    override fun onCreate() {
        super.onCreate()
        logger = JsonLineLogger(this)
        repository = GoMobileRepository(logger)
    }
}
