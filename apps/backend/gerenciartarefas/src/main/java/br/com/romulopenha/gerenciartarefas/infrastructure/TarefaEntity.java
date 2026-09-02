package br.com.romulopenha.gerenciartarefas.infrastructure;

import br.com.romulopenha.gerenciartarefas.domain.StatusTarefa;
import br.com.romulopenha.gerenciartarefas.domain.Tarefa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Representa a tarefa persistida no banco relacional e sua conversão para o modelo de domínio.
 */
@Entity
@Table(name = "tarefas")
public class TarefaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, length = 100)
    public String titulo;
    @Column(length = 255)
    public String descricao;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    public StatusTarefa status;
    @Column(name = "data_criacao", nullable = false)
    public LocalDateTime dataCriacao;
    @Column(name = "data_conclusao")
    public LocalDateTime dataConclusao;
    @Column(name = "tenant_id", nullable = false, length = 36)
    public String tenantId;

    public Tarefa toDomain() { return new Tarefa(id, titulo, descricao, status, dataCriacao, dataConclusao, tenantId); }
    public static TarefaEntity from(Tarefa tarefa) {
        var entity = new TarefaEntity();
        entity.id = tarefa.id();
        entity.titulo = tarefa.titulo();
        entity.descricao = tarefa.descricao();
        entity.status = tarefa.status();
        entity.dataCriacao = tarefa.dataCriacao();
        entity.dataConclusao = tarefa.dataConclusao();
        entity.tenantId = tarefa.tenantId();
        return entity;
    }
}
