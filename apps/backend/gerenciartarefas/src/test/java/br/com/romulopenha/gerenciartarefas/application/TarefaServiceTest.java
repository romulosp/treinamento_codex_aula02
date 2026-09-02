package br.com.romulopenha.gerenciartarefas.application;

import br.com.romulopenha.gerenciartarefas.domain.StatusTarefa;
import br.com.romulopenha.gerenciartarefas.domain.TarefaInvalidaException;
import br.com.romulopenha.gerenciartarefas.infrastructure.TarefaEntity;
import br.com.romulopenha.gerenciartarefas.infrastructure.TarefaRepository;
import br.com.romulopenha.gerenciartarefas.infrastructure.TenantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Conjunto de testes automatizados para TarefaServiceTest.
 */
class TarefaServiceTest {
    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("tenant-test");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void statusAusenteAssumePendente() {
        var repository = mock(TarefaRepository.class);
        doAnswer(invocation -> { invocation.<TarefaEntity>getArgument(0).id = 1L; return null; })
                .when(repository).persist(any(TarefaEntity.class));
        var tarefa = new TarefaService(repository).criar("Estudar", null, null);
        assertEquals(StatusTarefa.PENDENTE, tarefa.status());
        assertNull(tarefa.dataConclusao());
    }

    @Test
    void statusConcluidaRegistraDataConclusao() {
        var repository = mock(TarefaRepository.class);
        doAnswer(invocation -> { invocation.<TarefaEntity>getArgument(0).id = 1L; return null; })
                .when(repository).persist(any(TarefaEntity.class));
        var tarefa = new TarefaService(repository).criar("Estudar", null, "CONCLUIDA");
        assertNotNull(tarefa.dataConclusao());
    }

    @Test
    void rejeitaStatusInvalido() {
        var repository = mock(TarefaRepository.class);
        assertThrows(TarefaInvalidaException.class, () -> new TarefaService(repository).criar("Estudar", null, "INVALIDO"));
    }

    @Test
    void limpaConclusaoAoRetornarParaPendente() {
        var entity = new TarefaEntity();
        entity.id = 1L; entity.titulo = "Estudar"; entity.status = StatusTarefa.CONCLUIDA;
        entity.dataConclusao = java.time.LocalDateTime.now();
        var repository = mock(TarefaRepository.class);
        when(repository.buscarPorIdETenant(1L, "tenant-test")).thenReturn(Optional.of(entity));
        var tarefa = new TarefaService(repository).atualizar(1L, "Estudar", null, "PENDENTE");
        assertNull(tarefa.dataConclusao());
    }

    @Test
    void naoPermiteExcluirTarefaDeOutroTenant() {
        var repository = mock(TarefaRepository.class);
        when(repository.buscarPorIdETenant(1L, "tenant-test")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> new TarefaService(repository).excluir(1L));
        verify(repository, never()).delete(any(TarefaEntity.class));
    }

    @Test
    void listaUsaTenantAtual() {
        var entity = new TarefaEntity();
        entity.id = 1L;
        entity.titulo = "Estudar";
        entity.status = StatusTarefa.PENDENTE;
        entity.dataCriacao = java.time.LocalDateTime.now();
        entity.tenantId = "tenant-test";
        var repository = mock(TarefaRepository.class);
        when(repository.listarPorTenant("tenant-test")).thenReturn(List.of(entity));

        assertEquals(1, new TarefaService(repository).listar().size());
        verify(repository).listarPorTenant("tenant-test");
    }

    @Test
    void buscaTarefaDoTenantAtual() {
        var entity = new TarefaEntity();
        entity.id = 1L;
        entity.titulo = "Estudar";
        entity.status = StatusTarefa.PENDENTE;
        entity.dataCriacao = java.time.LocalDateTime.now();
        entity.tenantId = "tenant-test";
        var repository = mock(TarefaRepository.class);
        when(repository.buscarPorIdETenant(1L, "tenant-test")).thenReturn(Optional.of(entity));

        assertEquals(1L, new TarefaService(repository).buscar(1L).id());
    }

    @Test
    void rejeitaTituloNuloOuLongo() {
        var repository = mock(TarefaRepository.class);
        var service = new TarefaService(repository);

        assertThrows(TarefaInvalidaException.class, () -> service.criar(null, "Java", "PENDENTE"));
        assertThrows(TarefaInvalidaException.class, () -> service.criar("x".repeat(101), "Java", "PENDENTE"));
        verify(repository, never()).persist(any(TarefaEntity.class));
    }

    @Test
    void atualizaParaConcluidaRegistraData() {
        var entity = new TarefaEntity();
        entity.id = 1L;
        entity.titulo = "Estudar";
        entity.status = StatusTarefa.PENDENTE;
        entity.dataCriacao = java.time.LocalDateTime.now();
        entity.tenantId = "tenant-test";
        var repository = mock(TarefaRepository.class);
        when(repository.buscarPorIdETenant(1L, "tenant-test")).thenReturn(Optional.of(entity));

        var tarefa = new TarefaService(repository).atualizar(1L, "Estudar", "Java", "CONCLUIDA");

        assertNotNull(tarefa.dataConclusao());
    }

    @Test
    void exigeTenantNoContexto() {
        TenantContext.clear();
        var repository = mock(TarefaRepository.class);

        assertThrows(RuntimeException.class, () -> new TarefaService(repository).listar());
    }
}
