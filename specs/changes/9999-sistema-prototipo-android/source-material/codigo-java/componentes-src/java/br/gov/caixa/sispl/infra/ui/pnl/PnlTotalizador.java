package br.gov.caixa.sispl.infra.ui.pnl;

import java.math.BigDecimal;

import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.cesta.CestaFactory;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.cesta.ICestaCliente;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.util.Formatador;
import br.gov.caixa.sispl.util.Messages;

public class PnlTotalizador extends EFLPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2008031001L;

	private EFLLabel labelTotalizador;

	private EFLLabel labelValorCliente;

	// Constante do tamanho do label.
	private static final int TAM_LABEL_VALOR_CLIENTE = 120;

	// Constante da posicao do label.
	private static final int POSX_LABEL_VALOR_CLIENTE = 10;

	// Constante do tamanho do label.
	private static final int TAM_LABEL_TOTALIZADOR = 100;

	// Constante da posicao do label.
	private static final int POSX_LABEL_TOTALIZADOR = 120;

	private ICestaCliente cestaCliente;

	public PnlTotalizador() {
		super(PanelSkin.PANEL_TOTALIZACAO);
		initialize();

	}

	protected void initialize() {
		inicializarCesta();
		setSize(230, 25);
		setLocation(320, 52);
		this.add(getLabelValorCliente());
		this.add(getLabelTotalizador());
	}

	private void inicializarCesta() {
		try {
			this.cestaCliente = CestaFactory.getInstance();
		} catch (ExecutorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Responsável por criar o label que apresenta o valor total do cliente
	 * 
	 * @return EFLLabel
	 */
	private EFLLabel getLabelTotalizador() {
		if (labelTotalizador == null) {
			labelTotalizador = new EFLLabel(LabelSkin.LABEL_TOTALIZACAO, "");
			labelTotalizador.setSize(TAM_LABEL_TOTALIZADOR, 20);
			labelTotalizador.setLocation(POSX_LABEL_TOTALIZADOR, 1);
			labelTotalizador.setHorizontalAlignment(EFLLabel.RIGHT);
		}
		return labelTotalizador;

	}

	/**
	 * Responsável por criar o label que apresenta o texto "Valor do cliente"
	 * 
	 * @return EFLLabel
	 */
	private EFLLabel getLabelValorCliente() {
		if (labelValorCliente == null) {
			labelValorCliente = new EFLLabel(LabelSkin.LABEL_TOTALIZACAO, Messages.getString("VALOR_CLIENTE"));
			labelValorCliente.setSize(TAM_LABEL_VALOR_CLIENTE, 20);
			labelValorCliente.setLocation(POSX_LABEL_VALOR_CLIENTE, 1);
			labelValorCliente.setHorizontalAlignment(EFLLabel.LEFT);
		}
		return labelValorCliente;

	}

	public void refresh(BigDecimal valor) {
		getLabelTotalizador().setText(Formatador.getInstance().formataMonetario(valor));
	}

	public void refresh() {

		try {
			getLabelTotalizador()
					.setText(Formatador.getInstance().formataMonetario(this.cestaCliente.getValorResultado()));
		} catch (final Exception e) {
			getLabelTotalizador().setText(Formatador.getInstance().formataMonetario(BigDecimal.ZERO));
		}

	}

}
