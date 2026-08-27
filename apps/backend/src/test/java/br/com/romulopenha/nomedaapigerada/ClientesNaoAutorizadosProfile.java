package br.com.romulopenha.nomedaapigerada;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class ClientesNaoAutorizadosProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("caixa.security.clients-authorized", "");
    }
}