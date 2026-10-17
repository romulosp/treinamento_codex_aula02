package br.gov.caixa.sispl.infra.ui;


public interface UIManager {
    public void fireVirtualKeyboardKeyEvent(int keyCode, int keyModifiers);

    public void addFocusManagement(EFLTextField eflTextField);

    public EFLTextField getFocusedComponent();

    public void setFocus(EFLTextField eflTextField);

    public void cycleFocus();

    public EFLButton getDefaultButton();

    public void setDefaultButton(EFLButton defaultButton);

    public EFLButton getCancelButton();

    public void setCancelButton(EFLButton cancelButton);
}
