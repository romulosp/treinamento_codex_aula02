# Plano de implementação — 10012

Após REV-001: alterar somente iniciar-saque-cartao.ps1, preparar_emulador.ps1 e
testar_bridge_pinpad.bat; documentação/memória. Sem fonte Go/Kotlin/dependências
novo, portanto sem regeneração AAR ou ampliação de testes de protocolo/UI.

Ramo ProvisionOnly compartilha código de importação/hash já utilizado pelo
launcher completo; remove pré-condições COM/toolchain e efeitos Bridge/Activity
apenas nesse ramo. Fontes padrão são paths externos; outputs privados não são
conteúdo de Git/APK. ValidateOnly permanece sem efeitos.

Testes: parser PIN sintético da 10011, análise AST dos scripts; integração ADB
em host já instalado: três hashes, ValidateOnly inválido sem mutação, falha
mutante invalida PIN e restauração válida em finally. Helper com/sem switch;
comparar listener PID e contagem de eventos ABECS antes/depois. Não executar
BAT inteiro, pois criaria segundo listener. Sintaxe completa/estrutura revisada.
