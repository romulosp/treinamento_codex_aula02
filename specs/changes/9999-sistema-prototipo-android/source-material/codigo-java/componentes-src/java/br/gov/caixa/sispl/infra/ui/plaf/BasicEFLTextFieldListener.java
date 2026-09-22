package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import br.gov.caixa.sispl.infra.ui.EFLTextField;

public class BasicEFLTextFieldListener implements MouseListener {

    public void mouseClicked(MouseEvent e) {
        EFLTextField eflTextField = (EFLTextField) e.getSource();
        eflTextField.requestFocus();
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

}
