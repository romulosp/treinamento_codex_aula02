package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import br.gov.caixa.sispl.infra.perifericos.GerenciaPerifericos;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.EFLTextField;

public class BasicEFLPanelListener implements KeyListener {
	
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {}

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
        boolean lockingState = false;
        try {
            lockingState = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        } catch (UnsupportedOperationException unsupportedOperationException) {
            //Erro comum. Acontece quando o teclado não possui a tecla caps lock. É o caso do teclado numérico.
        }
        
         
        if(e.getKeyCode()  == KeyEvent.VK_CAPS_LOCK){
        	if(!GerenciaPerifericos.capsLock){
        		 GerenciaPerifericos.capsLock = true;
        	}else{
        		 GerenciaPerifericos.capsLock = false;
        	}
        }else{
        	 if(e.isShiftDown()){
        		 GerenciaPerifericos.shiftDown = true;
            }else{
            	 GerenciaPerifericos.shiftDown = false;
            }
        }
         
        EFLPanel eflPanel = (EFLPanel) e.getSource();
        eflPanel.getUiManager().fireVirtualKeyboardKeyEvent(e.getKeyCode(),  lockingState ? EFLTextField.KEYMODIFIER_CAPSLOCK_ON : EFLTextField.KEYMODIFIER_CAPSLOCK_OFF);
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {}
}
