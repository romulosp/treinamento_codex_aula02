# Evidências técnicas locais — 2026-10-10

Resultado da retomada: implementação para avaliação humana; detalhes,
comandos, ambiente, resultados e limitações em [validation.md](../validation.md).

- [Capturas das cinco etapas e janela compacta](layout/cinco-etapas.png).
- Capturas individuais: `layout/layout-10004/01-valores.png` até
  `05-etapa.png`; `06-compacto-fonte150.png` mostra a ação após rolagem.
- `technical/`: logs finais da revisão anterior à REV-007, XML de 19 testes
  host, relatório JaCoCo (181/188 linhas), cobertura Go e `OK (5 tests)` da
  UI/parser. Os resultados vigentes estão em `rev-008/`.

Os artefatos binários, XML e logs são locais e ignorados pela política de
`.gitignore`; este índice e validation.md são os registros textuais. Dados de
perfil/valores usados em capturas e testes são sintéticos. Nenhum desses
artefatos comprova pinpad físico, cartão, chave PIN ou autorização financeira.

O AAR utilizado tem SHA-256
`93545aea6c3e2d6711f78975947795d8cfa6124f697eb673953e79ff52b9d0aa`;
suas fontes de produção, comando e ABIs constam da proveniência junto ao AAR.

## Execução humana preparada

Na pasta `apps/frontend/smartphone/sistema-prototipo-android`, executar:

```powershell
.\scripts\iniciar-saque-cartao.ps1 `
    -PerfilPath '<perfil DES aprovado do laboratório>' `
    -TabelaPath 'D:\desenvolvimento\chave_des\emv_des.json' `
    -PortaPinpad '<COM física do laboratório>'
```

Perfil e COM precisam de valores reais do laboratório. O launcher valida
perfil, AAR/hash, toolchain e emulador; inicia Bridge oculto e exige PBRG/Ping
antes de provisionar e abrir o host. Falha impede abertura automática.
Login e seleção de `Outros Serviços > Saque Cartão` continuam obrigatórios.
O registro de processo impresso permite encerramento explícito:

```powershell
.\scripts\encerrar-bridge-saque.ps1 -ProcessRecord '<registro impresso pelo launcher>'
```

O encerramento confere PID, executável e horário da instância antes de parar
o processo. Não procura nem encerra outros Bridges.


REV-009: conjunto atual em [01-valores](layout/layout-10004/01-valores.png) e
[edição sem perfil padrão](layout/layout-10004/07-primeira-tela-sem-perfil.png);
logs de teste, build e cobertura em `rev-009/`. O indicador de etapas não aparece
em nenhuma tela. Conjuntos REV-008 e REV-007 preservados em `rev-008/` e
`rev-007/`; a montagem cinco-etapas continua histórica da REV-006. Hashes
vigentes e anteriores em [capturas-sha256.md](capturas-sha256.md).
