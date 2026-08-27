package br.com.romulopenha.nomedaapigerada;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@TestProfile(ClientesNaoAutorizadosProfile.class)
class SegurancaSemClientesAutorizadosTest {

    @Test
    @TestSecurity(user = "cliente-autorizado")
    @JwtSecurity(claims = @Claim(key = "azp", value = "cliente-autorizado"))
    void deveRecusarClienteAutenticadoQuandoListaEstaVazia() {
        given()
                .when().get("/categorias/")
                .then().statusCode(403)
                .body("mensagem", equalTo("Cliente não autorizado"));
    }
}