package br.gov.caixa.sispl.infra.ui;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import br.gov.caixa.sispl.infra.ui.document.EFLDocument;

public class DefaultUIManager implements UIManager {
    //Focused  TextField
    private EFLTextField focusedTextField;

    private ArrayList focusCollection;

    private EFLButton defaultButton;

    private EFLButton cancelButton;

    public DefaultUIManager() {
        focusCollection = new ArrayList();
    }

    public void addFocusManagement(EFLTextField eflTextField) {
        focusCollection.add(eflTextField);

        if (focusedTextField == null) {
            focusedTextField = eflTextField;
        }
    }

    public void fireVirtualKeyboardKeyEvent(int keyCode, int keyModifiers) {
        boolean textModified = false;
        boolean defaultButtonFired = false;
        boolean cancelButtonFired = false;
        switch (keyCode) {
        case KeyEvent.VK_TAB:
        case KeyEvent.VK_ADD:
        case KeyEvent.VK_PAGE_DOWN:
        case KeyEvent.VK_DOWN:
            cycleFocus();
            break;
        case KeyEvent.VK_SUBTRACT:
        case KeyEvent.VK_PAGE_UP:
        case KeyEvent.VK_UP:
            cycleFocusReverse();
            break;
        case KeyEvent.VK_ENTER:
            defaultButtonFired = true;
            break;
        case KeyEvent.VK_ESCAPE:
        case KeyEvent.VK_MULTIPLY:
            cancelButtonFired = true;
            break;
        case KeyEvent.VK_BACK_SPACE:
        case KeyEvent.VK_SPACE:
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
            textModified = true;
            break;
        }

        if (focusedTextField != null) {
            focusedTextField.processVirtualKey(
                keyCode,
                keyModifiers
            );
            if (textModified) {
                EFLDocument eflDocument = focusedTextField.getDocument();
                if (eflDocument.isFinished()) {
                    cycleFocus();
                }
            }
        }

        if ((defaultButton != null) && defaultButtonFired) {
            defaultButton.doClick();
        }

        if ((cancelButton != null) && cancelButtonFired) {
            cancelButton.doClick();
        }
    }

    public EFLTextField getFocusedComponent() {
        return focusedTextField;
    }

    public void setFocus(EFLTextField eflTextField) {
        EFLTextField oldFocusedTextField = focusedTextField;
        if (oldFocusedTextField != null) {
            oldFocusedTextField.fireEventLostFocus();
        }
        if (eflTextField.isEditable()) {
            focusedTextField = eflTextField;
            if (oldFocusedTextField != null) {
                oldFocusedTextField.repaint();
            }
            focusedTextField.repaint();
        }
    }

    public void cycleFocus() {
        if (focusCollection.isEmpty()) {
            return;
        }

        EFLTextField nextFocusedTextField = null;
        if ((focusedTextField == null) && (focusCollection.size() != 0)) {
            nextFocusedTextField = (EFLTextField) focusCollection.get(0);
        } else {
            nextFocusedTextField = (EFLTextField) focusCollection.get((focusCollection.indexOf(focusedTextField) + 1) % focusCollection.size());
        }

        int iterationCount = 0;
        int maxLoopIterations = focusCollection.size();
        while ((nextFocusedTextField != null) && (!nextFocusedTextField.isEditable()) && (nextFocusedTextField != focusedTextField)  && (iterationCount < maxLoopIterations)) {
            nextFocusedTextField = (EFLTextField) focusCollection.get((focusCollection.indexOf(nextFocusedTextField) + 1) % focusCollection.size());
            iterationCount++;
        }
        
        if (nextFocusedTextField != focusedTextField) {
            setFocus(nextFocusedTextField);
        } else if (focusedTextField != null) {
            focusedTextField.fireEventLostFocus();
            if ((focusedTextField != null) && (!focusedTextField.isEditable())) {
                focusedTextField = null;
            }
        }
    }

    public void cycleFocusReverse() {
        if (focusCollection.isEmpty()) {
            return;
        }

        EFLTextField previousFocusedTextField = null;
        if ((focusedTextField == null) && (!focusCollection.isEmpty())) {
            previousFocusedTextField = (EFLTextField) focusCollection.get(0);
        } else {
            int indexOfPreviousTextField = focusCollection.indexOf(focusedTextField) - 1;
            if (indexOfPreviousTextField < 0) {
                indexOfPreviousTextField = focusCollection.size() - 1;
            }
            previousFocusedTextField = (EFLTextField) focusCollection.get(indexOfPreviousTextField);
        }

        int iterationCount = 0;
        int maxLoopIterations = focusCollection.size();
        while ((previousFocusedTextField != null) && (!previousFocusedTextField.isEditable()) && (previousFocusedTextField != focusedTextField) && (iterationCount < maxLoopIterations)) {
            int indexOfPreviousTextField = focusCollection.indexOf(previousFocusedTextField) - 1;
            if (indexOfPreviousTextField < 0) {
                indexOfPreviousTextField = focusCollection.size() - 1;
            }
            previousFocusedTextField = (EFLTextField) focusCollection.get(indexOfPreviousTextField);
            iterationCount++;
        }
 
        if (previousFocusedTextField != focusedTextField) {
            setFocus(previousFocusedTextField);
        } else if (focusedTextField != null) {
            focusedTextField.fireEventLostFocus();
            if ((focusedTextField != null) && (!focusedTextField.isEditable())) {
                focusedTextField = null;
            }
        }
    }

    public EFLButton getDefaultButton() {
        return defaultButton;
    }

    public void setDefaultButton(EFLButton defaultButton) {
        this.defaultButton = defaultButton;
    }


    public EFLButton getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton(EFLButton cancelButton) {
        this.cancelButton = cancelButton;
    }
}
