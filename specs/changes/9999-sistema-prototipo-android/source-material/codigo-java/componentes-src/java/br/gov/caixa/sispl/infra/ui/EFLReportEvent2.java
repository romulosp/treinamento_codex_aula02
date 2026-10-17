package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;

import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;

public class EFLReportEvent2 extends EFLReportEvent {

	private static final long serialVersionUID = 1L;

	public EFLReportEvent2(int internalSize) {
		super(internalSize);

	}

	public EFLPanel addPanelCabecalho(Integer width, Integer height) {
		if (painelCabecalho != null) {
			panelReport.remove(painelCabecalho);
		}
		painelCabecalho = new EFLPanel(PanelSkin.PANEL_FAIXA_TITULO_PRINCIPAL);
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

}
