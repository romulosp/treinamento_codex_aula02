package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.passwordfield.PasswordFieldSkin;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkin;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.EFLTextField;
import br.gov.caixa.sispl.infra.ui.EFLTextFieldAdapter;
import br.gov.caixa.sispl.infra.ui.EFLVirtualKeyboard;
import br.gov.caixa.sispl.infra.ui.EFLVirtualKeyboardNumeric;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.UIAbstractControle;
import br.gov.caixa.sispl.infra.ui.document.EFLPasswordDocument;
import br.gov.caixa.sispl.infra.ui.document.EFLUserDocument;
import br.gov.caixa.sispl.util.MensagemErro;

/**
 * Tela para inserir dados do Supervisor para liberar acesso ao caso de uso.
 * 
 * @author r688989 - Natã ALves Evangelista
 *
 */
public class PnlSenSup extends EFLPanel {
	private static final long serialVersionUID = 5283061011208528685L;

	private UIAbstractControle ui = null;

    private EFLVirtualKeyboard eflVirtualKeyboard = null;

    private EFLVirtualKeyboardNumeric eflVirtualKeyboardNumeric = null;

    private static final int FIELDSIZE_PASSWORD = 8;

    private JPanel pnlLogin = null;

    private EFLLabel labelUsuario = null;

    private EFLTextField textUsuario = null;

    private EFLLabel labelSenha = null;

    private EFLTextFieldAdapter passwordSenha = null;

    public PnlSenSup(UIAbstractControle ui, Boolean ehProprietario) {
        super(PanelSkin.PANEL_EFL);
        
        this.ui = ui;
        initialize(ehProprietario);
        textUsuario.requestFocus();
    }

    private void initialize(Boolean ehProprietario) {
        setLayout(null);
        this.setOpaque(false);
        this.add(getEFLVirtualKeyboard());
        this.add(getEFLVirtualKeyboardNumeric());
        this.add(getPnlLogin());

		EFLLabel mensagem = new EFLLabel(LabelSkin.LABEL_TITULO_1,
				"Digite o nome do usuário e a senha "+ (ehProprietario ? "do PROPRIETÁRIO" : "DO SUPERVISOR"));
        mensagem.setHorizontalAlignment(EFLLabel.CENTER);
        mensagem.setSize(575, 30);
        mensagem.setLocation(30, 170);
        this.add(mensagem);

        getTextUsuario().requestFocus();
    }

    private EFLVirtualKeyboard getEFLVirtualKeyboard() {
        if (eflVirtualKeyboard == null) {
            eflVirtualKeyboard = new EFLVirtualKeyboard(uiManager);
            eflVirtualKeyboard.setLocation(0, 350);
            eflVirtualKeyboard.setConfirmarButtonListener(new SwingWorkerActionListener() {
				private static final long serialVersionUID = -2880882833240842578L;
						public void actionPerformedInBackground(ActionEvent e) {
                            getEFLVirtualKeyboard().setEnabledConfirmar(false);
                            getEFLVirtualKeyboard().setEnabledCancelar(false);
                            ui.apresentaMsg(MensagemErro.getString("GRLI0003"));
                            ui.setSupervisor(textUsuario.getText(), passwordSenha.getText());
                            ui.concluir();
                        }
                    }
                                                         );
            eflVirtualKeyboard.setCancelarButtonListener(new SwingWorkerActionListener() {
				private static final long serialVersionUID = -4645537852447064907L;
						public void actionPerformedInBackground(ActionEvent e) {
                            getEFLVirtualKeyboard().setEnabledConfirmar(false);
                            getEFLVirtualKeyboard().setEnabledCancelar(false);
                            ui.apagaMsg();
                            ui.fim();
                        }
                    }
                                                        );
        }
        return eflVirtualKeyboard;
    }

    private EFLVirtualKeyboardNumeric getEFLVirtualKeyboardNumeric() {
        if (eflVirtualKeyboardNumeric == null) {
            eflVirtualKeyboardNumeric = new EFLVirtualKeyboardNumeric(uiManager);
            eflVirtualKeyboardNumeric.setLocation(600, 100);
        }
        return eflVirtualKeyboardNumeric;
    }

    private EFLLabel getLabelUsuario() {
        if (labelUsuario == null) {
            labelUsuario = new EFLLabel(LabelSkin.LABEL_LOGIN, "USUÁRIO");
            labelUsuario.setLocation(87, 123);
            labelUsuario.setSize(98, 22);
            labelUsuario.setHorizontalAlignment(EFLLabel.CENTER);
        }
        return labelUsuario;
    }

    private EFLTextField getTextUsuario() {
        if (textUsuario == null) {
            textUsuario = new EFLTextField(uiManager, TextFieldSkin.TEXT_LOGIN);
            textUsuario.setLocation(190, 123);
            textUsuario.setSize(98, 22);
            textUsuario.setDocument(new EFLUserDocument());
        }
        return textUsuario;
    }

    private EFLLabel getLabelSenha() {
        if (labelSenha == null) {
            labelSenha = new EFLLabel(LabelSkin.LABEL_LOGIN, "SENHA");

            //posicionando e ajustando tamanho e disposição do texto
            labelSenha.setLocation(87, 150);
            labelSenha.setSize(98, 22);
            labelSenha.setHorizontalAlignment(EFLLabel.CENTER);
        }
        return labelSenha;
    }

    private EFLTextFieldAdapter getPasswordSenha() {
        if (passwordSenha == null) {
            passwordSenha = new EFLTextFieldAdapter(uiManager, PasswordFieldSkin.TEXT_LOGIN);
            passwordSenha.setDocument(new EFLPasswordDocument(FIELDSIZE_PASSWORD));
            passwordSenha.setLocation(190, 150);
            passwordSenha.setSize(98, 22);
        }
        return passwordSenha;
    }

    private JPanel getPnlLogin() {
        if (pnlLogin == null) {

            pnlLogin = new JPanel();
            pnlLogin.setSize(375, 235);
            pnlLogin.setBackground(new Color(92, 157, 228, 30));
            pnlLogin.setLocation(130, 110);
            pnlLogin.setLayout(null);

            //Adicionando os labels
            pnlLogin.add(getLabelSenha());
            pnlLogin.add(getLabelUsuario());

            //Adicionando os campos de texto
            pnlLogin.add(getTextUsuario());
            pnlLogin.add(getPasswordSenha());

        }
        return pnlLogin;
    }
}
