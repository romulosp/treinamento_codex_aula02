package br.com.romulopenha.gerenciartarefas.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

/**
 * Integration tests covering security (authentication/authorization) and tenant isolation.
 */
@QuarkusTest
public class TarefaResourceSecurityTest {

    private static final String BASE_PATH = "/tarefas";
    private static final String TENANT_A = "tenant-a";
    private static final String TENANT_B = "tenant-b";

    @Test
    @TestSecurity(user = TENANT_A, roles = "ADMIN")
    public void authenticatedAdminCanCreateTaskAndHeaderCannotSwitchTenant() {
        var requestBody = "{\"titulo\": \"Tarefa A\", \"descricao\": \"Desc A\", \"status\": \"PENDENTE\"}";
        var createdId =
            given()
                .header("X-Tenant-Id", TENANT_A)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
            .when()
                .post(BASE_PATH)
            .then()
                .statusCode(201)
                .extract()
                .path("id");

        // A client-supplied tenant header cannot switch the authenticated tenant.
        given()
            .header("X-Tenant-Id", TENANT_B)
        .when()
            .get(BASE_PATH)
        .then()
            .statusCode(403);

        // Verify that tenant A can see the task
        given()
            .header("X-Tenant-Id", TENANT_A)
        .when()
            .get(BASE_PATH)
        .then()
            .statusCode(200)
            .body("tarefas.titulo", hasItem("Tarefa A"));
    }

    @Test
    @TestSecurity(user = "user", roles = "USER")
    public void nonAdminIsForbiddenFromWriteOperations() {
        var requestBody = "{\"titulo\": \"Tarefa X\", \"descricao\": \"Desc X\", \"status\": \"PENDENTE\"}";
        given()
            .header("X-Tenant-Id", TENANT_A)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
        .when()
            .post(BASE_PATH)
        .then()
            .statusCode(403);
    }

    @Test
    public void unauthenticatedRequestIsRejected() {
        given()
        .when()
            .get(BASE_PATH)
        .then()
            .statusCode(401);
    }
}
