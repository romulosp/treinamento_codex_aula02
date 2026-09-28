package br.com.romulopenha.diagnosticopinpad.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.romulopenha.diagnosticopinpad.data.CatalogAction
import br.com.romulopenha.diagnosticopinpad.data.CatalogField
import br.com.romulopenha.diagnosticopinpad.data.CatalogInput

/** Renderiza configuração, estado e ações do diagnóstico sem lógica Go. */
@Composable
fun DiagnosticScreen(viewModel: DiagnosticViewModel, onExit: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DiagnosticScreenContent(
        state = state,
        onHostChanged = viewModel::updateHost,
        onPortChanged = viewModel::updatePort,
        onTimeoutChanged = viewModel::updateTimeout,
        onVersion = viewModel::checkVersion,
        onPing = viewModel::ping,
        onOpen = viewModel::open,
        onInfo = viewModel::getInfo,
        onClose = viewModel::close,
        onCancel = viewModel::cancel,
        onCatalogAction = viewModel::executeCatalog,
        onExit = { viewModel.exit(onExit) },
    )
}

/** Função pura de UI, apropriada para teste Compose e preview futuro. */
@Composable
fun DiagnosticScreenContent(
    state: DiagnosticUiState,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onTimeoutChanged: (String) -> Unit,
    onVersion: () -> Unit,
    onPing: () -> Unit,
    onOpen: () -> Unit,
    onInfo: () -> Unit,
    onClose: () -> Unit,
    onCancel: () -> Unit,
    onCatalogAction: (CatalogAction, CatalogInput) -> Unit,
    onExit: () -> Unit,
) {
    var dialogAction by remember { mutableStateOf<CatalogAction?>(null) }
    val busy = state.status == DiagnosticStatus.CONNECTING ||
        state.status == DiagnosticStatus.RUNNING ||
        state.status == DiagnosticStatus.CANCELING
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Diagnosticopinpad", style = MaterialTheme.typography.headlineSmall)
            Text("Functional Lab — Bridge Android/Windows")
        }
        item {
            OutlinedTextField(
                value = state.host,
                onValueChange = onHostChanged,
                label = { Text("Host") },
                supportingText = { Text("Default: localhost; Emulator sem adb reverse: 10.0.2.2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.port,
                    onValueChange = onPortChanged,
                    label = { Text("Porta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.timeoutMillis,
                    onValueChange = onTimeoutChanged,
                    label = { Text("Timeout ms") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Estado: ${state.status}", style = MaterialTheme.typography.titleMedium)
                    if (state.action.isNotBlank()) Text("Ação: ${state.action}")
                    state.operationId?.let { Text("Operação: $it") }
                    state.durationMillis?.let { Text("Duração: ${it} ms") }
                    state.version.takeIf { it.isNotBlank() }?.let { Text("Binding: $it") }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onVersion, enabled = !busy, modifier = Modifier.weight(1f)) {
                    Text("Versão")
                }
                Button(onClick = onPing, enabled = !busy, modifier = Modifier.weight(1f)) {
                    Text("Ping")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onOpen, enabled = !busy, modifier = Modifier.weight(1f)) {
                    Text("Abrir")
                }
                Button(
                    onClick = onInfo,
                    enabled = !busy && state.status == DiagnosticStatus.OPEN,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("GetInfo")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onClose, enabled = !busy, modifier = Modifier.weight(1f)) {
                    Text("Fechar")
                }
                OutlinedButton(onClick = onCancel, enabled = busy, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
            }
        }
        item {
            Text("Catálogo funcional", style = MaterialTheme.typography.titleLarge)
            Text("As ações abaixo correspondem ao menu local da biblioteca Go.")
        }
        CatalogAction.values().groupBy { it.group }.forEach { (group, actions) ->
            item {
                Text(group, style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    actions.forEach { catalogAction ->
                        val enabled = !busy && catalogAction.enabled
                        Button(
                            onClick = {
                                when {
                                    catalogAction == CatalogAction.EXIT -> onExit()
                                    catalogAction.fields.isEmpty() -> onCatalogAction(catalogAction, CatalogInput())
                                    else -> dialogAction = catalogAction
                                }
                            },
                            enabled = enabled,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("${catalogAction.number}. ${catalogAction.title}")
                        }
                        if (!catalogAction.enabled) {
                            Text(
                                text = catalogAction.disabledReason.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
            state.error?.let {
                Text(
                    text = "Erro: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { contentDescription = "Erro do diagnóstico: $it" },
                )
            }
            if (state.result.isNotBlank()) {
                Text("Resultado", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = state.result,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Resultado do diagnóstico" },
                )
            }
        }
    }
    dialogAction?.let { action ->
        CatalogActionDialog(
            action = action,
            onDismiss = { dialogAction = null },
            onConfirm = { input ->
                dialogAction = null
                onCatalogAction(action, input)
            },
        )
    }
}

/** Formulário efêmero para parâmetros de uma ação do catálogo. */
@Composable
private fun CatalogActionDialog(
    action: CatalogAction,
    onDismiss: () -> Unit,
    onConfirm: (CatalogInput) -> Unit,
) {
    val values = remember(action) { mutableStateMapOf<String, String>() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${action.number}. ${action.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                action.fields.forEach { field ->
                    CatalogFieldInput(field, values[field.key].orEmpty()) { values[field.key] = it }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(CatalogInput(values.toMap())) }) { Text("Executar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

/** Campo de entrada com transformação visual para segredos e chaves. */
@Composable
private fun CatalogFieldInput(field: CatalogField, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(field.label) },
        singleLine = true,
        visualTransformation = if (field.secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
    )
}
