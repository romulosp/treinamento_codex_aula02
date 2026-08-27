package br.com.romulopenha.nomedaapigerada.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultadoExclusaoResponse(@JsonProperty("RESULTADO") String resultado) {
}
