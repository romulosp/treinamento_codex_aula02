package br.com.romulopenha.nomedaapigerada;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@TestSecurity(user = "cliente-autorizado")
@JwtSecurity(claims = @Claim(key = "azp", value = "cliente-autorizado"))
class CategoriaResourceTest {

    @Test
    void deveGerenciarCategoriasConformeContratosAprovados() {
        given()
                .when().get("/categorias/")
                .then().statusCode(200)
                .body("categorias", hasSize(3))
                .body("categorias[0].id_categoria", equalTo(1))
                .body("categorias[0].nome_categoria", equalTo("CAMISAS"))
                .body("categorias[0].quantidade_produtos", equalTo(2))
                .body("categorias[1].id_categoria", equalTo(2))
                .body("categorias[1].nome_categoria", equalTo("ACESSÓRIOS"))
                .body("categorias[1].quantidade_produtos", equalTo(1))
                .body("categorias[2].id_categoria", equalTo(3))
                .body("categorias[2].nome_categoria", equalTo("VIDEO-GAMES"))
                .body("categorias[2].quantidade_produtos", equalTo(4));

        given()
                .when().get("/categorias/1")
                .then().statusCode(200)
                .body("id_categoria", equalTo(1))
                .body("nome_categoria", equalTo("CAMISAS"))
                .body("quantidade_produtos", equalTo(2));

        given().contentType("application/json")
                .body("{\"nome_categoria\":\"RELÓGIO\",\"quantidade_produtos\":5}")
                .when().post("/categorias/add")
                .then().statusCode(201)
                .body("id_categoria", equalTo(4))
                .body("nome_categoria", equalTo("RELÓGIO"))
                .body("quantidade_produtos", equalTo(5));

        given().contentType("application/json")
                .body("{\"nome_categoria\":\"CAMISAS SOCIAIS\",\"quantidade_produtos\":3}")
                .when().put("/categorias/1")
                .then().statusCode(200)
                .body("id_categoria", equalTo(1))
                .body("nome_categoria", equalTo("CAMISAS SOCIAIS"))
                .body("quantidade_produtos", equalTo(3));

        given()
                .when().delete("/categorias/deletar/1")
                .then().statusCode(200)
                .body("RESULTADO", equalTo("CATEGORIA EXCLUIDA COM SUCESSO"));

        given().contentType("application/json")
                .body("{\"nome_categoria\":\" \",\"quantidade_produtos\":-1}")
                .when().post("/categorias/add")
                .then().statusCode(400)
                .body("mensagem", notNullValue());

        given().contentType("application/json")
                .body("{\"nome_categoria\":\"TESTE\",\"quantidade_produtos\":\"invalido\"}")
                .when().post("/categorias/add")
                .then().statusCode(400)
                .body("mensagem", notNullValue());

        given()
                .when().get("/categorias/999")
                .then().statusCode(404)
                .body("mensagem", notNullValue());

        given().contentType("application/json")
                .body("{\"nome_categoria\":\"TESTE\",\"quantidade_produtos\":1}")
                .when().put("/categorias/999")
                .then().statusCode(404)
                .body("mensagem", notNullValue());

        given()
                .when().delete("/categorias/deletar/999")
                .then().statusCode(404)
                .body("mensagem", notNullValue());
    }
}