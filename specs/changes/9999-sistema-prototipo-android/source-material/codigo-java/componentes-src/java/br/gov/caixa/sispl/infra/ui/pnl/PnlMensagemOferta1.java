package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import br.gov.caixa.sispl.dominio.OfertaCliente;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.UIIniciaAtendimentoEFL;
import br.gov.caixa.sispl.infra.ui.atendimento.pnl.PnlOfertaAutorizacao;
import br.gov.caixa.sispl.util.Messages;

public class PnlMensagemOferta1 extends EFLPanel {

	private static final long serialVersionUID = 1;

	public static final String TITULO_MODULO_OFERTAS = "OPORTUNIDADES DE NEGÓCIOS!!!";
	private static final String TITLE_1 = "Seu cliente pode ter ofertas incríveis de produtos";
	private static final String TITLE_2 = "selecionados de forma personalizada! Oferte e";
	private static final String TITLE_3 = "alavanque seus resultados!";


	private EFLLabel labelTituloPrincipal = null;
	private EFLPanel pnlTituloPrincipal = null;

	private EFLLabel tituloLabel1;
	private EFLLabel tituloLabel2;
	private EFLLabel tituloLabel3;

	private EFLButton teclaVer;

	private EFLButton teclaOfertarDepois;

	private UIIniciaAtendimentoEFL uiAtendimentoEFL;

	private List<OfertaCliente> listaOfertaClienteDisponivel;

	/***
	 * @param listaOfertaClienteDisponivel 
	 * @param ui
	 * @param mensagem
	 * @param titulo
	 */
	public PnlMensagemOferta1(UIIniciaAtendimentoEFL uiAtendimentoEFL, List<OfertaCliente> listaOfertaClienteDisponivel) {
		super(PanelSkin.PANEL_EFL);
		this.uiAtendimentoEFL = uiAtendimentoEFL;
		this.listaOfertaClienteDisponivel = listaOfertaClienteDisponivel;
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
		this.add(getTeclaOfertarDepoisVOferta());
		this.add(getTeclaVer());
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
			tituloLabel2 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, TITLE_2);
			tituloLabel2.setLocation(120, getTituloLabel1().getLocation().y + 30);
			tituloLabel2.setSize(600, 40);
			tituloLabel2.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel2;
	}

	public EFLLabel getTituloLabel3() {
		if (tituloLabel3 == null) {
			tituloLabel3 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, TITLE_3);
			tituloLabel3.setLocation(120, getTituloLabel2().getLocation().y + 30);
			tituloLabel3.setSize(600, 40);
			tituloLabel3.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel3;
	}

	private EFLButton getTeclaOfertarDepoisVOferta() {
		if (teclaOfertarDepois == null) {
			teclaOfertarDepois = new EFLButton(new DefaultUIManager(), ButtonSkin.BOTAO_MODULO_OFERTA,
					Messages.getString("MODULO_OFERTA_LABEL_OFERTAR_DEPOIS")); //$NON-NLS-1$
			teclaOfertarDepois.setLocation(260, getTituloLabel3().getLocation().y + 40);
			teclaOfertarDepois.setSize(140, 80);
			teclaOfertarDepois.setDefaultButton();
			teclaOfertarDepois.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					uiAtendimentoEFL.mostrarAguardeProcessando();
					uiAtendimentoEFL.tratarOfertaDepois(null);
				}
			});
		}
		return teclaOfertarDepois;
	}

	private EFLButton getTeclaVer() {
		if (teclaVer == null) {
			teclaVer = new EFLButton(new DefaultUIManager(), ButtonSkin.BOTAO_MODULO_OFERTA,
					Messages.getString("MODULO_OFERTA_LABEL_VER_OFERTAS")); //$NON-NLS-1$
			teclaVer.setLocation(getTeclaOfertarDepoisVOferta().getLocation().x + 150, getTituloLabel3().getLocation().y + 40);
			teclaVer.setSize(140, 80);
			teclaVer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
						teclaVer.setEnabled(false);
						teclaVer.repaint();
						PnlMensagemOferta1.this.repaint();
						uiAtendimentoEFL.mostrarOfertaAutorizacaoMargemProdutoOferta(listaOfertaClienteDisponivel.get(0));
				}
			});
		}
		return teclaVer;
	}
}
