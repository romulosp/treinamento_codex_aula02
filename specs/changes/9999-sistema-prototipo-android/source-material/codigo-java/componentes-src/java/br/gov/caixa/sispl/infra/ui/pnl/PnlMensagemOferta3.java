package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import br.gov.caixa.sispl.dominio.OfertaCliente;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLComboBox;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.UIIniciaAtendimentoEFL;
import br.gov.caixa.sispl.util.Messages;

public class PnlMensagemOferta3 extends EFLPanel {

	private static final long serialVersionUID = 1;

	public static final String TITULO_MODULO_OFERTAS = "OPORTUNIDADES DE NEGÓCIOS!!!";

	private EFLPanel pnlTituloPrincipal = null;
	private EFLLabel labelTituloPrincipal = null;
	private EFLLabel tituloLabel1;

	private EFLComboBox comboOfertaCliente = null;

	@SuppressWarnings("unused")
	private DefaultUIManager defaultUIManager;

	private UIIniciaAtendimentoEFL ui;
	private List<OfertaCliente> ofertas;

	private EFLButton teclaCancelar = null;
	private EFLButton teclaConfirmar = null;

	private OfertaCliente ofertaSelecionada;

	/***
	 * @param ui
	 * @param ofertaSelecionada 
	 * @param mensagem
	 * @param titulo
	 */
	public PnlMensagemOferta3(UIIniciaAtendimentoEFL ui, List<OfertaCliente> ofertas, OfertaCliente ofertaSelecionada) {
		super(PanelSkin.PANEL_EFL);
		this.ui = ui;
		this.ofertas = ofertas;
		this.ofertaSelecionada = ofertaSelecionada;
		this.defaultUIManager = new DefaultUIManager();
		initialize();
	}

	/**
	 * Inicializa componentes gráficos do Dialog
	 *
	 */
	public void initialize() {
		this.add(getPnlTituloPrincipal());
		this.add(getTituloLabel1());
		this.add(getComboOfertaCliente());
		
		this.add(getTeclaConfirmar());
		this.add(getTeclaCancelar());

	}

	protected EFLLabel getLabelTituloPrincipal() {
		if (labelTituloPrincipal == null) {
			labelTituloPrincipal = new EFLLabel(LabelSkin.LABEL_FAIXA_TITULO_PRINCIPAL, TITULO_MODULO_OFERTAS,
					EFLLabel.RIGHT);
			labelTituloPrincipal.setLocation(0, 0);
			labelTituloPrincipal.setSize(750, 28);
		}
		return labelTituloPrincipal;
	}

	private EFLPanel getPnlTituloPrincipal() {
		if (pnlTituloPrincipal == null) {
			pnlTituloPrincipal = new EFLPanel(PanelSkin.PANEL_FAIXA_TITULO_PRINCIPAL);
			pnlTituloPrincipal.setLocation(0, 100);
			pnlTituloPrincipal.setSize(800, 28);
			pnlTituloPrincipal.setVisible(true);
			pnlTituloPrincipal.add(getLabelTituloPrincipal());
		}
		return pnlTituloPrincipal;
	}

	private EFLLabel getTituloLabel1() {
		if (tituloLabel1 == null) {
			tituloLabel1 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM,
					Messages.getString("MODULO_OFERTA_LABEL_LISTA_PRODUTO"));
			tituloLabel1.setLocation(120, 200);
			tituloLabel1.setSize(600, 40);
			tituloLabel1.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel1;
	}

	public EFLComboBox getComboOfertaCliente() {

		if (comboOfertaCliente == null) {
			comboOfertaCliente = new EFLComboBox();
			comboOfertaCliente.setAutoscrolls(true);
			comboOfertaCliente.setLocation(190, 260);
			comboOfertaCliente.setSize(350, 40);
			addProdutoComboBox();
		}
		return comboOfertaCliente;
	}

	private void addProdutoComboBox() {
		Iterator<OfertaCliente> iteratorProduto = ofertas.iterator();
		while (iteratorProduto.hasNext()) {
			OfertaCliente oferta = iteratorProduto.next();
			comboOfertaCliente.addItem(oferta.getNomeOferta().toUpperCase());
		}
	}

	@SuppressWarnings("serial")
	public EFLButton getTeclaConfirmar() {
		if (teclaConfirmar == null) {
			teclaConfirmar = new EFLButton(uiManager, ButtonSkin.BOTAO_CONFIRMAR, Messages.getString("CONFIRMAR"));
			teclaConfirmar.setLocation(665, 515);
			teclaConfirmar.setSize(110, 60);
			teclaConfirmar.addActionListener(new SwingWorkerActionListener() {
				@Override
				public void actionPerformedInBackground(ActionEvent actionEvent) {
					
					OfertaCliente novaOfertaSelecionada = ofertas.get(comboOfertaCliente.getSelectedIndex());
					
					 if(novaOfertaSelecionada != null) {
						 ui.mostrarOpcaoSimulacaoOferta(novaOfertaSelecionada);
					 }
				}
			});
			teclaConfirmar.setDefaultButton();
		}
		return teclaConfirmar;
	}
	
	@SuppressWarnings("serial")
	protected EFLButton getTeclaCancelar() {
		if (teclaCancelar == null) {
			teclaCancelar = new EFLButton(uiManager, ButtonSkin.BOTAO_CANCELAR, Messages.getString("CANCELAR"));
			teclaCancelar.setLocation(26, 515);
			teclaCancelar.setSize(110, 60);
			teclaCancelar.setCancelButton();
			teclaCancelar.addActionListener(new SwingWorkerActionListener() {
				public void actionPerformedInBackground(ActionEvent e) {
				  ui.mostrarOpcaoSimulacaoOferta(ofertaSelecionada);
				}
			});
		}
		return teclaCancelar;
	}
}
