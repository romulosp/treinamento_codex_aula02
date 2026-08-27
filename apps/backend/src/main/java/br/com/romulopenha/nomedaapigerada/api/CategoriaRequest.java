package br.com.romulopenha.nomedaapigerada.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CategoriaRequest(
        @JsonProperty("nome_categoria") String nomeCategoria,
        @JsonProperty("quantidade_produtos") Integer quantidadeProdutos) {
}
