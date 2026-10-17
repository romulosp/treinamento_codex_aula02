package br.gov.caixa.sispl.infra.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkin;

/**
 * UI responsável por exibir um report com manipulação de evento
 */
public class EFLReportEvent extends EFLPanel {

	private static final long serialVersionUID = -1805015229588401866L;

	protected static final int ESPACO_SOMBRA = 4;
	protected static final int ESPACO_RODAPE = 10;
	protected static final int TAMANHO_CABECALHO = 26;
	protected static final int TAMANHO_RODAPE = 32;
	protected static final int TAMANHO_BARRA_ROLAGEM = 0;
	public static final int ENTRADA_SIMPLES = 0;
	public static final int ENTRADA_SELECIONADA = 4;
	protected static final int ALTURA_ENTRADA_SIMPLES = 20;

	protected static final int ALTURA_ENTRADA_TITULO = 26;

	protected EFLPanel panelReport = null;
	protected EFLScrollPane2 scrollReport = null;
	protected EFLPanel painelCabecalho = null;
	protected ArrayList<EFLPanel> paineisLinhas = null;
	protected int indexPanelSelecionado = -1;
	protected EFLLabel decoradorCabecalho = null;
	protected EFLLabel decoradorRodape = null;
	protected int internalSize = 0;
	protected boolean selectable = true;
	protected boolean defaultSelectable = false;
	private boolean isBordaSuperior = true;
	private boolean isBordaInferior = true;

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

	private List<EFLReportEventListener> events = new ArrayList<EFLReportEventListener>();
	private int alturacustomizada;

	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public int getAlturacustomizada() {
		return alturacustomizada;
	}

	public void setAlturacustomizada(int alturacustomizada) {
		this.alturacustomizada = alturacustomizada;
	}

	public boolean isDefaultSelectable() {
		return defaultSelectable;
	}

	public void setDefaultSelectable(boolean defaultSelectable) {
		if (defaultSelectable) {
			setIndexPanelSelecionado(0);
		} else {
			setIndexPanelSelecionado(-1);
		}
		this.defaultSelectable = defaultSelectable;
	}

	public void setIndexPanelSelecionado(int indexPanelSelecionado) {
		this.indexPanelSelecionado = indexPanelSelecionado;
	}

	public void addSelectListener(EFLReportEventListener event) {
		events.add(event);
	}

	private void notifyObservers(int index) {
		for (EFLReportEventListener observer : this.events) {
			observer.onSelected(index);
		}
	}

	public EFLReportEvent(int internalSize) {
		setOpaque(false);
		this.setLayout(null);
		paineisLinhas = new ArrayList<EFLPanel>();
		this.internalSize = internalSize;
		initialize();
	}

	public EFLPanel addPanel() {
		EFLPanel novoPainel = null;
		if ((paineisLinhas.size() == 0) && isSelectable() && isDefaultSelectable()) {
			novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SELECTED);
		} else {
			novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
		}

		novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
		novoPainel.setPreferredSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
		novoPainel.setSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
		setAlturacustomizada(0);

		novoPainel.setLocation(0, getPosicaoAltura());
		novoPainel.setLayout(null);
		novoPainel.addMouseListener(new EFLReportActionListener(this, paineisLinhas.size()));

		paineisLinhas.add(novoPainel);
		panelReport.setPreferredSize(new Dimension(internalSize, getPosicaoAltura() + ESPACO_RODAPE));
		panelReport.setSize(new Dimension(internalSize, getPosicaoAltura() + ESPACO_RODAPE));
		panelReport.add(novoPainel);
		novoPainel.repaint();
		panelReport.repaint();
		return novoPainel;
	}

	public EFLPanel addPanelCustomizada(int alturaCustomizada) {
		EFLPanel novoPainel = null;
		if ((paineisLinhas.size() == 0) && isSelectable() && isDefaultSelectable()) {
			novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SELECTED);
		} else {
			novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
		}

		novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
		novoPainel.setPreferredSize(new Dimension(internalSize, alturaCustomizada));
		novoPainel.setSize(new Dimension(internalSize, alturaCustomizada));

		setAlturacustomizada(alturaCustomizada);

		novoPainel.setLocation(0, getPosicaoAlturaCustomizada(alturaCustomizada));
		novoPainel.setLayout(null);
		novoPainel.addMouseListener(new EFLReportActionListener(this, paineisLinhas.size()));

		paineisLinhas.add(novoPainel);
		panelReport.setPreferredSize(
				new Dimension(internalSize, getPosicaoAlturaCustomizada(alturaCustomizada) + ESPACO_RODAPE));
		panelReport
				.setSize(new Dimension(internalSize, getPosicaoAlturaCustomizada(alturaCustomizada) + ESPACO_RODAPE));
		panelReport.add(novoPainel);
		novoPainel.repaint();
		panelReport.repaint();
		return novoPainel;
	}

	public void removeAll() {
		indexPanelSelecionado = 0;
		for (Iterator<EFLPanel> it = paineisLinhas.iterator(); it.hasNext();) {
			panelReport.remove((EFLPanel) it.next());
		}
		paineisLinhas = new ArrayList<EFLPanel>();
	}

	public EFLPanel addPanelCabecalho() {
		if (painelCabecalho != null) {
			panelReport.remove(painelCabecalho);
		}
		painelCabecalho = new EFLPanel(PanelSkin.PANEL_REPORT_TITULO);
		painelCabecalho.setPreferredSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
		painelCabecalho.setSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
		painelCabecalho.setLocation(0, 0);
		painelCabecalho.setLayout(null);

		panelReport.setPreferredSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES + ESPACO_RODAPE));
		panelReport.setSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES + ESPACO_RODAPE));
		panelReport.add(painelCabecalho);
		painelCabecalho.repaint();
		panelReport.repaint();
		return painelCabecalho;
	}

	public EFLPanel addPanelCabecalho(Integer width, Integer height) {
		if (painelCabecalho != null) {
			panelReport.remove(painelCabecalho);
		}
		painelCabecalho = new EFLPanel(PanelSkin.PANEL_REPORT_TITULO);
		painelCabecalho.setPreferredSize(new Dimension(width, height));
		painelCabecalho.setSize(new Dimension(width, height));
		painelCabecalho.setLocation(0, 0);
		painelCabecalho.setLayout(null);

		panelReport.setPreferredSize(new Dimension(width, height));
		panelReport.setSize(new Dimension(width, height));

		panelReport.add(painelCabecalho);
		panelReport.repaint();

		painelCabecalho.repaint();

		return painelCabecalho;
	}

	private EFLPanel refreshPanel(int index) {
		EFLPanel novoPainel = null;
		if ((indexPanelSelecionado == index) && isSelectable()) {
			novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SELECTED);
		} else {
			novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
		}

		if (getAlturacustomizada() == 0) {
			novoPainel.setPreferredSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
			novoPainel.setSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
			novoPainel.setLocation(0, getPosicaoAltura(index));
		} else {
			novoPainel.setPreferredSize(new Dimension(internalSize, getAlturacustomizada()));
			novoPainel.setSize(new Dimension(internalSize, getAlturacustomizada()));
			novoPainel.setLocation(0, getPosicaoAltura(index, getAlturacustomizada()));
		}

		novoPainel.setLayout(null);
		novoPainel.addMouseListener(new EFLReportActionListener(this, index));

		panelReport.add(novoPainel);
		novoPainel.repaint();
		return novoPainel;
	}

	public void setSelected(int index) {
		if (indexPanelSelecionado == -1) {
			indexPanelSelecionado = 0;
		}

		if (index < paineisLinhas.size()) {
			Component[] componentsAntigoSelecionado = ((EFLPanel) paineisLinhas.get(indexPanelSelecionado))
					.getComponents();
			Component[] componentsNovoSelecionado = ((EFLPanel) paineisLinhas.get(index)).getComponents();
			int indexPainelAnterior = indexPanelSelecionado;
			indexPanelSelecionado = index;
			EFLPanel antigoPainel = refreshPanel(indexPainelAnterior);
			for (int i = 0; i < componentsAntigoSelecionado.length; i++) {
				Component component = componentsAntigoSelecionado[i];
				antigoPainel.add(component);
			}
			panelReport.remove((EFLPanel) paineisLinhas.set(indexPainelAnterior, antigoPainel));

			EFLPanel novoPainel = refreshPanel(indexPanelSelecionado);
			for (int i = 0; i < componentsNovoSelecionado.length; i++) {
				Component component = componentsNovoSelecionado[i];
				novoPainel.add(component);
			}
			panelReport.remove((EFLPanel) paineisLinhas.set(indexPanelSelecionado, novoPainel));

		}

		this.notifyObservers(index);
	}

	public int getSelectedIndex() {
		return indexPanelSelecionado;
	}

	protected int getPosicaoAltura() {
		return paineisLinhas.size() * ALTURA_ENTRADA_SIMPLES + (painelCabecalho != null ? ALTURA_ENTRADA_TITULO : 0);
	}

	protected int getPosicaoAlturaCustomizada(int alturaCustomizada) {
		return paineisLinhas.size() * alturaCustomizada + (painelCabecalho != null ? alturaCustomizada + 6 : 0);
	}

	protected int getPosicaoAltura(int index) {
		return index * ALTURA_ENTRADA_SIMPLES + (painelCabecalho != null ? ALTURA_ENTRADA_TITULO : 0);
	}

	protected int getPosicaoAltura(int index, int alturaCustomizada) {
		return index * alturaCustomizada + (painelCabecalho != null ? alturaCustomizada + 6 : 0);
	}

	private void initialize() {
		this.add(getDecoradorCabecalho());
		this.add(getScrollReport2());
		this.add(getDecoradorRodape());
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
		initialize();
	}

	private EFLLabel getDecoradorCabecalho() {
		if (decoradorCabecalho == null && isBordaSuperior()) {
			decoradorCabecalho = new EFLLabel(LabelSkin.LABEL_TABELA_CABECALHO);
			decoradorCabecalho.setLocation(0, 0);
		} else if (decoradorCabecalho != null && !isBordaSuperior()) {
			decoradorCabecalho = new EFLLabel(LabelSkin.LABEL_FORMULARIO);
			decoradorCabecalho.setLocation(0, 0);
		}
		decoradorCabecalho.setSize(this.getWidth(), TAMANHO_CABECALHO);
		return decoradorCabecalho;
	}

	private EFLLabel getDecoradorRodape() {
		if (decoradorRodape == null && isBordaInferior() ) {
			decoradorRodape = new EFLLabel(LabelSkin.LABEL_TABELA_RODAPE_2);
		} else if (decoradorRodape != null && !isBordaInferior()) {
			decoradorRodape = new EFLLabel(LabelSkin.LABEL_FORMULARIO);
		}
		decoradorRodape.setSize(this.getWidth(), TAMANHO_RODAPE);
		decoradorRodape.setLocation(0, this.getHeight() - TAMANHO_RODAPE);
		return decoradorRodape;
	}

	private EFLPanel getPanelReport() {
		if (panelReport == null) {
			panelReport = new EFLPanel(PanelSkin.PANEL_TABELA);
			panelReport.setLocation(0, 0);
		}
		panelReport.setPreferredSize(
				new Dimension(this.getWidth() - (ESPACO_SOMBRA + 3), getPosicaoAltura() + ESPACO_RODAPE));
		panelReport.setSize(new Dimension(this.getWidth() - (ESPACO_SOMBRA + 3), getPosicaoAltura() + ESPACO_RODAPE));
		return panelReport;
	}

	private EFLScrollPane2 getScrollReport2() {
		if (scrollReport == null) {
			scrollReport = new EFLScrollPane2(ScrollBarSkin.TEST_BAR);
			scrollReport.setViewportView(getPanelReport());
			scrollReport.setLocation(0, TAMANHO_CABECALHO);
		}
		scrollReport.setPreferredSize(new Dimension(this.getWidth() - ESPACO_SOMBRA,
				this.getHeight() - (TAMANHO_CABECALHO + TAMANHO_RODAPE)));
		scrollReport.setSize(new Dimension(this.getWidth() - ESPACO_SOMBRA,
				this.getHeight() - (TAMANHO_CABECALHO + TAMANHO_RODAPE)));
		return scrollReport;
	}

	protected class EFLReportActionListener extends MouseAdapter {
		private int index;
		private EFLReportEvent eflReport2;

		public EFLReportActionListener(EFLReportEvent eflReport2, int index) {
			EFLReportActionListener.this.index = index;
			this.eflReport2 = eflReport2;
		}

		public void mouseClicked(MouseEvent e) {
			if (eflReport2.isEnabled()) {
				eflReport2.setSelected(EFLReportActionListener.this.index);
			}
		}

	}
}
