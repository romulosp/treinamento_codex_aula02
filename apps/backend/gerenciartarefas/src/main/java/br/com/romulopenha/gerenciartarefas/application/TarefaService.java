package br.com.romulopenha.gerenciartarefas.application;

import br.com.romulopenha.gerenciartarefas.domain.StatusTarefa;
import br.com.romulopenha.gerenciartarefas.domain.Tarefa;
import br.com.romulopenha.gerenciartarefas.domain.TarefaInvalidaException;
import br.com.romulopenha.gerenciartarefas.domain.TarefaNaoEncontradaException;
import br.com.romulopenha.gerenciartarefas.infrastructure.TarefaEntity;
import br.com.romulopenha.gerenciartarefas.infrastructure.TarefaRepository;
import br.com.romulopenha.gerenciartarefas.infrastructure.TenantContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Coordena operações de tarefas no tenant corrente e aplica transações nas alterações persistentes.
 */
@ApplicationScoped
public class TarefaService {
    private final TarefaRepository repository;

    /** Cria o serviço usando o repositório de tarefas informado. */
    public TarefaService(TarefaRepository repository) { this.repository = repository; }

    private String currentTenant() {
        String tenant = TenantContext.getCurrentTenant();
        if (tenant == null) throw new RuntimeException("Tenant not found in context");
        return tenant;
    }

    /** @return tarefas do tenant corrente; pode ser uma lista vazia. */
    public List<Tarefa> listar() {
        String tenant = currentTenant();
        return repository.listarPorTenant(tenant).stream()
                .map(TarefaEntity::toDomain)
                .toList();
    }

    /** @param id identificador da tarefa no tenant corrente
     *  @return tarefa encontrada
     *  @throws TarefaNaoEncontradaException se o identificador não existir no tenant corrente */
    public Tarefa buscar(Long id) {
        TarefaEntity entity = localizar(id);
        return entity.toDomain();
    }

    @Transactional
    /** Cria e persiste uma tarefa no tenant corrente.
     *  @param titulo título não vazio com no máximo 100 caracteres
     *  @param descricao descrição da tarefa
     *  @param status status textual; quando vazio, usa {@link StatusTarefa#PENDENTE}
     *  @return tarefa persistida
     *  @throws TarefaInvalidaException se o título ou status forem inválidos
     *  @throws RuntimeException se não houver tenant no contexto */
    public Tarefa criar(String titulo, String descricao, String status) {
        validarTitulo(titulo);
        StatusTarefa statusTarefa = converterStatus(status);
        var agora = LocalDateTime.now();
        var tarefa = new Tarefa(null, titulo.trim(), descricao, statusTarefa, agora,
                statusTarefa == StatusTarefa.CONCLUIDA ? agora : null, currentTenant());
        var entity = TarefaEntity.from(tarefa);
        repository.persist(entity);
        return entity.toDomain();
    }

    @Transactional
    /** Atualiza e persiste os dados da tarefa no tenant corrente.
     *  @param id identificador da tarefa
     *  @param titulo título não vazio com no máximo 100 caracteres
     *  @param descricao nova descrição
     *  @param status novo status textual
     *  @return tarefa atualizada
     *  @throws TarefaNaoEncontradaException se a tarefa não existir no tenant corrente
     *  @throws TarefaInvalidaException se o título ou status forem inválidos */
    public Tarefa atualizar(Long id, String titulo, String descricao, String status) {
        var entity = localizar(id);
        validarTitulo(titulo);
        StatusTarefa novoStatus = converterStatus(status);
        entity.titulo = titulo.trim();
        entity.descricao = descricao;
        entity.status = novoStatus;
        entity.dataConclusao = novoStatus == StatusTarefa.CONCLUIDA
                ? (entity.dataConclusao == null ? LocalDateTime.now() : entity.dataConclusao) : null;
        return entity.toDomain();
    }

    @Transactional
    /** Exclui a tarefa do tenant corrente.
     *  @param id identificador da tarefa
     *  @throws TarefaNaoEncontradaException se a tarefa não existir no tenant corrente */
    public void excluir(Long id) { repository.delete(localizar(id)); }

    private TarefaEntity localizar(Long id) {
        if (id == null) throw new TarefaNaoEncontradaException(null);
        return repository.buscarPorIdETenant(id, currentTenant())
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new TarefaInvalidaException("O título da tarefa é obrigatório");
        }
        if (titulo.trim().length() > 100) {
            throw new TarefaInvalidaException("O título da tarefa deve ter no máximo 100 caracteres");
        }
    }

    private StatusTarefa converterStatus(String status) {
        if (status == null || status.isBlank()) return StatusTarefa.PENDENTE;
        try { return StatusTarefa.valueOf(status); }
        catch (IllegalArgumentException exception) { throw new TarefaInvalidaException("Status inválido"); }
    }
}
