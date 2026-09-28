from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import BaseDocTemplate, Frame, PageBreak, PageTemplate, Paragraph, Spacer, Table, TableStyle

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "docs" / "security-audit" / "relatorio-auditoria-seguranca.pdf"


def build_styles():
    base = getSampleStyleSheet()
    return {
        "title": ParagraphStyle("title", parent=base["Title"], fontName="Helvetica-Bold", fontSize=24, leading=29, textColor=colors.HexColor("#173B3F"), alignment=TA_CENTER),
        "subtitle": ParagraphStyle("subtitle", parent=base["Normal"], fontSize=11, leading=16, textColor=colors.HexColor("#52615D"), alignment=TA_CENTER),
        "h1": ParagraphStyle("h1", parent=base["Heading1"], fontName="Helvetica-Bold", fontSize=17, leading=21, textColor=colors.HexColor("#173B3F"), spaceBefore=8, spaceAfter=10),
        "h2": ParagraphStyle("h2", parent=base["Heading2"], fontName="Helvetica-Bold", fontSize=11, leading=14, textColor=colors.HexColor("#B94F35"), spaceBefore=8, spaceAfter=5),
        "body": ParagraphStyle("body", parent=base["BodyText"], fontSize=9.4, leading=14, textColor=colors.HexColor("#263E40"), spaceAfter=7),
        "small": ParagraphStyle("small", parent=base["BodyText"], fontSize=8, leading=11, textColor=colors.HexColor("#52615D")),
    }


def header_footer(canvas, doc):
    canvas.saveState()
    width, height = A4
    if doc.page > 1:
        canvas.setStrokeColor(colors.HexColor("#C9C0AF"))
        canvas.line(2 * cm, height - 1.25 * cm, width - 2 * cm, height - 1.25 * cm)
        canvas.line(2 * cm, 1.3 * cm, width - 2 * cm, 1.3 * cm)
        canvas.setFont("Helvetica", 7.5)
        canvas.setFillColor(colors.HexColor("#52615D"))
        canvas.drawString(2 * cm, height - 0.95 * cm, "Auditoria de Seguranca - 010-galeria-de-fotos")
        canvas.drawRightString(width - 2 * cm, 1.02 * cm, f"Pagina {doc.page}")
    canvas.restoreState()


def build_pdf():
    style = build_styles()
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    doc = BaseDocTemplate(str(OUTPUT), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm, topMargin=1.7 * cm, bottomMargin=1.8 * cm, title="Auditoria de Seguranca - 010-galeria-de-fotos", author="Codex")
    doc.addPageTemplates([PageTemplate(id="all", frames=Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height), onPage=header_footer)])
    p = lambda text, kind="body": Paragraph(text, style[kind])
    story = [
        Spacer(1, 4 * cm), p("Relatorio de Auditoria de Seguranca", "title"), Spacer(1, 0.35 * cm),
        p("010-galeria-de-fotos", "subtitle"), Spacer(1, 0.8 * cm), p("Data: 06 de setembro de 2026", "subtitle"),
        p("Escopo: backend Quarkus, frontend React/Vite, persistencia PostgreSQL, scripts locais e configuracao externa.", "subtitle"),
        Spacer(1, 1.2 * cm), p("Resultado: sem achados confirmados em aberto. A ausencia deliberada de autenticacao no backend permanece uma limitacao aceita exclusivamente para demonstracao local."),
        PageBreak(), p("Resumo executivo", "h1"),
        p("Foram examinados os cinco endpoints da API, fluxos por identificador, validacoes de upload, renderizacao do frontend, persistencia, configuracoes e automacao local. Nao foram confirmados segredos em artefatos versionados da Change, injecao de consulta, execucao dinamica de codigo ou HTML cru."),
        p("Limitacao de seguranca", "h2"),
        p("A API nao possui autenticacao, autorizacao, usuario, tenant ou isolamento por proprietario. Qualquer cliente com acesso ao ambiente local pode listar, consultar, alterar ou excluir fotos. Essa condicao consta como fora de escopo na SPEC e impede uso em producao."),
        p("Pontos fortes", "h2"),
        p("- Allowlist de JPEG, PNG, GIF e WebP.<br/>- Rejeicao de arquivos vazios ou maiores que 5 MiB.<br/>- Descricao limitada a 5.000 caracteres.<br/>- Entidades JPA nao expostas pela API.<br/>- JSX sem HTML cru ou eval.<br/>- Configuracao selecionada por secao sem impressao de valores.<br/>- BAT preenchido local e ignorado pelo Git."),
        p("Categorias avaliadas", "h1"),
    ]
    rows = [
        [p("Categoria", "small"), p("Conclusao", "small")],
        [p("Autenticacao e autorizacao"), p("Limitacao aceita para demonstracao local; inadequada para producao.")],
        [p("Tenant, usuario e IDOR"), p("Nao aplicavel ao contrato atual, sem identidade ou propriedade; risco coberto pela limitacao acima.")],
        [p("Segredos"), p("Conforme no estado atual: valores ficam no arquivo externo e no BAT local ignorado; evidencias usam dados sinteticos.")],
        [p("Entrada e upload"), p("Conforme: allowlist de media type, limites de tamanho e nome-base do arquivo.")],
        [p("XSS e injecao"), p("Nenhuma superficie exploravel confirmada na inspecao estatica.")],
    ]
    table = Table(rows, colWidths=[5.1 * cm, 11.7 * cm], repeatRows=1)
    table.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8E1D5")), ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#C9C0AF")), ("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 7), ("RIGHTPADDING", (0, 0), (-1, -1), 7), ("TOPPADDING", (0, 0), (-1, -1), 6), ("BOTTOMPADDING", (0, 0), (-1, -1), 6)]))
    story.extend([table, PageBreak(), p("Achados e recomendacoes", "h1"), p("Nenhum achado de seguranca confirmado permanece em aberto nesta Change."), p("P1 - antes de qualquer publicacao", "h2"), p("Criar uma Change especifica para autenticacao e autorizacao no servidor, vinculando cada operacao ao contexto autenticado. Nao reutilizar o login estatico do navegador como controle de acesso."), p("P2 - endurecimento futuro", "h2"), p("Validar a assinatura real dos bytes de imagem alem do media type declarado e considerar armazenamento de objetos fora do banco para reduzir impacto operacional de payloads Base64."), p("Metodologia e evidencias", "h1"), p("A auditoria combinou busca estatica por segredos e APIs perigosas, leitura dos fluxos REST e Fetch, verificacao dos limites de entrada, revisao do gerador de configuracao e confirmacao de que o BAT preenchido e ignorado. Valores externos foram redigidos e nao integram este relatorio."), p("Comandos relacionados: mvn verify; npm test -- --run; npm run build; gerador com secao valida e secao ausente.", "small")])
    doc.build(story)
    return OUTPUT


def build_correction_pdf():
    """Produz a auditoria específica da correção 011, preservando o relatório 010."""
    from reportlab.platypus import SimpleDocTemplate
    output = OUTPUT.with_name("relatorio-011-corrigir-upload-postgresql.pdf")
    styles = build_styles()
    paragraphs = [
        ("Auditoria de segurança", "title"),
        ("011-corrigir-upload-postgresql - 06/09/2026", "subtitle"),
        ("Escopo e método", "h1"),
        ("Inspeção dos artefatos restaurados: FotoEntity.java:32-34, pom.xml (H2 e Rest Assured no escopo test), src/test/resources/application.properties, FotoUploadHttpTest.java e scripts/corrigir-fotogaleria-lob.sql. Auditoria estática delimitada à correção; não substitui pentest ou auditoria de toda a aplicação.", "body"),
        ("Persistência e migração", "h1"),
        ("Mapeamento String/TEXT sem @Lob. Migração SQL: BEGIN/COMMIT, lock da tabela, backup do id e referência antiga antes de UPDATE, preservação dos Large Objects. Prefixo de imagem validado; falha de leitura ou prefixo inválido aborta a transação. SQL não recebe comandos concatenados de entrada HTTP. Backups permanecem locais e contêm dados da aplicação.", "body"),
        ("Isolamento e credenciais", "h1"),
        ("H2 em memória exclusivo para testes, com recriação de esquema restrita à configuração de teste. Aplicação usa PostgreSQL e variáveis externas. Nenhuma credencial externa copiada para a correção ou relatório. A busca de histórico do novo SQL não retorna commits anteriores; BAT preenchido continua ignorado pelo Git.", "body"),
        ("Limitações e resultado", "h1"),
        ("Nenhum novo achado de segurança confirmado nos artefatos desta correção. Autenticação, autorização, isolamento por proprietário e frontend não são alterados. A API continua sem autenticação, limitação conhecida da demonstração local; este relatório não a declara adequada para publicação.", "body"),
        ("Recomendações", "h1"),
        ("Preservar backup e Large Objects até conferir as fotos recuperadas. Não apontar a configuração de teste para o banco de desenvolvimento. Antes de qualquer publicação, implementar controles de acesso em mudança própria. Não existem novos achados confirmados que exijam bloco de issue nesta auditoria.", "body"),
    ]
    SimpleDocTemplate(str(output), pagesize=A4, leftMargin=2*cm, rightMargin=2*cm,
                      topMargin=1.5*cm, bottomMargin=1.5*cm,
                      title="Auditoria 011-corrigir-upload-postgresql").build(
        [Paragraph(text, styles[kind]) for text, kind in paragraphs])
    return output


def build_change_066_pdf():
    """Produz o relatorio atual da Change 066 sem reutilizar auditorias historicas."""
    output = OUTPUT.with_name("relatorio-066-lib-pinpad-abecs-go.pdf")
    styles = build_styles()
    document = BaseDocTemplate(
        str(output), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm,
        topMargin=1.7 * cm, bottomMargin=1.8 * cm,
        title="Auditoria de Seguranca - 066-lib-pinpad-abecs-go", author="Codex",
    )

    def header_footer_066(canvas, doc):
        canvas.saveState()
        width, height = A4
        if doc.page > 1:
            canvas.setStrokeColor(colors.HexColor("#C9C0AF"))
            canvas.line(2 * cm, height - 1.25 * cm, width - 2 * cm, height - 1.25 * cm)
            canvas.line(2 * cm, 1.3 * cm, width - 2 * cm, 1.3 * cm)
            canvas.setFont("Helvetica", 7.5)
            canvas.setFillColor(colors.HexColor("#52615D"))
            canvas.drawString(2 * cm, height - 0.95 * cm, "Auditoria de Seguranca - 066-lib-pinpad-abecs-go")
            canvas.drawRightString(width - 2 * cm, 1.02 * cm, f"Pagina {doc.page}")
        canvas.restoreState()

    document.addPageTemplates([PageTemplate(id="change-066", frames=Frame(document.leftMargin, document.bottomMargin, document.width, document.height), onPage=header_footer_066)])
    paragraph = lambda text, kind="body": Paragraph(text, styles[kind])
    rows = [
        [paragraph("Categoria", "small"), paragraph("Conclusao", "small")],
        [paragraph("Rede, autenticacao e autorizacao"), paragraph("Nao aplicavel: a biblioteca nao expoe listener, API HTTP, usuario ou tenant.")],
        [paragraph("Segredos e dados sensiveis"), paragraph("Conforme na inspecao estatica: nenhum segredo confirmado; payloads de GCX, GTK, GOX, FCX e GPN usam redaction.")],
        [paragraph("Entrada e protocolo"), paragraph("Conforme: limites, CRC, framing, respostas truncadas e cancelamento por contexto possuem validacao e testes automatizados.")],
        [paragraph("Dependencias e adaptador serial"), paragraph("Limitacao: a comunicacao real continua dependente de validacao no pinpad fisico; testes deterministas nao a substituem.")],
    ]
    table = Table(rows, colWidths=[5.1 * cm, 11.7 * cm], repeatRows=1)
    table.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8E1D5")), ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#C9C0AF")), ("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 7), ("RIGHTPADDING", (0, 0), (-1, -1), 7), ("TOPPADDING", (0, 0), (-1, -1), 6), ("BOTTOMPADDING", (0, 0), (-1, -1), 6)]))
    story = [
        Spacer(1, 4 * cm), paragraph("Relatorio de Auditoria de Seguranca", "title"), Spacer(1, 0.35 * cm),
        paragraph("066-lib-pinpad-abecs-go", "subtitle"), Spacer(1, 0.25 * cm),
        paragraph("Data: 11 de setembro de 2026", "subtitle"), Spacer(1, 0.8 * cm),
        paragraph("Escopo: biblioteca Go headless, protocolo ABECS, configuracao por ambiente, adaptador serial, fila, sessao, logging e dados sensiveis do pinpad.", "subtitle"),
        Spacer(1, 1.2 * cm), paragraph("Resultado: nenhum achado de seguranca confirmado em aberto na inspecao estatica. A validacao de comunicacao fisica permanece pendente e nao e substituida por este relatorio."),
        PageBreak(), paragraph("Resumo executivo", "h1"),
        paragraph("A auditoria examinou os pontos de entrada locais, o modulo Go, configuracao, logs, dependencias e tratamento de payloads. A biblioteca nao implementa rede, HTTP, persistencia, autenticacao ou autorizacao; essas categorias foram registradas como nao aplicaveis, e nao como controles existentes."),
        paragraph("Controles observados", "h2"),
        paragraph("O adaptador serial usa contexto e timeout. O protocolo valida CRC, framing e respostas truncadas. Dados de GCX, GTK, GOX, FCX e GPN sao redigidos nos logs estruturados. O codigo e os documentos da Change foram buscados por padroes de segredos e por superficies de rede ou execucao de comandos."),
        paragraph("Categorias avaliadas", "h1"), table,
        PageBreak(), paragraph("Achados e recomendacoes", "h1"),
        paragraph("Nenhum achado de seguranca confirmado exige correcao nesta Change. Nao foi identificado segredo versionado, endpoint de rede, execucao de comando do sistema, consulta SQL ou renderizacao HTML no modulo auditado."),
        paragraph("P1 - validacao operacional", "h2"),
        paragraph("Executar a matriz de comunicacao com pinpad fisico e porta serial real, registrando modelo, firmware, porta, comandos, status e redaction observada. Essa e a unica evidencia pendente para concluir a validacao funcional da Change."),
        paragraph("P2 - concorrencia", "h2"),
        paragraph("Executar go test -race ./... em ambiente Go com arquitetura suportada. A distribuicao windows/386 usada nesta validacao nao suporta o detector de corrida; isso e limitacao de ambiente, nao achado confirmado."),
        paragraph("Metodologia e evidencias", "h1"),
        paragraph("Comandos: go vet ./...; go test ./...; go test ./... -coverprofile=coverage; go tool cover -func=coverage; busca por padroes de segredo, rede e execucao de processo. Cobertura total aferida: 81,7%. Nenhum valor sensivel foi incluido neste relatorio."),
    ]
    document.build(story)
    return output


def build_change_069_pdf():
    """Produz o relatorio atual da Change 069 sem reutilizar auditorias antigas."""
    output = OUTPUT.with_name("relatorio-069-diagnosticopinpad-functional-lab.pdf")
    styles = build_styles()
    document = BaseDocTemplate(
        str(output), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm,
        topMargin=1.7 * cm, bottomMargin=1.8 * cm,
        title="Auditoria de Seguranca - 069-diagnosticopinpad-functional-lab", author="Codex",
    )

    def header_footer_069(canvas, doc):
        canvas.saveState()
        width, height = A4
        if doc.page > 1:
            canvas.setStrokeColor(colors.HexColor("#C9C0AF"))
            canvas.line(2 * cm, height - 1.25 * cm, width - 2 * cm, height - 1.25 * cm)
            canvas.line(2 * cm, 1.3 * cm, width - 2 * cm, 1.3 * cm)
            canvas.setFont("Helvetica", 7.5)
            canvas.setFillColor(colors.HexColor("#52615D"))
            canvas.drawString(2 * cm, height - 0.95 * cm, "Auditoria de Seguranca - 069-diagnosticopinpad-functional-lab")
            canvas.drawRightString(width - 2 * cm, 1.02 * cm, f"Pagina {doc.page}")
        canvas.restoreState()

    document.addPageTemplates([PageTemplate(id="change-069", frames=Frame(document.leftMargin, document.bottomMargin, document.width, document.height), onPage=header_footer_069)])
    paragraph = lambda text, kind="body": Paragraph(text, styles[kind])
    rows = [
        [paragraph("Categoria", "small"), paragraph("Conclusao", "small")],
        [paragraph("Autenticacao e autorizacao"), paragraph("Limitacao aceita somente para laboratorio local; o Bridge deve permanecer em loopback e nao deve ser exposto em rede.")],
        [paragraph("Dados sensiveis"), paragraph("Conforme na inspecao estatica: summaries Go usam allowlist e nao expoem PAN, trilhas, PIN block, KSN, chaves, EMV bruto ou bytes raw.")],
        [paragraph("Entrada"), paragraph("Conforme: Android valida formatos basicos e o core Go mantem validacoes de protocolo, hex, timeout e nomes.")],
        [paragraph("Segredos e logs"), paragraph("Nenhum segredo real encontrado ou incluido nas evidencias; parametros sensiveis nao sao persistidos nem registrados pelo app.")],
        [paragraph("Hardware"), paragraph("Limitacao: EMV, PIN, midia e serial fisica dependem do pinpad real e nao foram declarados como validados pelo Emulator.")],
    ]
    table = Table(rows, colWidths=[5.1 * cm, 11.7 * cm], repeatRows=1)
    table.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8E1D5")), ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#C9C0AF")), ("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 7), ("RIGHTPADDING", (0, 0), (-1, -1), 7), ("TOPPADDING", (0, 0), (-1, -1), 6), ("BOTTOMPADDING", (0, 0), (-1, -1), 6)]))
    story = [
        Spacer(1, 4 * cm), paragraph("Relatorio de Auditoria de Seguranca", "title"), Spacer(1, 0.35 * cm),
        paragraph("069-diagnosticopinpad-functional-lab", "subtitle"), Spacer(1, 0.25 * cm),
        paragraph("Data: 27 de setembro de 2026", "subtitle"), Spacer(1, 0.8 * cm),
        paragraph("Escopo: fachada gomobile Go, aplicativo Android Compose, Bridge Windows, configuracao PORTA_PINPAD, logs e dados sensiveis do pinpad.", "subtitle"),
        Spacer(1, 1.2 * cm), paragraph("Resultado: nenhum achado de seguranca confirmado em aberto. Foi registrada uma limitacao operacional P2 para o listener local sem autenticacao, restrito ao cenario de desenvolvimento."),
        PageBreak(), paragraph("Resumo executivo", "h1"),
        paragraph("A auditoria examinou a fronteira Android-AAR-Bridge, os metodos nomeados do binding, os formularios Compose, os summaries de GCX, GTK, GOX, FCX e GPN, a configuracao por ambiente e o logging. Nao foram encontrados segredos reais, comandos raw no Kotlin ou serializacao de payloads sensiveis na UI."),
        paragraph("Categorias avaliadas", "h1"), table,
        PageBreak(), paragraph("Achados e recomendacoes", "h1"),
        paragraph("SEC-069-001 - Bridge local sem autenticacao", "h2"),
        paragraph("Severidade P2 no contexto de desenvolvimento. O listener deve continuar limitado a 127.0.0.1. Antes de uso fora da maquina local, criar change especifica para autenticacao e transporte protegido. Criterio: nenhuma configuracao de producao deve expor listener nao autenticado fora de loopback."),
        paragraph("Metodologia e evidencias", "h1"),
        paragraph("Foram executados go test ./..., go vet ./..., go build do Bridge, validador estrutural Android, testes JVM, lint e assembleDebug. Foi realizada busca estatica por segredos, APIs perigosas, payloads sensiveis e superficies de rede. Valores de configuracao e dados de cartao nao foram incluidos neste relatorio."),
    ]
    document.build(story)
    return output


def build_change_070_pdf():
    """Produz o relatorio atual da Change 070 sem reutilizar auditorias antigas."""
    output = OUTPUT.with_name("relatorio-070-bridge-log-android.pdf")
    styles = build_styles()
    document = BaseDocTemplate(
        str(output), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm,
        topMargin=1.7 * cm, bottomMargin=1.8 * cm,
        title="Auditoria de Seguranca - 070-bridge-log-android", author="Codex",
    )
    document.addPageTemplates([PageTemplate(id="change-070", frames=Frame(document.leftMargin, document.bottomMargin, document.width, document.height))])
    paragraph = lambda text, kind="body": Paragraph(text, styles[kind])
    story = [
        Spacer(1, 4 * cm), paragraph("Relatorio de Auditoria de Seguranca", "title"),
        paragraph("070-bridge-log-android", "subtitle"), paragraph("Data: 27 de setembro de 2026", "subtitle"),
        Spacer(1, 1 * cm), paragraph("Resultado: nenhum achado de seguranca confirmado. A mudanca centraliza o mesmo tracer e o mesmo arquivo LogPinpadAbecs.txt no Bridge Windows."),
        PageBreak(), paragraph("Escopo e controles", "h1"),
        paragraph("Foram examinados o entrypoint do Bridge, os transportes fisico e scripted, a resolucao de PINPAD_LOG_FILE, a criacao do diretorio e os testes. O Android nao acessa o filesystem Windows e nenhum protocolo ou comando ABECS foi alterado."),
        paragraph("Conforme", "h2"), paragraph("O tracer existente e suas politicas de redaction sao reutilizados. O destino padrao e logs/LogPinpadAbecs.txt na raiz do modulo; PINPAD_LOG_FILE permanece override explicito. Falha de configuracao impede o startup."),
        paragraph("Limitacao", "h2"), paragraph("O Bridge continua local e restrito a loopback. Nao deve ser exposto em rede sem uma change propria de autenticacao e transporte protegido."),
        paragraph("Evidencias", "h1"), paragraph("go test ./...; go vet ./...; go build ./cmd/libpinpadabecsgo-bridge; testes unitarios de destino e criacao do arquivo; revisao estatica sem segredos reais."),
    ]
    document.build(story)
    return output


def build_change_072_pdf():
    """Produz a auditoria atual da correção 072, sem reutilizar relatório histórico."""
    output = OUTPUT.with_name("relatorio-072-corrigir-abertura-bridge-android.pdf")
    styles = build_styles()
    document = BaseDocTemplate(str(output), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm,
                               topMargin=1.7 * cm, bottomMargin=1.8 * cm,
                               title="Auditoria de Segurança - 072-corrigir-abertura-bridge-android", author="Codex")
    document.addPageTemplates([PageTemplate(id="change-072", frames=Frame(document.leftMargin, document.bottomMargin, document.width, document.height))])
    p = lambda text, kind="body": Paragraph(text, styles[kind])
    rows = [
        [p("Categoria", "small"), p("Conclusão", "small")],
        [p("Loopback e superfície de rede"), p("Conforme: Bridge aceita somente 127.0.0.1; não há autenticação porque o contrato é local e de desenvolvimento." )],
        [p("Segredos e dados sensíveis"), p("Conforme na inspeção estática: eventos e transporte opaco redigem payloads; busca delimitada não encontrou segredos reais." )],
        [p("Entrada e comandos"), p("Conforme: porta, host, timeout, ADB e launcher têm validação; comandos ADB usam argumentos separados e seleção explícita." )],
        [p("Android e logs"), p("Conforme: logger privado usa allowlist, tamanho máximo e sanitização; arquivo Windows permanece no processo Bridge." )],
        [p("Hardware"), p("O fluxo físico usou a porta lida de PORTA_PINPAD e comprovou Open, GIX, DSP, Close e reabertura no log. A confirmação visual do DSP pelo operador permanece pendente." )],
    ]
    table = Table(rows, colWidths=[5.1 * cm, 11.7 * cm], repeatRows=1)
    table.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8E1D5")), ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#C9C0AF")), ("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 7), ("RIGHTPADDING", (0, 0), (-1, -1), 7), ("TOPPADDING", (0, 0), (-1, -1), 6), ("BOTTOMPADDING", (0, 0), (-1, -1), 6)]))
    story = [
        Spacer(1, 4 * cm), p("Relatório de Auditoria de Segurança", "title"),
        p("072-corrigir-abertura-bridge-android", "subtitle"), p("Data: 28 de setembro de 2026", "subtitle"),
        Spacer(1, 1 * cm), p("Resultado: nenhum achado de segurança confirmado em aberto. O fluxo técnico físico passou usando exclusivamente a porta herdada de PORTA_PINPAD; a confirmação visual do display permanece um gate funcional."),
        PageBreak(), p("Resumo e controles", "h1"),
        p("Foram examinados o launcher Windows, helper ADB/reverse, Bridge loopback, ownership Windows, framing PBRG, transporte Emulator, fachada gomobile, UI Compose e logs compartilhados. O protocolo continua opaco; a fronteira pública usa categorias estáveis e não ecoa mensagens arbitrárias."),
        p("Categorias avaliadas", "h1"), table,
        PageBreak(), p("Achados, limitações e evidências", "h1"),
        p("Nenhum achado de segurança confirmado exige correção nesta Change. A ausência de autenticação é aceitável somente para o laboratório local restrito a loopback; exposição em rede exigiria Change própria."),
        p("Evidências executadas", "h2"),
        p("go test -tags=integration ./...; go vet ./...; build do Bridge; testes Windows de ownership multiprocesso; testes de launcher/helper com ADB controlado; Gradle testDebugUnitTest, lintDebug e assemble; AAR gomobile gerado com Go amd64 e Java 17; testes instrumentados scripted e físicos no emulator-5554; busca estática delimitada por segredos e APIs perigosas."),
        p("Pendente funcional", "h2"),
        p("Confirmar visualmente no display o texto TESTE ANDROID e a linha HOST seguida da porta efetiva. O valor da porta vem exclusivamente de PORTA_PINPAD; este relatório não fixa uma COM e ainda não afirma a observação visual pelo operador."),
    ]
    document.build(story)
def build_android_native_engineering_pdf():
    """Produz a auditoria da Change 067 sem substituir relatórios anteriores."""
    from reportlab.platypus import SimpleDocTemplate

    output = OUTPUT.with_name("relatorio-067-android-native-engineering.pdf")
    styles = build_styles()
    first_page = [
        ("Auditoria de segurança", "title"),
        ("067-android-native-engineering - 18/09/2026", "subtitle"),
        ("Resultado", "h1"),
        ("Nenhum achado de segurança confirmado permanece em aberto nesta Change.", "body"),
        ("Escopo", "h1"),
        ("Skill local, metadados YAML, referências Markdown, validador Python somente leitura e artefatos Spec Driven da Change. Não há aplicativo Android, API, frontend, autenticação, banco de dados, deploy ou integração com segredos.", "body"),
        ("Superfície e método", "h1"),
        ("A auditoria inspecionou a entrada de caminho do validador, operações de arquivos, diagnósticos, dependências, metadados, links, padrões de segredo, APIs Python perigosas e artefatos gerados. Também distinguiu categorias não aplicáveis de controles efetivamente verificados.", "body"),
        ("Conclusões", "h1"),
        ("Entrada e injeção: conforme no escopo. O caminho é resolvido e usado apenas em operações locais de leitura; não é interpolado em shell, SQL, HTML ou código dinâmico.", "body"),
        ("Segredos: nenhuma atribuição semelhante a senha, segredo, chave ou token foi encontrada nos artefatos atuais. Os caminhos novos ainda não possuem histórico Git. Correspondências históricas em outros caminhos ficaram fora do escopo e nenhum valor foi exposto no relatório.", "body"),
        ("Dependências: o validador usa somente a biblioteca padrão Python. Não foram encontradas chamadas a subprocess, execução dinâmica, desserialização insegura, rede ou escrita no projeto analisado.", "body"),
        ("Autenticação, autorização, tenant, IDOR, XSS, persistência e deploy: não aplicáveis porque essas superfícies não existem nesta Change.", "body"),
    ]
    second_page = [
        ("Controle preventivo Android", "h1"),
        ("A skill proíbe registrar tokens, credenciais, PAN, localização precisa e dados pessoais. O validador reprova a presença de local.properties, e aplicações consumidoras devem revisar Manifest, Intents, permissões, armazenamento, backup e rede conforme sua superfície real.", "body"),
        ("Limitações", "h1"),
        ("A análise é estática e delimitada à Change. Não existe aplicativo consumidor para testar permissões, componentes, rede ou comportamento em dispositivo. Ausência de achados neste escopo não prova a segurança de aplicações futuras.", "body"),
        ("Recomendações", "h1"),
        ("P3 - manter o validador somente leitura e sem comandos derivados da entrada. P3 - repetir auditoria contextual em cada aplicação consumidora. P3 - manter artefatos gerados, local.properties e credenciais fora do versionamento.", "body"),
        ("Evidências", "h1"),
        ("Busca de segredos atuais: nenhuma correspondência. Busca de APIs Python perigosas: nenhuma correspondência. Reparse points: nenhum. O bytecode .pyc produzido pela validação foi removido antes do commit e pode ser regenerado.", "body"),
    ]
    SimpleDocTemplate(
        str(output),
        pagesize=A4,
        leftMargin=2 * cm,
        rightMargin=2 * cm,
        topMargin=1.5 * cm,
        bottomMargin=1.5 * cm,
        title="Auditoria 067-android-native-engineering",
        author="Codex",
    ).build(
        [Paragraph(text, styles[kind]) for text, kind in first_page]
        + [PageBreak()]
        + [Paragraph(text, styles[kind]) for text, kind in second_page]
    )
    return output


if __name__ == "__main__":
    import sys
    if "--change-011" in sys.argv:
        print(build_correction_pdf())
    elif "--change-066" in sys.argv:
        print(build_change_066_pdf())
    elif "--change-069" in sys.argv:
        print(build_change_069_pdf())
    elif "--change-070" in sys.argv:
        print(build_change_070_pdf())
    elif "--change-072" in sys.argv:
        print(build_change_072_pdf())
    if "--change-067" in sys.argv:
        print(build_android_native_engineering_pdf())
    elif "--change-011" in sys.argv:
        print(build_correction_pdf())
    else:
        print(build_pdf())
