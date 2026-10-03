# Design: 10003-menu-negocio-dinamico

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Decisões

1. **Compatibilidade com API 1.1.** `BusinessMenuItem` passa a carregar filhos e mantém um construtor de quatro campos para preservar plugins compilados contra a API 1.1. Plugins de negócio novos precisam implementar `getCaminhoMenu`. A major permanece 1; a Change 10007 eleva posteriormente a minor do host para 2 e mantém plugins `business-menu` com minor requerida 1.
2. **Descritor de serviço explícito.** Android não faz descoberta automática de implementações em DEX. O host abre a entrada ZIP `META-INF/services/<FQCN>` diretamente, valida linhas de nome de classe e instancia apenas as classes declaradas por um `DexClassLoader` isolado. Isso realiza o contrato de ServiceLoader sem varredura reflexiva e sem depender da disponibilidade de recursos de `java.util.ServiceLoader` em APK externo.
3. **Reuso da fronteira de confiança.** Assets debug e APKs recebidos no inbox passam pelo mesmo staging, quarentena, validação e promoção da Change 10002. Somente o artefato privado promovido chega ao `DexClassLoader`.
4. **Sem hot unload de DEX.** `PluginHandle` perde referências cooperativamente na troca de sessão/falha; o runtime decide a coleta do classloader.

## Arquitetura e componentes

```text
APK privado verificado (N)
        | ZIP: META-INF/services/<IPluginNegocioApp>
        v
BusinessPluginDiscovery -- DexClassLoader por APK --> PluginHandle(s)
        | getCaminhoMenu() + geração ativa
        v
MenuPathValidator -> MenuTreeBuilder -> List<BusinessMenuItem>
        | fatal                            |
        v                                  v
Modal + log                          BusinessPluginManager -> Compose UI
```

- `BusinessPluginDiscovery`: localiza o descritor de serviço em cada arquivo, cria handles e converte erro técnico em falha isolada.
- `MenuPathValidator`: única fonte da gramática, normalização e ID de caminho.
- `MenuTreeBuilder`: detecta colisão de folhas, unifica apenas ancestrais com chave idêntica e converte recursivamente para itens ordenados.
- `BusinessPluginManager`: possui executor/escopo, geração de cancelamento e
  publica resultado somente quando a sessão ainda é válida. Esta Change não
  define timeout temporal para callbacks de plugin.
- `MainActivity`: exibe a árvore, estado vazio ou modal fatal e não faz IO.

## Alternativas e consequências

- Manter a lista plana foi rejeitado: ela não representa agrupadores exigidos.
- Varredura de todas as classes foi rejeitada por custo, fragilidade e por ser proibida pela especificação.
- `java.util.ServiceLoader` direto foi rejeitado como detalhe de implementação: a entrada de serviço é mantida como contrato, mas a leitura explícita do ZIP torna a descoberta verificável para APKs DEX isolados.
