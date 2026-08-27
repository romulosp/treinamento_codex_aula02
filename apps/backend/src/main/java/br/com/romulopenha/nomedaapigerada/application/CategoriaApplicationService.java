package br.com.romulopenha.nomedaapigerada.application;

import br.com.romulopenha.nomedaapigerada.domain.Categoria;
import br.com.romulopenha.nomedaapigerada.infrastructure.CategoriaEmMemoriaStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CategoriaApplicationService {

    @Inject
    CategoriaEmMemoriaStore store;

    public List<Categoria> listar() {
        return store.listar();
    }

    public Optional<Categoria> detalhar(long id) {
        return store.buscarPorId(id);
    }

    public Categoria adicionar(String nome, int quantidadeProdutos) {
        return store.adicionar(nome, quantidadeProdutos);
    }

    public Optional<Categoria> atualizar(long id, String nome, int quantidadeProdutos) {
        return store.atualizar(id, nome, quantidadeProdutos);
    }

    public boolean excluir(long id) {
        return store.excluir(id);
    }
}
