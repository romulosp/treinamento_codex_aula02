/*
 * Created on 03/08/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import br.gov.caixa.sispl.infra.ui.EFLButton;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BasicEFLButtonListener implements MouseListener {

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        //Não faz nada
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
        EFLButton eflButton = (EFLButton) e.getSource();
        eflButton.setArmed(true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        EFLButton eflButton = (EFLButton) e.getSource();
        eflButton.setArmed(false);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        EFLButton eflButton = (EFLButton) e.getSource();
        eflButton.setPressed(true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        EFLButton eflButton = (EFLButton) e.getSource();
        eflButton.setPressed(false);
    }
}
