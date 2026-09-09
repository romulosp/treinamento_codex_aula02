# Plano de implementação: 035-skill-golang

## Escopo autorizado

- Criar a Skill principal `backend-golang`.
- Separar os 30 blocos textuais do anexo em Skills Go independentes conforme `skill-import-manifest.md`.
- Criar a documentação compartilhada de arquitetura, REST, testes, segurança e convenções.
- Criar templates e exemplos documentais reutilizáveis para os perfis `api` e `desktop`.
- Atualizar o catálogo local de Skills.
- Não importar arquivos auxiliares ausentes no anexo e não criar dependência Java.

## Estratégia

1. Validar colisões nos destinos `.agents/skills/`.
2. Separar cada bloco pela posição do identificador e do marcador `Cópia`.
3. Gravar UTF-8 em `.agents/skills/<destino>/SKILL.md`, preservando o conteúdo Markdown.
4. Criar `backend-golang/SKILL.md` conforme a SPEC.
5. Criar os documentos compartilhados e os READMEs de templates e exemplos.
6. Atualizar `.agents/skills/README.md` com os 30 itens importados e a Skill principal.

## Testes e evidências

- Contar os 30 blocos na origem.
- Confirmar os 30 diretórios de destino e a presença de um `SKILL.md` em cada um.
- Comparar identificadores e hashes/conteúdo normalizado entre origem e destino.
- Verificar ausência de colisões e de arquivos auxiliares inventados.
- Validar Markdown e obter erros do editor para os documentos alterados.
- Confirmar que não há código Go, módulo ou dependência executável introduzida pela Change.

## Qualidade e segurança

A importação é documental e não executa código Go, instala dependências ou acessa serviços externos. A Skill `security-audit` será aplicável ao conjunto textual para confirmar ausência de segredos; SonarQube não é aplicável à ausência de código executável e isso será registrado na validação.

## Riscos

- Títulos descritivos em português podem não coincidir com identificadores operacionais; o manifesto fixa o mapeamento.
- O anexo contém referências a arquivos auxiliares não fornecidos; elas permanecerão como texto e não serão materializadas.
- A origem pode conter blocos duplicados semanticamente; cada bloco será preservado como destino distinto conforme o manifesto.
