package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import br.gov.caixa.sispl.dominio.OfertaCliente;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.UIIniciaAtendimentoEFL;
import br.gov.caixa.sispl.util.Messages;

public class PnlMensagemOferta2 extends EFLPanel {

	private static final long serialVersionUID = 1;

	public static final String TITULO_MODULO_OFERTAS = "OPORTUNIDADES DE NEGÓCIOS!!!";
	private static final String TITLE_1 = "Este cliente possui oferta de:";

	private EFLLabel labelTituloPrincipal = null;
	private EFLPanel pnlTituloPrincipal = null;

	private EFLLabel tituloLabel1;
	private EFLLabel tituloLabel2;
	private EFLLabel tituloLabel3;

	private EFLButton teclaOfertarDepois;
	private EFLButton teclaVerOutras;
	private EFLButton teclaSimular;
	private EFLButton teclaNao;

	private DefaultUIManager defaultUIManager;

	private UIIniciaAtendimentoEFL ui;

	private OfertaCliente ofertaSelecionada;

	/***
	 * @param ui
	 * @param mensagem
	 * @param titulo
	 */
	public PnlMensagemOferta2(UIIniciaAtendimentoEFL ui, OfertaCliente ofertaSelecionada) {
		super(PanelSkin.PANEL_EFL);
		this.ui = ui;
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
		this.add(getTituloLabel2());
		this.add(getTituloLabel3());
		this.add(getTeclaOfertarDepois());
		this.add(getTeclaNao());
		this.add(getTeclaVerOutras());
		this.add(getTeclaSimular());
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
			tituloLabel1 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, TITLE_1);
			tituloLabel1.setLocation(120, 200);
			tituloLabel1.setSize(600, 40);
			tituloLabel1.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel1;
	}

	public EFLLabel getTituloLabel2() {
		if (tituloLabel2 == null) {
			tituloLabel2 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, getDescricaoProduto());
			tituloLabel2.setLocation(120, getTituloLabel1().getLocation().y + 30);
			tituloLabel2.setSize(600, 40);
			tituloLabel2.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel2;
	}

	private String getDescricaoProduto() {
		String retorno = "";
		if (ofertaSelecionada != null) {
			retorno = ofertaSelecionada.getDescricao().trim();
		}
		return retorno;
	}

	public EFLLabel getTituloLabel3() {
		if (tituloLabel3 == null) {
			tituloLabel3 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, getNomeOferta());
			tituloLabel3.setLocation(120, getTituloLabel2().getLocation().y + 30);
			tituloLabel3.setSize(600, 40);
			tituloLabel3.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel3;
	}

	private String getNomeOferta() {
		String retorno = "";
		if (ofertaSelecionada.getNomeOferta() != null) {
			retorno = ofertaSelecionada.getNomeOferta().toUpperCase().trim();
		}
		return retorno;
	}

	private EFLButton getTeclaOfertarDepois() {
		if (teclaOfertarDepois == null) {
			teclaOfertarDepois = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA,
					Messages.getString("MODULO_OFERTA_LABEL_OFERTAR_DEPOIS")); //$NON-NLS-1$
			teclaOfertarDepois.setLocation(120, getTituloLabel3().getLocation().y + 40);
			teclaOfertarDepois.setSize(120, 80);
			teclaOfertarDepois.setDefaultButton();
			teclaOfertarDepois.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ui.mostrarAguardeProcessando();
					ui.tratarOfertaDepois(ofertaSelecionada);
				}
			});
		}
		return teclaOfertarDepois;
	}

	private EFLButton getTeclaNao() {
		if (teclaNao == null) {
			teclaNao = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA,
					Messages.getString("MODULO_OFERTA_LABEL_NAO_QUER_PRODUTO")); //$NON-NLS-1$
			teclaNao.setLocation(getTeclaOfertarDepois().getLocation().x + 120, getTituloLabel3().getLocation().y + 40);
			teclaNao.setSize(180, 80);
			teclaNao.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ui.mostrarAguardeProcessando();
					ui.tratarRejeitouOferta(ofertaSelecionada);
				}
			});
		}
		return teclaNao;
	}

	private EFLButton getTeclaVerOutras() {
		if (teclaVerOutras == null) {
			teclaVerOutras = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA,
					Messages.getString("MODULO_OFERTA_LABEL_VER_OUTRAS_OFERTAS")); //$NON-NLS-1$
			teclaVerOutras.setLocation(getTeclaNao().getLocation().x + 170, getTituloLabel3().getLocation().y + 40);
			teclaVerOutras.setSize(140, 80);
			teclaVerOutras.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					teclaVerOutras.setEnabled(false);
					teclaVerOutras.repaint();
					PnlMensagemOferta2.this.repaint();
					ui.mostrarListaProdutoOferta(ofertaSelecionada);
				}
			});
		}
		return teclaVerOutras;
	}

	@SuppressWarnings("serial")
	private EFLButton getTeclaSimular() {
		if (teclaSimular == null) {
			teclaSimular = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA,
					Messages.getString("MODULO_OFERTA_LABEL_SIMULAR")); //$NON-NLS-1$
			teclaSimular.setLocation(getTeclaVerOutras().getLocation().x + 140, getTituloLabel3().getLocation().y + 40);
			teclaSimular.setSize(140, 80);
			teclaSimular.addActionListener(new SwingWorkerActionListener() {

				@Override
				public void actionPerformedInBackground(ActionEvent actionEvent) {
					ui.mostrarAguardeProcessando();
					ui.simularOferta(ofertaSelecionada);
				}
			});
		}
		return teclaSimular;
	}
}
