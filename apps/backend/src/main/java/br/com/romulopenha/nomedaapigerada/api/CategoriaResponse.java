package br.com.romulopenha.nomedaapigerada.api;

import br.com.romulopenha.nomedaapigerada.domain.Categoria;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoriaResponse(
        @JsonProperty("id_categoria") Long idCategoria,
        @JsonProperty("nome_categoria") String nomeCategoria,
        @JsonProperty("quantidade_produtos") Integer quantidadeProdutos) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.id(), categoria.nome(), categoria.quantidadeProdutos());
    }
}
