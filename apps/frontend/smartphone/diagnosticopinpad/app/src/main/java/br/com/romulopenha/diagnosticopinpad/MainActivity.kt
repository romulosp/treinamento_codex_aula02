package br.com.romulopenha.diagnosticopinpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.romulopenha.diagnosticopinpad.ui.DiagnosticScreen
import br.com.romulopenha.diagnosticopinpad.ui.DiagnosticViewModel

/** Activity única que hospeda a tela do Functional Lab inicial. */
class MainActivity : ComponentActivity() {
    private val viewModel: DiagnosticViewModel by viewModels {
        val application = application as DiagnosticApplication
        DiagnosticViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiagnosticScreen(viewModel = viewModel, onExit = { finish() })
        }
    }
}

/** Factory explícita para manter a composição simples e testável. */
private class DiagnosticViewModelFactory(
    private val application: DiagnosticApplication,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiagnosticViewModel::class.java)) {
            return DiagnosticViewModel(application.repository, application.logger) as T
        }
        error("ViewModel desconhecido: ${modelClass.name}")
    }
}
