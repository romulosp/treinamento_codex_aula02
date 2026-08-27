package br.com.romulopenha.nomedaapigerada.domain;

/** Modelo de domínio de uma categoria mantida apenas durante a execução. */
public record Categoria(Long id, String nome, Integer quantidadeProdutos) {
}
