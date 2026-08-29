package br.com.romulopenha.gerenciartarefas.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TarefaResourceTest {
    @Test
    @TestSecurity(user = "tenant-a", roles = "ADMIN")
    void criaEConsultaTarefa() {
        var id = given().contentType("application/json")
                .body("{\"titulo\":\"Tarefa manual\",\"descricao\":\"Persistir no H2 de teste\",\"status\":\"PENDENTE\"}")
                .when().post("/tarefas").then().statusCode(201).body("status", equalTo("PENDENTE"))
                .extract().path("id");
        given().when().get("/tarefas/" + id).then().statusCode(200).body("titulo", equalTo("Tarefa manual"));
    }

    @Test
    @TestSecurity(user = "tenant-a", roles = "ADMIN")
    void rejeitaTituloVazio() {
        given().contentType("application/json")
                .body("{\"titulo\":\" \",\"descricao\":\"descricao\",\"status\":\"PENDENTE\"}")
                .when().post("/tarefas").then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "tenant-a", roles = "ADMIN")
    void retorna404ParaTarefaInexistente() {
        given().when().get("/tarefas/99999").then().statusCode(404).body("mensagem", equalTo("Tarefa não encontrada"));
    }
}
