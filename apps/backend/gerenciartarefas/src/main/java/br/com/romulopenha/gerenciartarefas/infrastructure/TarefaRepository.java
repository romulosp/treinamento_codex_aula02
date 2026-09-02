package br.com.romulopenha.gerenciartarefas.infrastructure;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

/**
 * Fornece consultas Panache de tarefas filtradas pelo tenant.
 */
@ApplicationScoped
public class TarefaRepository implements PanacheRepositoryBase<TarefaEntity, Long> {
    public List<TarefaEntity> listarPorTenant(String tenantId) {
        return list("tenantId", tenantId);
    }

    public Optional<TarefaEntity> buscarPorIdETenant(Long id, String tenantId) {
        return find("id = ?1 and tenantId = ?2", id, tenantId).firstResultOptional();
    }
}
