package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.event.ActionEvent;

import br.gov.caixa.sispl.dominio.CodigoErro;
import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.infra.controle.ControleMenu;
import br.gov.caixa.sispl.infra.controle.InfoMovimento;
import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.UIMenuControle;

public class PnlConfirmarResposta extends EFLPanel {

	// Label que apresenta a titulo.
	private EFLLabel labelTituloPrincipal = null;

	private EFLPanel pnlTituloPrincipal = null;

	// Label que apresenta a mensagem.
	private EFLLabel labelMensagem = null;

	// Botao de Afirmação: Leva a listagem de Notificações
	private EFLButton teclaSim = null;

	private EFLButton teclaNao = null;

	private UIMenuControle uiAbstractControle;
	private ControleMenu controle;
	private InfoMovimento info;

	private String mensagem;

	private String titulo;

	private boolean apresentarMensagemRodape;
	private int tipoMovimento = 0;

	/***
	 * A CLASSE(EXTENDIDA DE UIAbstractControle) QUE NECESSITAR PODE SOBREESCREVER O
	 * MÉTODO executarTeclaOKMsgConfirmada CONFORME SUA NECESSIDADE.
	 * 
	 * @param ui
	 * @param mensagem
	 * @param titulo
	 */

	public PnlConfirmarResposta(UIMenuControle ui, ControleMenu controle, InfoMovimento info, String mensagem,
			String titulo, int tipoMovimento) {
		super(PanelSkin.PANEL_EFL);
		GerenteTimeoutUI.getInstance().stop();
		this.controle = controle;
		this.info = info;
		this.uiAbstractControle = ui;
		this.mensagem = mensagem != null && !mensagem.isEmpty() ? mensagem.toUpperCase() : mensagem;
		this.titulo = titulo;
		this.tipoMovimento = tipoMovimento;
		initialize();

	}

	public void initialize() {
		this.add(getPnlTituloPrincipal());
		this.add(getLabelMensagem());
		this.add(getTeclaNao());
		this.add(getTeclaSim());

	}

	protected EFLLabel getLabelTituloPrincipal() {
		if (labelTituloPrincipal == null) {
			labelTituloPrincipal = new EFLLabel(LabelSkin.LABEL_FAIXA_TITULO_PRINCIPAL, this.titulo, EFLLabel.RIGHT);
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

	/**
	 * Método responsável por criar o Label que contém a pergunta a ser respondida.
	 * 
	 * @return EFLLabel Label que contém a pergunta a ser respondida.
	 */
	protected EFLLabel getLabelMensagem() {
		if (labelMensagem == null) {
			labelMensagem = new EFLLabel(LabelSkin.LABEL_TITULO_1_PIX, this.mensagem);
			labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
			labelMensagem.setSize(800, 300);
			labelMensagem.setLocation((800 - labelMensagem.getWidth()) / 2, (600 - labelMensagem.getHeight()) / 2);
		}
		return labelMensagem;

	}

	public EFLButton getTeclaNao() {
		if (teclaNao == null) {
			teclaNao = new EFLButton(uiManager, ButtonSkin.BOTAO_CONFIRMAR, "NÃO");
			teclaNao.setSize(110, 60);
			teclaNao.setLocation(270, 415);
			teclaNao.setDefaultButton();
			teclaNao.addActionListener(new SwingWorkerActionListener() {
				public void actionPerformedInBackground(ActionEvent e) {
					teclaNao.repaint();
					uiAbstractControle.menu();

				}
			});
		}
		return teclaNao;
	}

	public EFLButton getTeclaSim() {
		if (teclaSim == null) {
			teclaSim = new EFLButton(uiManager, ButtonSkin.BOTAO_CONFIRMAR, "SIM");
			teclaSim.setLocation(470, 415);
			teclaSim.setSize(110, 60);
			teclaSim.addActionListener(new TeclaSimActionListener());
		}
		return teclaSim;
	}

	private class TeclaSimActionListener extends SwingWorkerActionListener {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformedInBackground(ActionEvent e) {
			uiAbstractControle.confirmaTrocaTipoConcurso(info);
		}
	}

}