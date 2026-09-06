# Plano de implementação — 002-ajustar-configuracao-produto-base

## Impactos

- `application.properties`: alterar somente `quarkus.http.port` para 1000.
- `vite.config.js`: definir porta 2000 e proxy de `/produtos` para a porta 1000.
- `LoginPage.jsx`: substituir as credenciais demonstrativas por `root` / `root`.
- `start_aplicacao_frontend.bat` e `testar_aplicacao.bat`: refletir as URLs e portas aprovadas.

## Estratégia

1. Ajustar as configurações de execução e o login demonstrativo.
2. Preservar `produtoService.js` com caminho relativo para `/produtos`.
3. Executar o build do Vite e verificações textuais das portas, URL relativa e script batch.

## Qualidade e testes

- O frontend não possui executor de testes automatizados configurado; será executado `npm run build` como verificação disponível e os cenários de aceite serão inspecionados nos arquivos alterados.
- A mudança Java limita-se a configuração declarativa; não altera classe Java nem requer teste unitário Java.

## Segurança

- A auditoria verificará que a autenticação continua explicitamente demonstrativa, que não foram inseridos segredos e que o frontend não utiliza HTML inseguro ou URL de API fixa.

## Riscos

- As portas 1000 e 2000 podem estar ocupadas no ambiente local. A validação registra a configuração e o build, sem iniciar serviços persistentes.
