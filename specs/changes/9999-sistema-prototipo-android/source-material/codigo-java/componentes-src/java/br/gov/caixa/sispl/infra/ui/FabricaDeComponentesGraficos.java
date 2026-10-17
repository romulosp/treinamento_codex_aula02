package br.gov.caixa.sispl.infra.ui;


import java.awt.event.ActionListener;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkin;
import br.gov.caixa.sispl.infra.ui.document.EFLDocument;

public class FabricaDeComponentesGraficos {

    public static EFLButton getTeclaConfirmar(UIManager uiManager, ActionListener actionListener, int x, int y, int width, int height) {
        EFLButton teclaConfirmar = new EFLButton(uiManager, ButtonSkin.BOTAO_CONFIRMAR, "CONFIRMAR");
        teclaConfirmar.setLocation(x, y);
        teclaConfirmar.setSize(width, height);
        teclaConfirmar.setEnabled(false);
        teclaConfirmar.setDefaultButton();
        teclaConfirmar.addActionListener(actionListener);

        return teclaConfirmar;
    }

    public static EFLButton getTeclaConfirmar(UIManager uiManager, ActionListener actionListener) {
        return getTeclaConfirmar(uiManager, actionListener, 665, 515, 110, 60);
    }

    public static EFLButton getTeclaCancelar(UIManager uiManager, ActionListener actionListener, int x, int y, int width, int height) {
        EFLButton buttonCancel = new EFLButton(uiManager, ButtonSkin.BOTAO_CANCELAR, "CANCELAR");
        buttonCancel.setLocation(x, y);
        buttonCancel.setSize(width, height);
        buttonCancel.setCancelButton();
        buttonCancel.addActionListener(actionListener);
        return buttonCancel;
    }

    public static EFLButton getTeclaCancelar(UIManager uiManager, ActionListener actionListener) {
        return getTeclaCancelar(uiManager, actionListener, 26, 515, 110, 60);
    }

    public static EFLVirtualKeyboard getVirtualKeyboard(UIManager uiManager) {
        return getVirtualKeyboard(uiManager, 0, 375, null, null);
    }

    public static EFLVirtualKeyboard getVirtualKeyboard(UIManager uiManager, ActionListener cancelarListener, ActionListener confirmarListener) {
        return getVirtualKeyboard(uiManager, 0, 375, cancelarListener, confirmarListener);
    }

    public static EFLVirtualKeyboard getVirtualKeyboard(UIManager uiManager, int x, int y, ActionListener cancelarListener, ActionListener confirmarListener) {
        EFLVirtualKeyboard eflVirtualKeyboard = new EFLVirtualKeyboard(uiManager);
        eflVirtualKeyboard.setLocation(0, 375);
        eflVirtualKeyboard.setCancelarButtonListener(cancelarListener);
        eflVirtualKeyboard.setConfirmarButtonListener(confirmarListener);
        return eflVirtualKeyboard;
    }

    public static EFLVirtualKeyboardNumeric getVirtualKeyboardNumeric(UIManager uiManager, int x, int y) {
        EFLVirtualKeyboardNumeric virtualKeyboardNumeric = new EFLVirtualKeyboardNumeric(uiManager);
        virtualKeyboardNumeric.setLocation(x, y);
        return virtualKeyboardNumeric;
    }

    public static EFLVirtualKeyboardNumeric getVirtualKeyboardNumeric(UIManager uiManager) {
        return getVirtualKeyboardNumeric(uiManager, 600, 125);
    }

    public static EFLPanel getPnlTituloPrincipal(String casoDeUso) {
        EFLLabel labelTituloPrincipal = new EFLLabel(
                                            LabelSkin.LABEL_FAIXA_TITULO_PRINCIPAL, casoDeUso,
                                            EFLLabel.RIGHT);
        labelTituloPrincipal.setLocation(0, 0);
        labelTituloPrincipal.setSize(750, 28);

        EFLPanel pnlTituloPrincipal = new EFLPanel(PanelSkin.PANEL_FAIXA_TITULO_PRINCIPAL);
        pnlTituloPrincipal.setLocation(0, 100);
        pnlTituloPrincipal.setSize(800, 28);
        pnlTituloPrincipal.setVisible(true);
        pnlTituloPrincipal.add(labelTituloPrincipal);
        return pnlTituloPrincipal;
    }

    public static EFLToggleButton getToggleButton(UIManager uiManager, int x, int y, ActionListener actionListener) {
        EFLToggleButton button ;
        button = new EFLToggleButton(uiManager, ToggleButtonSkin.BOTAO_PROGNOSTICOS_LOTOMANIA);
        button.setLocation(x, y);
        button.setSize(32, 32);

        button.addActionListener(actionListener);

        return button;
    }

    public static EFLLabel getLabelCentral(String msg, String skin) {
        return getLabel(msg, skin, 0, 150, 600, 30, EFLLabel.CENTER);
    }

    public static EFLLabel getLabel(String msg, String skin, int x, int y, int width, int height, int alignment) {
        EFLLabel label = new EFLLabel(skin, msg);
        label.setLocation(x, y);
        label.setSize(width, height);
        label.setHorizontalAlignment(alignment);

        return label;
    }

    public static EFLTextField getTextField(UIManager uiManager,
                                            String skin, EFLDocument document, int x, int y, int width,
                                            int height) {
        EFLTextField textField = new EFLTextField(uiManager, skin);
        textField.setDocument(document);
        textField.setLocation(x, y);
        textField.setSize(width, height);

        return textField;
    }

    /**
     * @deprecated
     */
    public static EFLPanel getPnlCentral() {
        return getPnlTransparente(15, 140, 570, 360);
    }

    public static EFLPanel getPnlTransparenteGrande() {
        return getPnlTransparente(100, 150, 600, 350);
    }

    public static EFLPanel getPnlTransparente() {
        return getPnlTransparente(15, 140, 570, 360);
    }

    public static EFLPanel getPnlTransparente(int x, int y, int width, int height) {
        EFLPanel pnlCentral = new EFLPanel(PanelSkin.PANEL_TRANSP_510_360);
        pnlCentral.setLocation(x, y);
        pnlCentral.setSize(width, height);
        pnlCentral.setLayout(null);

        return pnlCentral;
    }

    public static EFLButton getButtonMenu(String labelButton, int locationX,
                                          int locationY, UIManager uiManager,
                                          ActionListener actionListener) {

        /* Botão do tipo menu */
        EFLButton buttonMenu = new EFLButton(uiManager,	ButtonSkin.BOTAO_MENU, labelButton);

        /* Seta as propriedades do botão */
        buttonMenu.setLocation(locationX, locationY); //TODO 250, 350
        buttonMenu.setSize(150, 60);

        buttonMenu.addActionListener(actionListener);

        return buttonMenu;
    }

}
