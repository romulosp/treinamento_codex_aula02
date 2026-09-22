/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import br.gov.caixa.sispl.infra.ui.document.EFLDocument;
import br.gov.caixa.sispl.infra.ui.document.EFLPlainDocument;
import br.gov.caixa.sispl.infra.ui.plaf.EFLTextFieldUI;

/**
 *
 * @author p504116
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLTextField extends JComponent {

    public static final int KEYMODIFIER_CAPSLOCK_OFF = 0;
    public static final int KEYMODIFIER_CAPSLOCK_ON = 1;

    protected EFLDocument eflDocument;
    private int horizontalAlignment = SwingConstants.LEFT;
    private boolean marked;
    private UIManager uiManager;
    private ActionListener endOfFieldListener = null;
    protected ActionListener userChangeListener = null;
    protected ActionListener tabPressedListener = null;

    public EFLTextField(UIManager uiManager, String textFieldSkin) {
        this.uiManager = uiManager;
        eflDocument = new EFLPlainDocument();
        setUI(new EFLTextFieldUI(textFieldSkin));
        this.uiManager.addFocusManagement(this);
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return eflDocument.getText();
    }

    /**
     * @return Returns the masked text.
     */
    public String getMaskedText() {
        return eflDocument.getMaskedText();
    }

    /**
     * @param text The text to set.
     */
    public void setText(String text) {
        eflDocument.setText(text);
        repaint();
    }

    /**
     * @return Returns the horizontalAlignment.
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * @param horizontalAlignment The horizontalAlignment to set.
     */
    public void setHorizontalAlignment(int horizontalAlignment) {
        if ((horizontalAlignment != SwingConstants.CENTER) || (horizontalAlignment != SwingConstants.LEFT) || (horizontalAlignment != SwingConstants.RIGHT)) {
            this.horizontalAlignment = horizontalAlignment;
        }
        repaint();
    }

    public EFLDocument getDocument() {
        return eflDocument;
    }

    public void setDocument(EFLDocument eflDocument) {
        this.eflDocument = eflDocument;
        repaint();
    }

    /**
     * @return Returns the cursorPosition.
     */
    public int getCursorPosition() {
        return eflDocument.getCursorPosition();
    }

    /**
     * @param cursorPosition The cursorPosition to set.
     */
    public void setCursorPosition(int cursorPosition) {
        eflDocument.setCursorPosition(cursorPosition);
        repaint();
    }

    /**
        * @return Returns the marked.
        */
    public boolean isMarked() {
        return marked;
    }

    /**
     * @param marked The marked to set.
     */
    public void setMarked(boolean marked) {
        this.marked = marked;
        eflDocument.setCursorPosition(getText().length());
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
            if (modifiers == KEYMODIFIER_CAPSLOCK_ON) {
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

    /* (non-Javadoc)
     * @see javax.swing.JComponent#requestFocus()
     */
    public void requestFocus() {
        uiManager.setFocus(this);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#hasFocus()
     */
    public boolean hasFocus() {
        return (uiManager.getFocusedComponent() == this);
    }

    public void selectAll() {
        setMarked(true);
    }

    public boolean isEditable() {
        return isVisible() && isEnabled();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if ((!enabled) && hasFocus()) {
            uiManager.cycleFocus();
        } else if (enabled && (uiManager.getFocusedComponent() == null)) {
            requestFocus();
        }
    }

    /**
     * @param actionListener The actionListener to add.
     */
    public void addEndOfFieldListener(ActionListener actionListener) {
        this.endOfFieldListener = actionListener;
    }

    /**
     * @param actionListener The actionListener to remove.
     */
    public void removeEndOfFieldListener(ActionListener actionListener) {
        if (this.endOfFieldListener == actionListener) {
            this.endOfFieldListener = null;
        }
    }

    public void addUserActionListener(ActionListener actionListener) {
        this.userChangeListener = actionListener;
    }
    
    public void removeUserActionListener(ActionListener actionListener) {
        if (this.userChangeListener == actionListener) {
            this.userChangeListener = null;
        }
    }

    public void addTabPressedListener(ActionListener actionListener){
        this.tabPressedListener = actionListener;
    }

    public void removeTabPressedActionListener(ActionListener actionListener) {
        if (this.tabPressedListener == actionListener) {
            this.tabPressedListener = null;
        }
    }

    public void fireEventLostFocus() {
        if ((endOfFieldListener != null) && isEnabled() && getMaskedText().length() >= eflDocument.getMinLength()) {
            ActionEvent actionEvent = new ActionEvent(this, 0, "endOfField");
            endOfFieldListener.actionPerformed(actionEvent);
        }
    }

}