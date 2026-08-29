package br.com.romulopenha.gerenciartarefas.domain;

import java.time.LocalDateTime;

/**
 * Representa a entidade de tarefa incluindo o tenantId para isolamento multi‑tenant.
 */
public record Tarefa(Long id,
                     String titulo,
                     String descricao,
                     StatusTarefa status,
                     LocalDateTime dataCriacao,
                     LocalDateTime dataConclusao,
                     String tenantId) { }
