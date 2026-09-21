package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.util.MensagemErro;
import br.gov.caixa.sispl.util.Messages;

public  class PnlMensagemErroNegocial  extends EFLPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EFLLabel labelMensagemQuantidadeTentativa = null;

	private EFLPanel pnlTituloPrincipal = null;

	private EFLLabel labelTituloPrincipal = null;

	private EFLLabel labelMensagem = null;

	private String labelTitulo = null;

	private EFLButton btnCancelar = null;

	private EFLButton btnConfirmar = null;

	private EFLPanel pnlCentral = null;

	private boolean transparente = false;

	public PnlMensagemErroNegocial(String titulo,
			String mensagem,final SwingWorkerActionListener cancelarActionListener,
			final SwingWorkerActionListener confirmarActionListener, boolean transparente) {
		super(PanelSkin.PANEL_EFL);
		this.labelTitulo = titulo;
		this.transparente = transparente;
		initialize();
		getLabelMensagem().setText(mensagem);
		if (cancelarActionListener != null) {
			btnCancelar.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					btnCancelar.setEnabled(false);
					btnCancelar.repaint();
					cancelarActionListener.actionPerformed(new ActionEvent(cancelarActionListener, 2, ""));
				}
			});
			btnCancelar.setEnabled(true);
		}

		if (confirmarActionListener != null) {
			btnConfirmar.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnConfirmar.setEnabled(false);
					btnConfirmar.repaint();
					confirmarActionListener.actionPerformed(new ActionEvent(confirmarActionListener, 1, ""));
				}
			});
			btnConfirmar.setEnabled(true);
		}

		if (confirmarActionListener == null && cancelarActionListener == null) {
			remove(getBtnCancelar());
			remove(getBtnConfirmar());
		}
	}

	public PnlMensagemErroNegocial(String titulo,
			SwingWorkerActionListener cancelarActionListener, SwingWorkerActionListener confirmarActionListener,
			String codigoMensagem, boolean transparente) {
		this(titulo, MensagemErro.getString(codigoMensagem), cancelarActionListener,
				confirmarActionListener, transparente);
	}

	public PnlMensagemErroNegocial(String titulo,
			String mensagem) {
		this(titulo, mensagem, null, null, false);
	}

	public void initialize() {
		this.setLayout(null);
		if (transparente) {
			getPnlCentral().add(getLabelMensagem());
			getPnlCentral().add(getLabelMensagemQuantidadeTentativa());
			add(getPnlCentral());
		} else {
			add(getLabelMensagem());
			add(getLabelMensagemQuantidadeTentativa());
		}
		add(getBtnConfirmar());
		add(getBtnCancelar());
		add(getPnlTituloPrincipal());
	}

	public EFLButton getBtnConfirmar() {
		if (btnConfirmar == null) {
			btnConfirmar = new EFLButton(uiManager, ButtonSkin.BOTAO_CONFIRMAR, Messages.getString("CONFIRMAR"));
			btnConfirmar.setLocation(665, 515);
			btnConfirmar.setSize(110, 60);
			btnConfirmar.setEnabled(false);

			btnConfirmar.setDefaultButton();
		}
		return btnConfirmar;
	}

	public EFLButton getBtnCancelar() {
		if (btnCancelar == null) {
			btnCancelar = new EFLButton(uiManager, ButtonSkin.BOTAO_CANCELAR, Messages.getString("CANCELAR"));
			btnCancelar.setLocation(26, 515);
			btnCancelar.setSize(110, 60);
			btnCancelar.setCancelButton();
			btnCancelar.setEnabled(false);
		}
		return btnCancelar;
	}

	protected EFLPanel getPnlCentral() {
		if (pnlCentral == null) {
			pnlCentral = new EFLPanel(PanelSkin.PANEL_TRANSP_510_360);
			pnlCentral.setLocation(120, 160);
			pnlCentral.setSize(550, 310);
			pnlCentral.setLayout(null);
		}
		return pnlCentral;
	}

	/**
	 * Método responsável por criar o painel do icon
	 * 
	 * @return EFLPanel
	 */
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
	 * Método responsável por criar o label do icon.
	 * 
	 * @return EFLLabel
	 */
	private EFLLabel getLabelTituloPrincipal() {
		if (labelTituloPrincipal == null) {
			labelTituloPrincipal = new EFLLabel(LabelSkin.LABEL_FAIXA_TITULO_PRINCIPAL, labelTitulo, EFLLabel.RIGHT);
			labelTituloPrincipal.setLocation(0, 0);
			labelTituloPrincipal.setSize(750, 28);
		}
		return labelTituloPrincipal;
	}

	public boolean isTransparente() {
		return transparente;
	}

	public EFLLabel getLabelMensagemQuantidadeTentativa() {
		if (labelMensagemQuantidadeTentativa == null) {
			labelMensagemQuantidadeTentativa = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM);
			labelMensagemQuantidadeTentativa.setHorizontalAlignment(EFLLabel.CENTER);
			if (isTransparente()) {
				labelMensagemQuantidadeTentativa.setLocation(55, 200);
				labelMensagemQuantidadeTentativa.setSize(450, 100);
			} else {
				labelMensagemQuantidadeTentativa.setLocation(50, 300);
				labelMensagemQuantidadeTentativa.setSize(700, 100);
			}
 
		}
		return labelMensagemQuantidadeTentativa;
	}
	public EFLLabel getLabelMensagem() {
		if (labelMensagem == null) {
			labelMensagem = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM);
			labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
			if (isTransparente()) {
				labelMensagem.setLocation(55, 95);
				labelMensagem.setSize(450, 100);
			} else {
				labelMensagem.setLocation(50, 220);
				labelMensagem.setSize(700, 100);
			}
		}
		return labelMensagem;
	}
	
	
	public static PnlMensagemErroNegocial gerarPnlInformativo(String titulo,String mensagem, SwingWorkerActionListener acaoConfirmar,
			SwingWorkerActionListener acaoCancelar) {
		PnlMensagemErroNegocial pnlMensagem = new PnlMensagemErroNegocial(titulo, mensagem,acaoCancelar, acaoConfirmar, false);
		return pnlMensagem;
	}
	
	public static StringBuilder getMensagemErroInfra(ExecutorException executorException) {
		StringBuilder mensagemErro = new StringBuilder(
				MensagemErro.getString(executorException.getCodigoErro().getCodigoMensagem()) + " \n");
		mensagemErro.append("\nCódigo de erro [");
		mensagemErro.append(executorException.getCodigoErro().getCodigo() + "-");
		mensagemErro.append(executorException.getProperty("0"));
		mensagemErro.append("]");
		return mensagemErro;
	}
	
}