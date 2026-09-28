# Implementation plan — 071 BAT de teste do módulo Windows

Criar o BAT no módulo Go, atualizar a documentação e validar o conteúdo com
um teste controlado de inicialização. O teste não deve exigir operação ABECS
nem alterar o pinpad físico.

Este plano registra a primeira implementação. A correção da configuração e
do diagnóstico de startup é governada pelo
[plano da 072](../2026-09-27-072-corrigir-abertura-bridge-android/implementation-plan.md),
com gates adicionais; não reutilizar teste de conteúdo como prova de readiness.
