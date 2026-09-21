/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import br.gov.caixa.sispl.infra.ui.plaf.EFLToggleButtonUI;

/**
 *
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLToggleButton extends JComponent {

    private String text = null;
    
    private String text2 = null;

    private boolean armed = false;

    private boolean pressed = false;

    private boolean selected = false;

    private ImageIcon imageIcon = null;

    private ActionListener actionListener = null;

    private UIManager uiManager;

    private EFLButtonGroup eflButtonGroup = null;

    /**
     * Construtor que recebe apenas o tipo de skin do button
     * 
     * @param buttonSkin
     *            String skin do button
     */
    public EFLToggleButton(UIManager uiManager, String toggleButtonSkin) {
        this(uiManager, toggleButtonSkin, null);
    }

    /**
     * Construtor que recebe um tipo de skin para o buton e um label
     * 
     * @param buttonSkin
     *            String tipo de skin desse componente
     * @param buttonLabel
     *            String label do button
     */
    public EFLToggleButton(UIManager uiManager, String toggleButtonSkin, String text) {
        this.uiManager = uiManager;
        setUI(new EFLToggleButtonUI(toggleButtonSkin));
        setText(text);
    }
    
    /**
     * Construtor que recebe um tipo de skin para o buton e um label
     * 
     * @param buttonSkin
     *            String tipo de skin desse componente
     * @param buttonLabel
     *            String label do button
     */
    public EFLToggleButton(UIManager uiManager, String toggleButtonSkin, String text, String text2) {
        this.uiManager = uiManager;
        setUI(new EFLToggleButtonUI(toggleButtonSkin));
        setText(text);
        setText2(text2);
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        repaint();
    }

    /**
     * @return Returns the armed.
     */
    public boolean isArmed() {
        return armed;
    }

    /**
     * @param armed
     *            The armed to set.
     */
    public void setArmed(boolean armed) {
        this.armed = armed && isEnabled();
        repaint();
    }

    /**
     * @return Returns the armed.
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * @param armed
     *            The armed to set.
     */
    public void setPressed(boolean pressed) {
        if (this.pressed && !pressed && armed) {
            ActionEvent actionEvent = new ActionEvent(this, 0, "click");
            setSelected(!isSelected());
            if ((actionListener != null) && isEnabled()) {
                actionListener.actionPerformed(actionEvent);
            }
        }
        this.pressed = pressed && isEnabled();
        repaint();
    }

    /**
     * @return Returns the armed.
     */
    public boolean isSelected() {
        return (eflButtonGroup != null)
               ? (eflButtonGroup.getSelected() == this)
               : selected;
    }

    /**
     * @param armed
     *            The armed to set.
     */
    public void setSelected(boolean selected) {
        if ((eflButtonGroup != null) && selected) {
            eflButtonGroup.setSelected(this);
        } else {
            this.selected = selected;
        }
        repaint();
    }

    /**
     * @return Returns the eflButtonGroup.
     */
    public EFLButtonGroup getEFLButtonGroup() {
        return eflButtonGroup;
    }

    /**
     * @param eflButtonGroup
     *            The eflButtonGroup to set.
     */
    public void setEFLButtonGroup(EFLButtonGroup eflButtonGroup) {
        this.eflButtonGroup = eflButtonGroup;
    }

    /**
     * @return Returns the imageIcon.
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }

    /**
     * @param imageIcon
     *            The imageIcon to set.
     */
    public void setIcon(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
        repaint();
    }

    /**
     * @return Returns the actionListener.
     */
    public ActionListener getActionListener() {
        return actionListener;
    }

    /**
     * @param actionListener
     *            The actionListener to add.
     */
    public void addActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * @param actionListener
     *            The actionListener to remove.
     */
    public void removeActionListener(ActionListener actionListener) {
        if (this.actionListener == actionListener) {
            this.actionListener = null;
        }
    }

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}
}
