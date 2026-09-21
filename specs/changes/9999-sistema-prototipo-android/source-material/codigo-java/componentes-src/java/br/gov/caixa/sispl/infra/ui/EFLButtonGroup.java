/*
 * Created on 03/08/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.ui;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLButtonGroup {

    private EFLToggleButton eflToggleButton = null;

    public void add(EFLToggleButton eflToggleButton) {
        eflToggleButton.setEFLButtonGroup(this);
    }

    public EFLToggleButton getSelected() {
        return eflToggleButton;
    }

    public void setSelected(EFLToggleButton eflToggleButton) {
        if (eflToggleButton == null) {
            this.eflToggleButton = null;
            return;
        }

        if (eflToggleButton.getEFLButtonGroup() == this) {
            EFLToggleButton tempEFLToggleButton = this.eflToggleButton;
            this.eflToggleButton = eflToggleButton;
            if (tempEFLToggleButton != null) {
                tempEFLToggleButton.repaint();
            }
            eflToggleButton.repaint();
        }
    }

}
