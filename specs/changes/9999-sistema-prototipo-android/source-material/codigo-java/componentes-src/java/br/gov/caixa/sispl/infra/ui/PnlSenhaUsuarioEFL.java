/*
 * Created on 17/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import br.gov.caixa.sispl.dominio.Logon;
import br.gov.caixa.sispl.dominio.TipoMovimento;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkin;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.EFLTextField;
import br.gov.caixa.sispl.infra.ui.EFLTextFieldAdapter;
import br.gov.caixa.sispl.infra.ui.EFLVirtualKeyboard;
import br.gov.caixa.sispl.infra.ui.EFLVirtualKeyboardNumeric;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.document.EFLPasswordDocument;
import br.gov.caixa.sispl.infra.ui.document.EFLUserDocument;
import br.gov.caixa.sispl.infra.util.ControleAcesso;
import br.gov.caixa.sispl.util.Messages;

/**
 * @author p532406
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PnlSenhaUsuarioEFL extends EFLPanel {
	private static final long serialVersionUID = 1L;

	private EFLVirtualKeyboard eflVirtualKeyboard = null;

	private EFLVirtualKeyboardNumeric eflVirtualKeyboardNumeric = null;

	private static final int FIELDSIZE_PASSWORD = 8;

	private EFLPanel pnlTituloPrincipal = null;
	private EFLLabel labelTituloPrincipal = null;

	private JPanel pnlLogin = null;

	private EFLLabel labelUsuario = null;

	private EFLTextField textUsuario = null;

	private EFLLabel labelSenha = null;

	private EFLTextFieldAdapter passwordSenha = null;
	
	private UIMenuControle ui;

	public PnlSenhaUsuarioEFL(UIMenuControle ui) {
		super(PanelSkin.PANEL_EFL);
		initialize();
		textUsuario.requestFocus();
		this.ui = ui;
	}

	private void initialize() {
		setLayout(null);
		this.setOpaque(false);
		this.add(getEFLVirtualKeyboard());
		this.add(getEFLVirtualKeyboardNumeric());
		this.add(getPnlLogin());
		this.add(getPnlTituloPrincipal());

		final EFLLabel mensagem = new EFLLabel(LabelSkin.LABEL_TITULO_1,
				"Digite usuário e senha");
		mensagem.setHorizontalAlignment(EFLLabel.CENTER);
		mensagem.setSize(575, 30);
		mensagem.setLocation(30, 170);
		this.add(mensagem);

		getTextUsuario().requestFocus();
	}

	/**
	 * Método responsável por criar o painel do icon
	 * 
	 * @return EFLLabel
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
			labelTituloPrincipal = new EFLLabel(LabelSkin.LABEL_FAIXA_TITULO_PRINCIPAL,
					"Desbloquear tela da EFL", EFLLabel.RIGHT);
			labelTituloPrincipal.setLocation(0, 0);
			labelTituloPrincipal.setSize(750, 28);
		}
		return labelTituloPrincipal;
	}

	private EFLVirtualKeyboard getEFLVirtualKeyboard() {
		if (eflVirtualKeyboard == null) {
			eflVirtualKeyboard = new EFLVirtualKeyboard(uiManager);
			eflVirtualKeyboard.setLocation(0, 375);
			eflVirtualKeyboard.setConfirmarButtonListener(new SwingWorkerActionListener() {

				private static final long serialVersionUID = 344050899557346483L;

				public void actionPerformedInBackground(final ActionEvent e) {
					getEFLVirtualKeyboard().setEnabledConfirmar(false);
					getEFLVirtualKeyboard().setEnabledCancelar(false);
					if (!ui.desbloqueiaTelaEFL(textUsuario.getText(), passwordSenha.getText())) {
						passwordSenha.setText("");
						getEFLVirtualKeyboard().setEnabledConfirmar(true);
						getEFLVirtualKeyboard().setEnabledCancelar(true);
					}
				}
			});
			
			eflVirtualKeyboard.getTeclaCancelar().setText("SAIR");
			eflVirtualKeyboard.setCancelarButtonListener(new SwingWorkerActionListener() {

				private static final long serialVersionUID = 8693518351022622361L;

				public void actionPerformedInBackground(final ActionEvent e) {
					getEFLVirtualKeyboard().setEnabledConfirmar(false);
					getEFLVirtualKeyboard().setEnabledCancelar(false);
					
					 ui.processaCasoUsoLogin(TipoMovimento.LOGOFF, 0);
				}
			});
		}
		return eflVirtualKeyboard;
	}

	private EFLVirtualKeyboardNumeric getEFLVirtualKeyboardNumeric() {
		if (eflVirtualKeyboardNumeric == null) {
			eflVirtualKeyboardNumeric = new EFLVirtualKeyboardNumeric(uiManager);
			eflVirtualKeyboardNumeric.setLocation(600, 125);
		}
		return eflVirtualKeyboardNumeric;
	}

	private EFLLabel getLabelUsuario() {
		if (labelUsuario == null) {
			labelUsuario = new EFLLabel(LabelSkin.LABEL_LOGIN, "USUÁRIO");
			labelUsuario.setLocation(80, 90);
			labelUsuario.setSize(110, 30);
			labelUsuario.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return labelUsuario;
	}

	private EFLTextField getTextUsuario() {
		if (textUsuario == null) {
			textUsuario = new EFLTextField(uiManager, TextFieldSkin.TEXT_LOGIN);
			textUsuario.setDocument(new EFLUserDocument());
			textUsuario.setLocation(190, 93);
			textUsuario.setSize(98, 22);
			
			Logon logon = ControleAcesso.getLogon();
			if(!logon.getOperador().isEmpty()) {
				textUsuario.setText(logon.getOperador());
				textUsuario.setEnabled(false);
			}
		}
		return textUsuario;
	}

	private EFLLabel getLabelSenha() {
		if (labelSenha == null) {
			labelSenha = new EFLLabel(LabelSkin.LABEL_LOGIN, "SENHA");
			labelSenha.setLocation(80, 120);
			labelSenha.setSize(110, 30);
			labelSenha.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return labelSenha;
	}

	private EFLTextFieldAdapter getPasswordSenha() {
		if (passwordSenha == null) {
			passwordSenha = new EFLTextFieldAdapter(uiManager, TextFieldSkin.TEXT_LOGIN);
			passwordSenha.setDocument(new EFLPasswordDocument(FIELDSIZE_PASSWORD));
			passwordSenha.setLocation(190, 123);
			passwordSenha.setSize(98, 22);
		}
		return passwordSenha;
	}

	private JPanel getPnlLogin() {
		if (pnlLogin == null) {

			pnlLogin = new JPanel();
			pnlLogin.setSize(375, 235);
			pnlLogin.setBackground(new Color(92, 157, 228, 30));
			pnlLogin.setLocation(130, 140);
			pnlLogin.setLayout(null);

			// Adicionando os labels
			pnlLogin.add(getLabelSenha());
			pnlLogin.add(getLabelUsuario());

			// Adicionando os campos de texto
			pnlLogin.add(getTextUsuario());
			pnlLogin.add(getPasswordSenha());

		}
		return pnlLogin;
	}

	public UIMenuControle getUi() {
		return ui;
	}

	public void setUi(UIMenuControle ui) {
		this.ui = ui;
	}
}
