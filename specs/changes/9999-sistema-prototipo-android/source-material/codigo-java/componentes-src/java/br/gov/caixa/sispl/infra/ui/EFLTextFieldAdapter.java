/*
 * Created on 12/05/2016
 *
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import br.gov.caixa.sispl.infra.perifericos.GerenciaPerifericos;

/**
 *
 * @author f744113	
 *
 */
public class EFLTextFieldAdapter extends EFLTextField {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public EFLTextFieldAdapter(UIManager uiManager, String textFieldSkin) {
	         super(uiManager, textFieldSkin);
	    }
	public void processVirtualKey(int keyCode, int modifiers) {
	       switch (keyCode) {
	        case KeyEvent.VK_BACK_SPACE:
	            if (isMarked()) {
	                setMarked(false);
	                setText("");
	            } else {
	                eflDocument.deleteCharBeforeCursor();
	            }
	            if ((userChangeListener != null) && isEnabled()) {
	                ActionEvent actionEvent = new ActionEvent(this, 0, "userAction");
	                userChangeListener.actionPerformed(actionEvent);
	            }
	            break;
	        case KeyEvent.VK_LEFT:
	            if (isMarked()) {
	                setMarked(false);
	                eflDocument.setCursorPosition(0);
	            } else {
	                eflDocument.setCursorPosition(getCursorPosition() - 1);
	            }
	            break;
	        case KeyEvent.VK_RIGHT:
	            if (isMarked()) {
	                setMarked(false);
	                eflDocument.setCursorPosition(getText().length());
	            } else {
	                eflDocument.setCursorPosition(getCursorPosition() + 1);
	            }
	            break;
	        case KeyEvent.VK_HOME:
	            if (isMarked()) {
	                setMarked(false);
	            }
	            eflDocument.setCursorPosition(0);
	            break;
	        case KeyEvent.VK_END:
	            if (isMarked()) {
	                setMarked(false);
	            }
	            eflDocument.setCursorPosition(getText().length());
	            break;
	        case KeyEvent.VK_0:
	        case KeyEvent.VK_1:
	        case KeyEvent.VK_2:
	        case KeyEvent.VK_3:
	        case KeyEvent.VK_4:
	        case KeyEvent.VK_5:
	        case KeyEvent.VK_6:
	        case KeyEvent.VK_7:
	        case KeyEvent.VK_8:
	        case KeyEvent.VK_9:
	            if (isMarked()) {
	                setMarked(false);
	                setText("");
	            }
	            eflDocument.insertTextAtCursor(String.valueOf((char) keyCode));
	            if ((userChangeListener != null) && isEnabled()) {
	                ActionEvent actionEvent = new ActionEvent(this, 0, "userAction");
	                userChangeListener.actionPerformed(actionEvent);
	            }
	            break;
	        case KeyEvent.VK_NUMPAD0:
	        case KeyEvent.VK_NUMPAD1:
	        case KeyEvent.VK_NUMPAD2:
	        case KeyEvent.VK_NUMPAD3:
	        case KeyEvent.VK_NUMPAD4:
	        case KeyEvent.VK_NUMPAD5:
	        case KeyEvent.VK_NUMPAD6:
	        case KeyEvent.VK_NUMPAD7:
	        case KeyEvent.VK_NUMPAD8:
	        case KeyEvent.VK_NUMPAD9:
	            if (isMarked()) {
	                setMarked(false);
	                setText("");
	            }
	            eflDocument.insertTextAtCursor(String.valueOf(((char) (keyCode - 48))));
	            if ((userChangeListener != null) && isEnabled()) {
	                ActionEvent actionEvent = new ActionEvent(this, 0, "userAction");
	                userChangeListener.actionPerformed(actionEvent);
	            }
	            break;
	        case KeyEvent.VK_SPACE:
	            if (isMarked()) {
	                setMarked(false);
	                setText("");
	            }
	            eflDocument.insertTextAtCursor(" ");
	            if ((userChangeListener != null) && isEnabled()) {
	                ActionEvent actionEvent = new ActionEvent(this, 0, "userAction");
	                userChangeListener.actionPerformed(actionEvent);
	            }
	            break;
	        case KeyEvent.VK_A:
	        case KeyEvent.VK_B:
	        case KeyEvent.VK_C:
	        case KeyEvent.VK_D:
	        case KeyEvent.VK_E:
	        case KeyEvent.VK_F:
	        case KeyEvent.VK_G:
	        case KeyEvent.VK_H:
	        case KeyEvent.VK_I:
	        case KeyEvent.VK_J:
	        case KeyEvent.VK_K:
	        case KeyEvent.VK_L:
	        case KeyEvent.VK_M:
	        case KeyEvent.VK_N:
	        case KeyEvent.VK_O:
	        case KeyEvent.VK_P:
	        case KeyEvent.VK_Q:
	        case KeyEvent.VK_R:
	        case KeyEvent.VK_S:
	        case KeyEvent.VK_T:
	        case KeyEvent.VK_U:
	        case KeyEvent.VK_V:
	        case KeyEvent.VK_W:
	        case KeyEvent.VK_X:
	        case KeyEvent.VK_Y:
	        case KeyEvent.VK_Z:
	            if (isMarked()) {
	                setMarked(false);
	                setText("");
	            }
	            String keyText = KeyEvent.getKeyText(keyCode);
	            if (modifiers == KEYMODIFIER_CAPSLOCK_ON || GerenciaPerifericos.capsLock|| GerenciaPerifericos.shiftDown) {
	                keyText = keyText.toUpperCase();
	            } else {
	                keyText = keyText.toLowerCase();
	            }
	            eflDocument.insertTextAtCursor(keyText);
	            if ((userChangeListener != null) && isEnabled()) {
	                ActionEvent actionEvent = new ActionEvent(this, 0, "userAction");
	                userChangeListener.actionPerformed(actionEvent);
	            }
	            break;
	        case KeyEvent.VK_TAB:
	            if ((tabPressedListener != null) && isEnabled()) {
	                ActionEvent actionEvent = new ActionEvent(this, 0, "tabPressed");
	                tabPressedListener.actionPerformed(actionEvent);
	            }
	            break;
	        }
	        repaint();
	    }
}