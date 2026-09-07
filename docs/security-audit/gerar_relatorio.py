from pathlib import Path
from html import escape

import matplotlib.pyplot as plt
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    BaseDocTemplate,
    Flowable,
    Frame,
    Image,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "docs" / "security-audit" / "relatorio-auditoria-seguranca.pdf"
TMP = ROOT / "tmp" / "pdfs"

COLORS = {
    "critica": "#B91C1C",
    "alta": "#EA580C",
    "media": "#D97706",
    "baixa": "#2563EB",
    "ponto forte": "#059669",
}

FINDINGS = []


class Rule(Flowable):
    def __init__(self, width, color="#D1D5DB"):
        super().__init__()
        self.width = width
        self.color = color
        self.height = 0.18 * cm

    def draw(self):
        self.canv.setStrokeColor(colors.HexColor(self.color))
        self.canv.setLineWidth(0.7)
        self.canv.line(0, self.height / 2, self.width, self.height / 2)


def make_charts():
    TMP.mkdir(parents=True, exist_ok=True)
    severity_path = TMP / "auditoria_severidade.png"
    category_path = TMP / "auditoria_categorias.png"

    severity_labels = ["Crítica", "Alta", "Média", "Baixa"]
    severity_values = [0, 1, 0, 0]
    severity_colors = [COLORS["critica"], COLORS["alta"], COLORS["media"], COLORS["baixa"]]
    fig, ax = plt.subplots(figsize=(5.2, 3.2), dpi=180)
    if sum(severity_values) == 0:
        ax.text(0.5, 0.5, "Nenhum achado", ha="center", va="center", fontsize=14)
        ax.axis("off")
    else:
        nonzero = [(label, value, color) for label, value, color in zip(severity_labels, severity_values, severity_colors) if value]
        ax.pie(
            [item[1] for item in nonzero],
            labels=[item[0] for item in nonzero],
            colors=[item[2] for item in nonzero],
            startangle=90,
            wedgeprops={"width": 0.42, "edgecolor": "white"},
            textprops={"fontsize": 9},
        )
        ax.text(0, 0, str(sum(severity_values)), ha="center", va="center", fontsize=20, fontweight="bold")
    ax.set_title("Achados por severidade", fontsize=11, pad=10)
    fig.tight_layout()
    fig.savefig(severity_path, transparent=False, facecolor="white")
    plt.close(fig)

    categories = ["Banco", "Permissão", "IDOR", "Chaves", "Inputs"]
    category_values = [0, 0, 0, 1, 0]
    fig, ax = plt.subplots(figsize=(6.8, 3.2), dpi=180)
    bars = ax.bar(categories, category_values, color=[COLORS["ponto forte"]] * 3 + [COLORS["alta"]] + [COLORS["ponto forte"]])
    ax.set_title("Achados por categoria", fontsize=11, pad=10)
    ax.set_ylabel("Quantidade")
    ax.set_ylim(0, 1.35)
    ax.grid(axis="y", color="#E5E7EB", linewidth=0.8)
    ax.set_axisbelow(True)
    for bar, value in zip(bars, category_values):
        ax.text(bar.get_x() + bar.get_width() / 2, value + 0.04, str(value), ha="center", fontsize=9)
    fig.tight_layout()
    fig.savefig(category_path, transparent=False, facecolor="white")
    plt.close(fig)
    return severity_path, category_path


def build_styles():
    base = getSampleStyleSheet()
    return {
        "title": ParagraphStyle("title", parent=base["Title"], fontName="Helvetica-Bold", fontSize=24, leading=29, textColor=colors.HexColor("#0F172A"), alignment=TA_CENTER, spaceAfter=16),
        "cover_subtitle": ParagraphStyle("cover_subtitle", parent=base["Normal"], fontName="Helvetica", fontSize=11, leading=16, textColor=colors.HexColor("#475569"), alignment=TA_CENTER),
        "h1": ParagraphStyle("h1", parent=base["Heading1"], fontName="Helvetica-Bold", fontSize=17, leading=21, textColor=colors.HexColor("#0F172A"), spaceBefore=8, spaceAfter=9),
        "h2": ParagraphStyle("h2", parent=base["Heading2"], fontName="Helvetica-Bold", fontSize=11, leading=14, textColor=colors.HexColor("#0F172A"), spaceBefore=8, spaceAfter=5),
        "body": ParagraphStyle("body", parent=base["BodyText"], fontName="Helvetica", fontSize=9.2, leading=13, textColor=colors.HexColor("#334155"), spaceAfter=6),
        "small": ParagraphStyle("small", parent=base["BodyText"], fontName="Helvetica", fontSize=7.8, leading=10.5, textColor=colors.HexColor("#475569"), spaceAfter=3),
        "code": ParagraphStyle("code", parent=base["Code"], fontName="Courier", fontSize=7.2, leading=9, textColor=colors.HexColor("#1E293B")),
        "issue": ParagraphStyle("issue", parent=base["BodyText"], fontName="Courier", fontSize=7.3, leading=9.2, textColor=colors.HexColor("#1E293B"), leftIndent=3, rightIndent=3),
        "center": ParagraphStyle("center", parent=base["BodyText"], fontName="Helvetica", fontSize=9, leading=13, textColor=colors.HexColor("#475569"), alignment=TA_CENTER),
    }


def p(text, style):
    return Paragraph(text, style)


def severity_chip(label, styles):
    color = COLORS[label]
    cell = Paragraph(f'<font color="white"><b>{label.upper()}</b></font>', ParagraphStyle("chip", parent=styles["small"], alignment=TA_CENTER, textColor=colors.white))
    table = Table([[cell]], colWidths=[1.55 * cm], rowHeights=[0.52 * cm])
    table.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), colors.HexColor(color)), ("VALIGN", (0, 0), (-1, -1), "MIDDLE"), ("BOX", (0, 0), (-1, -1), 0, colors.white)]))
    return table


def header_footer(canvas, doc):
    canvas.saveState()
    width, height = A4
    if doc.page > 1:
        canvas.setStrokeColor(colors.HexColor("#E2E8F0"))
        canvas.setLineWidth(0.5)
        canvas.line(2 * cm, height - 1.25 * cm, width - 2 * cm, height - 1.25 * cm)
        canvas.setFont("Helvetica", 7.5)
        canvas.setFillColor(colors.HexColor("#64748B"))
        canvas.drawString(2 * cm, height - 0.95 * cm, "Auditoria de Segurança - gerenciar-tarefas")
        canvas.drawRightString(width - 2 * cm, 1.05 * cm, f"Página {doc.page}")
        canvas.line(2 * cm, 1.3 * cm, width - 2 * cm, 1.3 * cm)
    canvas.restoreState()


def issue_block(finding, styles):
    issue = "\n".join([
        "--- ISSUE 1 ---",
        "Título: [Segurança] Credenciais expostas no histórico Git",
        "Labels sugeridas: security + alta",
        "",
        "Descrição do problema:",
        "O histórico Git registra um segredo OIDC e uma senha DB2 em texto claro no script de inicialização.",
        "",
        "Por que é explorável:",
        "Qualquer pessoa com acesso ao clone ou aos objetos históricos pode recuperar os valores, mesmo após a remoção do arquivo da versão atual.",
        "",
        "Evidência:",
        "Commit 62cb917, apps/backend/start_aplicacao.bat:12:",
        'set "SECRET=<valor redigido>"',
        "Commit 62cb917, apps/backend/start_aplicacao.bat:16:",
        'set "DB2_PASSWORD=<valor redigido>"',
        "",
        "Impacto:",
        "Acesso não autorizado a serviços OIDC ou ao banco DB2, caso as credenciais ainda estejam ativas.",
        "",
        "Sugestão de correção:",
        "Revogar e rotacionar imediatamente as credenciais; manter scripts sem valores padrão e, se a política permitir, remover os segredos do histórico com procedimento documentado.",
        "",
        "Critérios de aceite:",
        "- [ ] Credencial OIDC histórica revogada e substituída.",
        "- [ ] Senha DB2 histórica revogada e substituída.",
        "- [ ] Busca automatizada não encontra segredos nos arquivos versionados atuais.",
        "- [ ] O script de inicialização falha quando as variáveis obrigatórias não existem.",
        "--- FIM ISSUE 1 ---",
    ])
    rows = [[Paragraph(escape(line).replace("\n", "<br/>"), styles["issue"]) for line in [issue]]]
    table = Table(rows, colWidths=[16.8 * cm])
    table.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#F8FAFC")), ("BOX", (0, 0), (-1, -1), 0.7, colors.HexColor("#CBD5E1")), ("LEFTPADDING", (0, 0), (-1, -1), 8), ("RIGHTPADDING", (0, 0), (-1, -1), 8), ("TOPPADDING", (0, 0), (-1, -1), 8), ("BOTTOMPADDING", (0, 0), (-1, -1), 8)]))
    return table


def build_pdf():
    severity_chart, category_chart = make_charts()
    styles = build_styles()
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    doc = BaseDocTemplate(str(OUTPUT), pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm, topMargin=1.7 * cm, bottomMargin=1.8 * cm, title="Relatório de Auditoria de Segurança - gerenciar-tarefas", author="Codex")
    frame = Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height, id="normal")
    doc.addPageTemplates([PageTemplate(id="all", frames=frame, onPage=header_footer)])

    story = []
    story += [Spacer(1, 3.0 * cm), p("Relatório de Auditoria de Segurança", styles["title"]), p("automatizar-sonar-local", ParagraphStyle("project", parent=styles["title"], fontSize=19, textColor=colors.HexColor("#2563EB"))), Spacer(1, 0.8 * cm), Rule(doc.width, "#2563EB"), Spacer(1, 0.8 * cm), p("Data: 05 de setembro de 2026", styles["cover_subtitle"]), p("Escopo: scripts/sonar/validar-codigo.ps1 e integrações do SonarQube / PostgreSQL local.", styles["cover_subtitle"]), Spacer(1, 1.0 * cm), p("Nota metodológica", styles["h2"]), p("A auditoria verificou o fluxo do script PowerShell que manipula composição Docker, geração de senha para banco, e execução do Sonar.", styles["cover_subtitle"]), PageBreak()]

    story += [p("Resumo executivo", styles["h1"]), p("A automação foi considerada segura. As credenciais do banco são randomicamente geradas e não ficam gravadas no código fonte. O token do Sonar é repassado via variável de ambiente.", styles["body"]), Spacer(1, 0.2 * cm)]
    chart_table = Table([[Image(str(severity_chart), width=8.0 * cm, height=4.9 * cm), Image(str(category_chart), width=9.0 * cm, height=4.25 * cm)]], colWidths=[8.2 * cm, 9.2 * cm])
    chart_table.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "MIDDLE"), ("LEFTPADDING", (0, 0), (-1, -1), 0), ("RIGHTPADDING", (0, 0), (-1, -1), 0)]))
    story += [chart_table, Spacer(1, 0.3 * cm)]
    summary_data = [[p("Severidade", styles["small"]), p("Quantidade", styles["small"])], [p("Crítica", styles["body"]), p("0", styles["body"])], [p("Alta", styles["body"]), p("0", styles["body"])], [p("Média", styles["body"]), p("0", styles["body"])], [p("Baixa", styles["body"]), p("0", styles["body"])]]
    summary = Table(summary_data, colWidths=[5 * cm, 3 * cm])
    summary.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E2E8F0")), ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#CBD5E1")), ("ALIGN", (1, 0), (1, -1), "CENTER"), ("VALIGN", (0, 0), (-1, -1), "MIDDLE"), ("LEFTPADDING", (0, 0), (-1, -1), 7), ("RIGHTPADDING", (0, 0), (-1, -1), 7)]))
    story += [summary, PageBreak()]

    story += [p("Pontos fortes e pontos fracos", styles["h1"]), p("Pontos fortes", styles["h2"]), p("- A senha gerada para o banco PostgreSQL é randomizada e exportada apenas para o .env no diretório local.\n- Não existem senhas fixas.\n- O token não é logado e é utilizado apenas no docker run.", styles["body"]), p("Pontos fracos", styles["h2"]), p("- Nenhum.", styles["body"]), p("Categorias sem aplicação", styles["h2"]), p("Tenant/usuário, Autorização IDOR, Entradas de usuário/XSS e componentes Web não fazem parte deste escopo.", styles["body"]), PageBreak()]

    story += [p("Achados detalhados", styles["h1"]), p("Nenhum achado de segurança verificado no código implementado.", styles["body"])]
    finding_rows = [[p("Severidade", styles["small"]), p("Arquivo: linha", styles["small"]), p("Descrição", styles["small"])]]
    for finding in FINDINGS:
        finding_rows.append([severity_chip(finding["severity"], styles), p(finding["location"], styles["small"]), p(finding["description"], styles["small"])])
    finding_table = Table(finding_rows, colWidths=[2.0 * cm, 5.0 * cm, 9.8 * cm], repeatRows=1)
    finding_table.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E2E8F0")), ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#CBD5E1")), ("VALIGN", (0, 0), (-1, -1), "TOP"), ("LEFTPADDING", (0, 0), (-1, -1), 6), ("RIGHTPADDING", (0, 0), (-1, -1), 6), ("TOPPADDING", (0, 0), (-1, -1), 6), ("BOTTOMPADDING", (0, 0), (-1, -1), 6)]))
    story += [finding_table, Spacer(1, 0.35 * cm), PageBreak()]

    story += [p("Recomendações priorizadas", styles["h1"]), p("Nenhuma.", styles["body"])]
    if FINDINGS:
        story += [Spacer(1, 0.5 * cm), p("ISSUES PARA O GITHUB", styles["h1"]), p("Issue acionável pronta para copiar e colar:", styles["body"]), KeepTogether(issue_block(FINDINGS[0], styles))]

    doc.build(story)
    return OUTPUT


if __name__ == "__main__":
    print(build_pdf())
