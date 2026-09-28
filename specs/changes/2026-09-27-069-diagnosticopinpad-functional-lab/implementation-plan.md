# Implementation Plan — 069 diagnosticopinpad Functional Lab

## Ordem

1. Fixar contrato da fachada e criar testes Go de resumo/redaction.
2. Implementar a fachada em arquivo separado para limitar regressão no cliente
   mínimo da 068.
3. Regenerar o AAR e confirmar assinaturas Java geradas.
4. Expandir repository/ViewModel e depois a tela por grupos.
5. Executar gates Go, Android e Emulator; corrigir uma falha por vez.

## Riscos

- assinaturas gomobile com listas e muitos parâmetros: usar strings primitivas e
  JSON somente em retornos nomeados;
- operações sensíveis: nunca transportar resultado bruto para Kotlin;
- formulário GOX/FCX/GTK: manter valores efêmeros, validar no Go e registrar
  somente códigos/status;
- tamanho da tela: usar diálogos e seções recolhíveis, sem criar novo módulo.

## Qualidade

Todo comportamento novo terá teste observável. O AAR e o APK permanecerão
artefatos locais ignorados; apenas metadados e comandos serão registrados.
