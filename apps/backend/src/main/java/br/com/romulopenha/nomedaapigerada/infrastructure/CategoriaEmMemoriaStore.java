package br.com.romulopenha.nomedaapigerada.infrastructure;

import br.com.romulopenha.nomedaapigerada.domain.Categoria;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class CategoriaEmMemoriaStore {

    private final Map<Long, Categoria> categorias = new LinkedHashMap<>();
    private long proximoId = 4L;

    public CategoriaEmMemoriaStore() {
        categorias.put(1L, new Categoria(1L, "CAMISAS", 2));
        categorias.put(2L, new Categoria(2L, "ACESSÓRIOS", 1));
        categorias.put(3L, new Categoria(3L, "VIDEO-GAMES", 4));
    }

    public synchronized List<Categoria> listar() {
        return List.copyOf(categorias.values());
    }

    public synchronized Optional<Categoria> buscarPorId(long id) {
        return Optional.ofNullable(categorias.get(id));
    }

    public synchronized Categoria adicionar(String nome, int quantidadeProdutos) {
        Categoria categoria = new Categoria(proximoId++, nome, quantidadeProdutos);
        categorias.put(categoria.id(), categoria);
        return categoria;
    }

    public synchronized Optional<Categoria> atualizar(long id, String nome, int quantidadeProdutos) {
        if (!categorias.containsKey(id)) {
            return Optional.empty();
        }
        Categoria categoria = new Categoria(id, nome, quantidadeProdutos);
        categorias.put(id, categoria);
        return Optional.of(categoria);
    }

    public synchronized boolean excluir(long id) {
        return categorias.remove(id) != null;
    }
}
