package br.com.romulopenha.nomedaapigerada;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SegurancaCategoriasTest {

    @Test
    @TestSecurity
    void deveRecusarChamadaSemToken() {
        given()
                .when().get("/categorias/")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "cliente-nao-autorizado")
    @JwtSecurity(claims = @Claim(key = "azp", value = "cliente-nao-autorizado"))
    void deveRecusarClienteNaoAutorizado() {
        given()
                .when().get("/categorias/")
                .then().statusCode(403)
                .body("mensagem", equalTo("Cliente não autorizado"));
    }

    @Test
    @TestSecurity(user = "cliente-sem-azp")
    void deveRecusarTokenSemClaimDeCliente() {
        given()
                .when().get("/categorias/")
                .then().statusCode(403)
                .body("mensagem", equalTo("Cliente não autorizado"));
    }

    @Test
    void deveDocumentarRespostasDeSegurancaNoOpenApi() {
        given()
                .when().get("/swagger_gerenciar-categorias.json")
                .then().statusCode(200)
                .body(containsString("Token Bearer ausente ou inválido"))
                .body(containsString("Cliente não autorizado"));
    }
}