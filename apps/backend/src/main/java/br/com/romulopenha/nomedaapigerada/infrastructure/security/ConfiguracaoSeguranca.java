package br.com.romulopenha.nomedaapigerada.infrastructure.security;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "caixa.security")
public interface ConfiguracaoSeguranca {

    Optional<String> clientsAuthorized();
}