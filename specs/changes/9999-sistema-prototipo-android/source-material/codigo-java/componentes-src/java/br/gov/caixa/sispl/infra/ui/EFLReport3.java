/**
 * Criado em: 29/11/2005
 * Ultima modificacao em: 14/12/2005
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;
import java.util.ArrayList;

import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkin;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLScrollPane2;

/**
 * Classe que implementa um Report que permite posuir varios Reports Internos
 * cada um com funcoes diferentes.
 *
 * OBS.: Essa classe foi criada pois os Report existentes nao atendiam as
 * funcionalidades de alguns Casos de Uso, alem de nao possuirem JavaDoc o que
 * dificultava a implementacao das funcionalidades requeridas nos mesmos.
 *
 * @author p543424 (Jadson Jose dos Santos)
 */
public class EFLReport3 extends EFLPanel2 {

	/* para compatibilidade com java 5 */
	private static final long serialVersionUID = 0;

	/* desconta o espaco da sombra que os labels decoradores possuem */
	private static final int ESPACO_SOMBRA = 4;

	/* Tamanhos dos Label Decoradores do cabecalho e do rodape do Report */
	private static final int ALTURA_LABEL_DECORADOR_CABECALHO = 26;
	private static final int ALTURA_LABEL_DECORADOR_RODAPE = 32;

	/* Tamanho da cabecalho e rodape */
	private static final int ALTURA_CABECALHO_REPORT = 28;
	private static final int ALTURA_RODAPE_REPORT = 28;

	/* Labels do cabecalho e rodape do Report */
	private EFLLabel decoradorCabecalho = null;
	private EFLLabel decoradorRodape = null;

	/* Cabecalho do Report */
	private EFLPanel2 painelCabecalho = null;

	/* Painel central que contem os outros paineis */
	private EFLPanel2 panelReport = null;

	/* Painel Rodape do Report */
	private EFLPanel2 painelRodape = null;

	/* A barra de rolagem do Report */
	private EFLScrollPane2 scrollReport = null;

	/* Largura dos Componentes internos do Report */
	private int internalSize = 0;

	/* Lista que quarda os paineis internos */
	private ArrayList paineisInternos = null;

	private boolean isBordaSuperior = true;
	private boolean isBordaInferior = true;

	/**
	 * Construtor
	 * 
	 * @param internalSize
	 */
	public EFLReport3(int internalSize) {
		super();
		setOpaque(false);
		this.setLayout(null);
		this.internalSize = internalSize;
		initialize();
	}

	public boolean isBordaSuperior() {
		return isBordaSuperior;
	}

	public void setBordaSuperior(boolean isBordaSuperior) {
		this.isBordaSuperior = isBordaSuperior;
	}

	public boolean isBordaInferior() {
		return isBordaInferior;
	}

	public void setBordaInferior(boolean isBordaInferior) {
		this.isBordaInferior = isBordaInferior;
	}

	/**
	 * Método que inializa os elementos da interface gráfica do Report.
	 */
	private void initialize() {
		this.add(getDecoradorCabecalho());
		this.add(getDecoradorRodape());

		this.add(getScrollReport2());

	}

	/*******************************************************************************
	 * Cria os Elementos *
	 *******************************************************************************/

	/**
	 * Método que cria o Label que decora o cabeçalho do Report.
	 * 
	 * @return O label criado.
	 */
	private EFLLabel getDecoradorCabecalho() {
		if (decoradorCabecalho == null && isBordaSuperior()) {
			// fixo
			decoradorCabecalho = new EFLLabel(LabelSkin.LABEL_TABELA_CABECALHO_2);
			decoradorCabecalho.setLocation(0, 0);
		} else if (decoradorCabecalho != null && !isBordaSuperior()) {
			decoradorCabecalho = new EFLLabel(LabelSkin.LABEL_FORMULARIO);
			decoradorCabecalho.setLocation(0, 0);
		}
		// variavel
		decoradorCabecalho.setSize(this.getWidth(), ALTURA_LABEL_DECORADOR_CABECALHO);

		return decoradorCabecalho;
	}

	/**
	 * Método que cria o Label que decora o rodapé do Report
	 * 
	 * @return EFLLabel
	 */
	private EFLLabel getDecoradorRodape() {
		if (decoradorRodape == null && isBordaInferior()) {
			decoradorRodape = new EFLLabel(LabelSkin.LABEL_TABELA_RODAPE_2);
		}else if (decoradorRodape != null && !isBordaInferior()) {
			decoradorRodape = new EFLLabel(LabelSkin.LABEL_FORMULARIO);
		}
		// varialvel
		decoradorRodape.setSize(this.getWidth(), ALTURA_LABEL_DECORADOR_RODAPE);
		decoradorRodape.setLocation(0, this.getHeight() - ALTURA_LABEL_DECORADOR_RODAPE);

		return decoradorRodape;
	}

	/**
	 * Metodo que cria a barra de rolagem do Report
	 * 
	 * @return EFLScrollPane2 (a barra de rolagem que contem o painel central do
	 *         Report)
	 */
	private EFLScrollPane2 getScrollReport2() {
		if (scrollReport == null) {

			// TODO: Esse nao eh o Skin que aparece no documento: Design de Telas //
			// No tempo de criacao desse componente nao existia ainda o Skin corrento //

			scrollReport = new EFLScrollPane2(ScrollBarSkin.TEST_BAR);
			scrollReport.setLocation(0, ALTURA_LABEL_DECORADOR_CABECALHO); // começa abaixo do cabecalho
		}
		scrollReport.setViewportView(getPanelReport());

		scrollReport.setPreferredSize(new Dimension((this.getWidth() - ESPACO_SOMBRA),
				this.getHeight() - (ALTURA_LABEL_DECORADOR_CABECALHO + ALTURA_LABEL_DECORADOR_RODAPE)));
		scrollReport.setSize(new Dimension((this.getWidth() - ESPACO_SOMBRA),
				this.getHeight() - (ALTURA_LABEL_DECORADOR_CABECALHO + ALTURA_LABEL_DECORADOR_RODAPE)));

		return scrollReport;
	}

	/**
	 * Metodo que cria o painel central do report
	 * 
	 * @return EFLPanel (o painel central do Report )
	 */
	private EFLPanel2 getPanelReport() {
		if (panelReport == null) {
			panelReport = new EFLPanel2(PanelSkin.PANEL_TABELA);
			panelReport.setLocation(0, ALTURA_LABEL_DECORADOR_CABECALHO); // começa abaixo do cabecalho
			panelReport.setPreferredSize(new Dimension((this.getWidth() - ESPACO_SOMBRA),
					this.getHeight() - (ALTURA_LABEL_DECORADOR_CABECALHO + ALTURA_LABEL_DECORADOR_RODAPE)));
			panelReport.setSize(new Dimension((this.getWidth() - ESPACO_SOMBRA),
					this.getHeight() - (ALTURA_LABEL_DECORADOR_CABECALHO + ALTURA_LABEL_DECORADOR_RODAPE)));
		}

		return panelReport;
	}

	/**
	 * Metodo que calcula a altura do panelReport
	 * 
	 * @return altura (altura do panelReport)
	 */
	private int calculaAlturaPainelCentral() {
		/******************************************************************
		 * Altura painel central = altura cabecalho+ altura rodape + * altura de cada
		 * painel interno *
		 ******************************************************************/
		int altura = 0;
		altura += painelCabecalho != null ? ALTURA_CABECALHO_REPORT : 0;
		altura += painelRodape != null ? ALTURA_RODAPE_REPORT : 0;

		if (paineisInternos != null)
			for (int i = 0; i < paineisInternos.size(); i++) {
				// Altura de cada painel interno
				altura += ((EFLInternalReport) paineisInternos.get(i)).getAltura();
			}
		return altura;
	}

	/**
	 * Calcula a posicao que os paineis internos vao ficar OBS.: Altura total -
	 * Altura do Rodape
	 * 
	 * @return altura (altura do painel interno)
	 */
	private int calculaAlturaPainelInterno() {
		int altura = 0;
		altura += painelCabecalho != null ? ALTURA_CABECALHO_REPORT : 0;

		if (paineisInternos != null)
			for (int i = 0; i < paineisInternos.size(); i++) {
				// Altura de cada painel interno
				altura += ((EFLInternalReport) paineisInternos.get(i)).getAltura();
			}
		return altura;
	}

	/*********************
	 * fim da criacao dos Elementos
	 ****************************/

	/********************************************************************************
	 * Interfaces da Classe *
	 ********************************************************************************/

	/**
	 * Cria o painel cabecalho do Report
	 *
	 */
	public void addPanelCabecalho(EFLPanel2 painelCabecalho) {
		if (painelCabecalho != null) { // todo fixo soh cria panel cabecalho uma vez
			this.painelCabecalho = painelCabecalho;
			painelCabecalho.setSkin(PanelSkin.PANEL_REPORT_TITULO);
			painelCabecalho.setPreferredSize(new Dimension(internalSize, ALTURA_CABECALHO_REPORT));
			painelCabecalho.setSize(new Dimension(internalSize, ALTURA_CABECALHO_REPORT));
			painelCabecalho.setLocation(0, 0);
			painelCabecalho.setLayout(null);

			int alturaPainelCentral = calculaAlturaPainelCentral();

			panelReport.setPreferredSize(new Dimension(internalSize, alturaPainelCentral));
			panelReport.setSize(new Dimension(internalSize, alturaPainelCentral));
			panelReport.add(painelCabecalho);
			painelCabecalho.repaint();
			panelReport.repaint();
		}

	}

	/**
	 * Cria o painel rodape do Report
	 *
	 */
	public void addPanelRodape(EFLPanel2 painelRodape) {
		if (painelRodape != null) {
			this.painelRodape = painelRodape;
			painelRodape.setSkin(PanelSkin.PANEL_REPORT_TITULO);
			painelRodape.setPreferredSize(new Dimension(internalSize, ALTURA_RODAPE_REPORT));
			painelRodape.setSize(new Dimension(internalSize, ALTURA_RODAPE_REPORT));

			painelRodape.setLayout(null);

			panelReport.add(painelRodape);

			int alturaPainelCentral = calculaAlturaPainelCentral();

			panelReport.setPreferredSize(new Dimension(internalSize, alturaPainelCentral));
			panelReport.setSize(new Dimension(internalSize, alturaPainelCentral));

			painelRodape.setLocation(0, panelReport.getHeight() - ALTURA_RODAPE_REPORT);
			painelRodape.repaint();
			panelReport.repaint();
		}

	}

	/**
	 * Metodo que adiciona um painel interno ao painel contral do Report
	 * 
	 * @param painelInterno
	 *            (um painel interno a ser adicionado ao Report)
	 */
	public void addInternalReport(EFLInternalReport painelInterno) {
		/***********************************************************************
		 * Adiciona um painel interno ao painel externo, * o coloca na lista de paineis
		 * internos, atualiza o tamanho do * panelReport e redesenha tudo *
		 ***********************************************************************/

		if (paineisInternos == null)
			paineisInternos = new ArrayList();

		if (panelReport != null) {
			panelReport.add(painelInterno); // adiciona ao painel
			painelInterno.setLocation(0, calculaAlturaPainelInterno());
			paineisInternos.add(painelInterno); // adiciona a lista
			painelInterno.repaint();
			panelReport.setPreferredSize(new Dimension(internalSize, calculaAlturaPainelCentral()));
			panelReport.setSize(new Dimension(internalSize, calculaAlturaPainelCentral()));

			// atualiza o a posicao painel rodape
			if (painelRodape != null) {
				painelRodape.setLocation(0, panelReport.getHeight() - ALTURA_RODAPE_REPORT);
				painelRodape.repaint();
			}

			panelReport.repaint();
		}
	}

	/**
	 * Retorna o tamanho interno do EFLReport para a criacao dos InternalReports
	 * 
	 * @return internalSize (tamanho interno do Report)
	 */
	public int getInternalSize() {
		return internalSize;
	}

	/**
	 * Metodo que configura o tamanho do Report.
	 * 
	 * @param int
	 *            width
	 * @param int
	 *            heigth
	 */
	public void setSize(int width, int height) {
		super.setSize(width, height);
		initialize();
	}

	/***********************
	 * fim da interface da classe
	 ******************************/

} // fim da classe
