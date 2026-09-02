package br.com.romulopenha.gerenciartarefas.domain;

import java.time.LocalDateTime;

/**
 * Representa uma tarefa de domínio, incluindo o identificador do tenant para isolamento entre clientes.
 */
public record Tarefa(Long id,
                     String titulo,
                     String descricao,
                     StatusTarefa status,
                     LocalDateTime dataCriacao,
                     LocalDateTime dataConclusao,
                     String tenantId) { }
