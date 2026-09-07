# Evidências

## Situação atual no encerramento

Estado final: `VALIDADA` após restauração e segunda revisão `IMPLEMENTACAO_APROVADA`. A reprovação por ausência de código foi resolvida. Evidências anteriores preservadas abaixo; VAL-006 registra nova execução no código restaurado.

Estado: evidências da implementação registradas; disponível para teste manual. Não representa aprovação integral da aplicação nem novo encerramento formal.

## VAL-006 — Validação após restauração

Windows 10 amd64, Oracle Java 17.0.11 e Maven 3.8.8 confirmados por java -version e mvn -version. Sessão temporária com JAVA_HOME e MAVEN_HOME nos caminhos obrigatórios.

Comando: `mvn verify` em apps/backend/fotogaleria. Saída 0, 14 testes (13 unitários e 1 HTTP/H2), zero falhas, erros e skips; build Quarkus concluído. Sem filtro de classes. Comprova requisitos 1, 2, 3 e 6 no código restaurado. Requisito 4 mantém comprovação PostgreSQL de VAL-003 e inspeção atual do SQL, sem nova alteração no banco do usuário. Requisito 5 atendido por este registro.

Inventário: FotoEntity é a única classe Java de produção alterada, mapeamento declarativo, sem regra nova que exija teste unitário próprio. Teste HTTP/H2 verifica contrato; a evidência PostgreSQL anterior comprova o problema específico de driver. Demais classes de produção não são alteradas pela 011.

Qualidade assistida: sem configuração Sonar específica no POM deste módulo, usados compilação, testes, build e inspeção dos artefatos da correção. Nenhum novo percentual de cobertura declarado: JaCoCo continua avisando sobre instrumentação de FotoPanacheStore. Avisos H2Dialect redundante e compartilhamento de classes da JVM não impediram testes. Correção sem duplicação de lógica; migração com transação, lock e backup; nenhum arquivo secreto incluído. O aviso de cobertura é limitação da medição, não uma falha funcional escondida.

Auditoria atual: `docs/security-audit/relatorio-011-corrigir-upload-postgresql.pdf`, gerado por `python docs/security-audit/gerar_relatorio.py --change-011` (saída 0), uma página com texto extraído e renderização PyMuPDF inspecionada (saída 0). Sem achados novos confirmados no escopo corretivo; limitação preexistente de API sem autenticação permanece explícita. Inspeção de SQL, mapeamento, escopo test e isolamento H2, sem credenciais externas em evidências. Frontend não alterado pela 011; não houve nova auditoria integral da galeria.

## Ambiente

Windows, Java 17.0.11, Maven 3.8.8 (JAVA_HOME e MAVEN_HOME temporários), Quarkus 3.2.10.Final, PostgreSQL 15 em container isolado `fotogaleria-regressao-011`, porta 55439, banco `fotogaleria_regressao`. Nenhum teste automatizado utiliza o banco do usuário.

## VAL-001 — Reprodução antes da correção

`mvn test -Dtest=FotoUploadPostgresqlTest`: saída 1. POST multipart retornou 201; GET de listagem retornou 500, esperado 200. Stack trace reproduziu `Unable to access lob stream` e erro de Large Objects em auto-commit. Essa é a regressão que testes Mockito não detectavam.

## VAL-002 — Após remoção de @Lob

`mvn verify`: saída 0. 14 testes executados (13 unitários existentes e 1 integração HTTP), sem falhas, erros ou skips; empacotamento Quarkus concluído. Teste verifica PNG válido, POST 201, GET de lista 200 sem imagemBase64, GET de detalhe 200 com Data URL idêntico e DELETE 204 da foto sintética. Cada operação ocorre em uma requisição independente.

Inventário Java alterado de produção: somente FotoEntity, sem nova lógica, apenas mapeamento declarativo. Verificação por integração real; testes unitários existentes preservados. JaCoCo reportou incompatibilidade de dados instrumentados para FotoPanacheStore; por isso não se declara novo percentual de cobertura. Avisos adicionais: dialect PostgreSQL redundante e aviso da JVM sobre compartilhamento de classes.

## VAL-003 — Migração

`scripts/corrigir-fotogaleria-lob.sql` aplicado com `psql -v ON_ERROR_STOP=1` ao banco isolado: saída 0, uma referência sintética convertida. Segunda execução: saída 0 e UPDATE 0. Consulta de comparação confirmou Data URL recuperado e conteúdo original acessível via backup/LOB (ambos true).

Aplicado ao banco local `fotogaleria` no container `postgres_db`, previamente inspecionado: saída 0, uma foto convertida, uma linha em foto_imagem_lob_backup, nenhum Large Object apagado. SQL usa transação e lock; não faz exclusão de fotos. Backup contém dados locais e não é exportado para o repositório.

## VAL-004 — Aplicação local

`Invoke-RestMethod http://localhost:2000/api/fotos?page=0&size=12` e consulta do id retornado: saída 0. Listagem com uma foto e detalhe com Data URL válido, sem imprimir imagem, credenciais ou metadados pessoais. Quarkus dev recarregou a correção.

## Repetir testes

Configuração atual: H2 em memória, por orientação do solicitante e estratégia compartilhada. Configurar Java/Maven e executar `mvn verify` em apps/backend/fotogaleria. Não é mais necessário iniciar o container de regressão. O teste foi renomeado para FotoUploadHttpTest. VAL-001 a VAL-004 preservam as evidências históricas de PostgreSQL.

O arquivo src/test/resources/application.properties aponta exclusivamente para jdbc:h2:mem:fotogaleria_test e usa drop-and-create. Dependência quarkus-jdbc-h2 com scope test. Configuração principal permanece PostgreSQL via POSTGRES_URL, fallback localhost:5432/fotogaleria.

## VAL-005 — Correção dos testes contínuos

Log do usuário: falha em Test runner thread ao acessar 55439, não falha de inicialização do perfil dev. Tentativa Dev Services: mvn test falhou ao descobrir ambiente Docker; substituída por H2 conforme solicitação. mvn clean verify: saída 1 porque fotogaleria-dev.jar estava em uso pela aplicação. Verificação sem clean: `mvn verify -Dtest=FotoUploadHttpTest,FotoServiceTest,FotoRepositoryPanacheTest`, 14 testes aprovados com H2 (zero falhas/erros/skips). Aviso JaCoCo de FotoPanacheStore permanece, sem alegação de cobertura nova. Reiniciar quarkus:dev para recarregar a dependência H2 e os testes contínuos.

## Teste manual

Recarregar a galeria, conferir a foto recuperada, enviar outra imagem e abrir o detalhe. Se o navegador mantiver conteúdo antigo, recarregar a página. Não é necessário apagar banco, volume ou fotos. SPEC anterior arquivada preservada; decisão @Lob foi substituída pela SPEC desta Change.
