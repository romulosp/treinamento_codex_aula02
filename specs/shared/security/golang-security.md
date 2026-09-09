# Segurança para Golang

## Escopo

Esta orientação se aplica a aplicações Go geradas, APIs, configuração, dependências e integrações que estejam no escopo da SPEC. A auditoria deve delimitar pontos de entrada, identidade, autorização, persistência e artefatos realmente existentes.

## Entrada e injeção

- Valide entradas na fronteira HTTP, CLI ou mensageria.
- Use consultas parametrizadas e não concatene entrada em SQL.
- Não construa comandos de sistema, caminhos ou expressões com entrada sem validação estrita.
- Faça escaping e serialização segura na saída; não trate conteúdo recebido como HTML confiável.
- Defina limites de tamanho, timeout e paginação para entradas controladas pelo cliente.

## Autenticação, autorização e isolamento

Autenticação e autorização devem ser verificadas no servidor para cada rota protegida. Guards de interface ou headers controlados pelo cliente não substituem identidade validada.

Para cada identificador recebido, confirme que a consulta e a escrita preservam o escopo de usuário ou tenant autorizado. Listar, buscar, atualizar e excluir devem aplicar esse filtro no repositório quando o domínio exigir isolamento.

## Segredos e dados sensíveis

Credenciais, tokens, chaves e valores sensíveis devem vir do ambiente seguro ou do provedor apropriado. Não versionar segredos e não registrá-los em logs, testes, exemplos, relatórios ou mensagens de erro. Durante a auditoria, redigir imediatamente qualquer valor encontrado e diferenciar estado atual de histórico Git.

## Erros e logs

Respostas públicas não devem conter stack trace, SQL, token, caminho interno ou dados pessoais desnecessários. Logs estruturados devem usar correlação, níveis adequados e redaction. Erros internos devem manter contexto técnico somente no limite confiável.

## Dependências e runtime

Fixe e revise dependências no `go.mod`/`go.sum`, prefira bibliotecas mantidas e execute verificações de vulnerabilidade disponíveis. Configure timeouts, cancelamento de contexto, limites de conexão e encerramento controlado.

## Evidências da auditoria

A validação deve registrar:

- escopo e artefatos inspecionados;
- entradas e superfícies de confiança existentes;
- categorias conformes e não aplicáveis;
- comandos de busca e versões relevantes;
- achados confirmados com caminho e linha, sem segredo;
- limitações e recomendações priorizadas.

A ausência de evidência não é prova de segurança. Para o fluxo Spec Driven, execute a Skill `security-audit` quando houver artefato técnico; em uma Change exclusivamente documental, registre a não aplicabilidade em `validation.md` e não reutilize PDF histórico.
