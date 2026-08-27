package br.com.romulopenha.nomedaapigerada.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CategoriaListaResponse(@JsonProperty("categorias") List<CategoriaResponse> categorias) {
}
