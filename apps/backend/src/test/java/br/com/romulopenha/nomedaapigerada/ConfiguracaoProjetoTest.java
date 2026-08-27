package br.com.romulopenha.nomedaapigerada;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracaoProjetoTest {

    @Test
    void deveUsarNomeDerivadoEOferecerScriptDeInicializacao() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        String properties = Files.readString(Path.of("src/main/resources/application.properties"));
        String script = Files.readString(Path.of("start_aplicacao.bat"));
        String prompt = Files.readString(Path.of("../../.github/prompts/executar-mudanca-spec-driven.prompt.md"));

        assertTrue(pom.contains("<artifactId>gerenciar-categorias</artifactId>"));
        assertTrue(properties.contains("quarkus.application.name=gerenciar-categorias"));
        assertTrue(properties.contains("quarkus.smallrye-openapi.path=/swagger_gerenciar-categorias.json"));
        assertTrue(properties.contains("jdbc:h2:mem:GERENCIAR_CATEGORIAS"));
        assertTrue(properties.contains("quarkus.hibernate-orm.database.default-schema=GERENCIAR_CATEGORIAS"));
        assertFalse(properties.contains("nome_da_api_gerada"));
        assertFalse(properties.contains("nome_api_projeto"));
        assertFalse(properties.contains("NOME_SCHEMA"));

        assertTrue(script.contains("setlocal"));
        assertTrue(script.contains("set JAVA_HOME=C:\\Desenvolvimento\\jdk-17.0.11"));
        assertTrue(script.contains("set MAVEN_HOME=C:\\Desenvolvimento\\apache-maven-3.8.8"));
        assertTrue(script.contains("set PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"));
        assertTrue(script.contains("\"%JAVA_HOME%\\bin\\java.exe\" -version"));
        assertTrue(script.contains("mvn quarkus:dev"));
        assertTrue(script.contains("pause"));
        assertTrue(script.contains("endlocal"));

        assertTrue(prompt.contains("sem solicitar ao usuário uma nova instrução entre fases aprovadas"));
        assertTrue(prompt.contains("avance automaticamente para a seguinte quando o respectivo gate for aprovado"));
        assertTrue(prompt.contains("Interrompa imediatamente se um gate for reprovado, falhar ou estiver bloqueado"));
        assertTrue(prompt.contains("primeira fase à qual a mudança deve retornar"));
    }
}
