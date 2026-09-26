package br.gov.caixa.sispl.infra.ui.pnl;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.util.MensagemErro;
import br.gov.caixa.sispl.util.Messages;

public class PnlMensagem extends EFLPanel {

	private EFLPanel pnlTituloPrincipal = null;

	private EFLLabel labelTituloPrincipal = null;

	private EFLLabel labelMensagem = null;

	private String labelTitulo = null;

	private EFLButton btnCancelar = null;

	private EFLButton btnConfirmar = null;

	private EFLPanel pnlCentral = null;

	private boolean transparente = false;

	public PnlMensagem(String titulo, String mensagem, SwingWorkerActionListener cancelarActionListener, SwingWorkerActionListener confirmarActionListener, boolean transparente) {
		super(PanelSkin.PANEL_EFL);
		this.labelTitulo = titulo;
		this.transparente = transparente;
		initialize();
		labelMensagem.setText(mensagem);
		if (cancelarActionListener != null) {
			btnCancelar.addActionListener(cancelarActionListener);
			btnCancelar.setEnabled(true);
		} 

		if (confirmarActionListener != null) {
			btnConfirmar.addActionListener(confirmarActionListener);
			btnConfirmar.setEnabled(true);
		} 
        
        if(confirmarActionListener == null && cancelarActionListener == null){
            remove(getBtnCancelar());
            remove(getBtnConfirmar());
        }
	}

	public PnlMensagem(String titulo, SwingWorkerActionListener cancelarActionListener, SwingWorkerActionListener confirmarActionListener, String codigoMensagem, boolean transparente) {
		this(titulo, MensagemErro.getString(codigoMensagem), cancelarActionListener, confirmarActionListener, transparente);
	}

	public PnlMensagem(String titulo, String mensagem) {
		this(titulo, mensagem, null, null, false);
	}

	public void initialize() {
		this.setLayout(null);
		if (transparente) {
			getPnlCentral().add(getLabelMensagem());
			add(getPnlCentral());
		} else {
			add(getLabelMensagem());
		}
		add(getBtnConfirmar());
		add(getBtnCancelar());
		add(getPnlTituloPrincipal());
	}

	protected EFLLabel getLabelMensagem() {
		if (labelMensagem == null) {
			labelMensagem = new EFLLabel(LabelSkin.LABEL_TITULO_1);
			labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
			if (transparente) {
				labelMensagem.setLocation(55, 95);
				labelMensagem.setSize(450, 100);
			} else {
				labelMensagem.setLocation(50, 220);
				labelMensagem.setSize(700, 100);
			}

		}
		return labelMensagem;
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
}
