# ADR-001 — detalhamento observacional no Bridge

Data: 2026-10-15. Decisão: aceita pela revisão 3 da SPEC.

O caminho real usa testar_bridge_pinpad.bat e não o utilitário local. O desenho anterior deixou esse caminho sem integração. Manter serial.SetOpaqueTrace(true), eventos, wire protocol e regras de ownership. Envolver o transporte em observador do pacote já encaminhado, no pacote logging. O observador conhece apenas enquadramento e catálogo de diagnóstico; não executa comandos nem reconstrói transações. A proteção exige validação integral antes de qualquer hexdump e buffers limitados, sem recuperação por busca de SYN em conteúdo inválido.

A composição do Bridge seleciona LEGACY ou DETAILED. O BAT de teste usa DETAILED como default explícito de laboratório; executável isolado conserva LEGACY por padrão. Formato inválido falha na inicialização. Produção não é habilitada automaticamente.

Exceção à restrição anterior de Bridge sem interpretação: apenas observabilidade; não há mudança de semântica no servidor ou ABI Android. Copiar bytes privados temporariamente é necessário para enquadrar chunks; cópias são apagadas após processamento e Close. Nenhum payload protegido entra no formatador.