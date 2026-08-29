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

@ApplicationScoped
public class TarefaService {
    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) { this.repository = repository; }

    private String currentTenant() {
        String tenant = TenantContext.getCurrentTenant();
        if (tenant == null) throw new RuntimeException("Tenant not found in context");
        return tenant;
    }

    public List<Tarefa> listar() {
        String tenant = currentTenant();
        return repository.listarPorTenant(tenant).stream()
                .map(TarefaEntity::toDomain)
                .toList();
    }

    public Tarefa buscar(Long id) {
        TarefaEntity entity = localizar(id);
        return entity.toDomain();
    }

    @Transactional
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
