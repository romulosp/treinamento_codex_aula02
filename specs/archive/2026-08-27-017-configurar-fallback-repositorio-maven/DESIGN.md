# Design: 017-configurar-fallback-repositorio-maven

## Decisões

1. O `pom.xml` não escolhe repositório: a escolha pertence a arquivos `.mvn/settings-*.xml` e ao script de sessão.
2. O script usa `Test-NetConnection` para verificar o host e a porta do Nexus antes de chamar Maven no modo padrão.
3. Os modos são `nexus`, `public` e automático (padrão). No automático, somente indisponibilidade de rede leva diretamente ao público.
4. Uma falha Maven após usar Nexus não é automaticamente classificada como rede; o script solicita/expõe a tentativa pública para preservar erros de build.

## Fluxo

```text
modo informado?
  nexus  -> settings-nexus.xml
  public -> settings-public.xml
  padrão -> Nexus acessível? -> Nexus : Maven Central
```

## Consequências

- Separar configurações evita que um mirror `*` do Nexus bloqueie o acesso ao Maven Central fora da VPN.
- O fallback não torna Maven Central uma fonte corporativa nem substitui a governança do Nexus quando disponível.
