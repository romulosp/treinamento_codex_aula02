package br.com.romulopenha.gerenciartarefas.api;

import br.com.romulopenha.gerenciartarefas.application.TarefaService;
import br.com.romulopenha.gerenciartarefas.domain.StatusTarefa;
import br.com.romulopenha.gerenciartarefas.domain.Tarefa;
import br.com.romulopenha.gerenciartarefas.domain.TarefaInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Conjunto de testes automatizados para TarefaResourceUnitTest.
 */
class TarefaResourceUnitTest {
    private TarefaService service;
    private TarefaResource resource;
    private Tarefa tarefa;

    @BeforeEach
    void setUp() {
        service = mock(TarefaService.class);
        resource = new TarefaResource(service);
        tarefa = new Tarefa(1L, "Estudar", "Java", StatusTarefa.PENDENTE,
                LocalDateTime.of(2026, 8, 28, 10, 0), null, "tenant-a");
    }

    @Test
    void listaTarefasMapeiaParaContrato() {
        when(service.listar()).thenReturn(List.of(tarefa));

        var response = resource.listar();

        assertEquals(1, response.tarefas().size());
        assertEquals("Estudar", response.tarefas().get(0).titulo());
    }

    @Test
    void buscaTarefaMapeiaParaContrato() {
        when(service.buscar(1L)).thenReturn(tarefa);

        assertEquals("PENDENTE", resource.buscar(1L).status());
        verify(service).buscar(1L);
    }

    @Test
    void criaTarefaRetorna201() {
        when(service.criar("Estudar", "Java", "PENDENTE")).thenReturn(tarefa);

        var response = resource.criar(new TarefaRequest("Estudar", "Java", "PENDENTE"));

        assertEquals(201, response.getStatus());
        assertEquals("Estudar", ((TarefaResponse) response.getEntity()).titulo());
    }

    @Test
    void atualizaTarefaRetornaContrato() {
        when(service.atualizar(1L, "Estudar", "Java", "PENDENTE")).thenReturn(tarefa);

        assertEquals("Estudar", resource.atualizar(1L, new TarefaRequest("Estudar", "Java", "PENDENTE")).titulo());
    }

    @Test
    void excluiTarefaRetornaMensagem() {
        var result = resource.excluir(1L);

        assertEquals("TAREFA EXCLUIDA COM SUCESSO", result.resultado());
        verify(service).excluir(1L);
    }

    @Test
    void rejeitaCorpoNuloNaCriacao() {
        assertThrows(TarefaInvalidaException.class, () -> resource.criar(null));
    }

    @Test
    void rejeitaCorpoNuloNaAtualizacao() {
        assertThrows(TarefaInvalidaException.class, () -> resource.atualizar(1L, null));
    }
}
