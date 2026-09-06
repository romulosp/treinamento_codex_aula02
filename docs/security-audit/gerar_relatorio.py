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


if __name__ == "__main__":
    import sys
    print(build_correction_pdf() if "--change-011" in sys.argv else build_pdf())
