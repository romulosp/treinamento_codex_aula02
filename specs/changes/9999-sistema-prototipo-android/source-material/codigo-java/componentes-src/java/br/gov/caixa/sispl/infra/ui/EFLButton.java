/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import br.gov.caixa.sispl.infra.ui.plaf.EFLButtonUI;

/**
 *
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLButton extends JComponent {

    private String text = null;
    private boolean armed = false;
    private boolean pressed = false;
    private ImageIcon imageIcon = null;
    private ActionListener actionListener = null;
    private UIManager uiManager;


    /**
     * Construtor que recebe apenas o tipo de skin do button
     * @param buttonSkin String skin do button
     */
    public EFLButton(UIManager uiManager, String buttonSkin) {
        this(uiManager, buttonSkin, null);
    }

    /**
     * Construtor que recebe um tipo de skin para o buton e um label 
     * @param buttonSkin String tipo de skin desse componente
     * @param buttonLabel String label do button
     */
    public EFLButton(UIManager uiManager, String buttonSkin, String text) {
        this.uiManager = uiManager;
        setUI(new EFLButtonUI(buttonSkin));
        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    public boolean isArmed() {
        return armed;
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
        repaint();
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        if (this.pressed && !pressed && armed && isEnabled()) {
            doClick();
        }
        this.pressed = pressed;
        repaint();
    }

    public void doClick() {
        ActionEvent actionEvent = new ActionEvent(this, 0, "click");
        if ((actionListener != null) && isEnabled()) {
            Toolkit.getDefaultToolkit().beep();
            actionListener.actionPerformed(actionEvent);
        }
    }

    public ImageIcon getIcon() {
        return imageIcon;
    }

    public void setIcon(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
        repaint();
    }

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void addActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void removeActionListener(ActionListener actionListener) {
        if (this.actionListener == actionListener) {
            this.actionListener = null;
        }
    }

    public boolean isDefaultButton() {
        return uiManager.getDefaultButton() == this;
    }

    public void setDefaultButton() {
        uiManager.setDefaultButton(this);
    }

    public boolean isCancelButton() {
        return uiManager.getCancelButton() == this;
    }

    public void setCancelButton() {
        uiManager.setCancelButton(this);
    }

}

