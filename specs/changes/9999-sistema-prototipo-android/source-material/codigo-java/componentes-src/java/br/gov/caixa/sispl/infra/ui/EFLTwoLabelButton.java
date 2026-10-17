/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;

import br.gov.caixa.sispl.infra.skin.SkinPainter;
import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;

/**
 *
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLTwoLabelButton extends JButton implements SkinPainter {

    private ButtonSkin component;

    private String value;

    public EFLTwoLabelButton(UIManager uiManager, String buttonSkin) {
        this(uiManager, buttonSkin, null, null);
    }
    /**
     * 
     * @param buttonSkin
     * @param buttonLabel
     */
    public EFLTwoLabelButton(UIManager uiManager, String buttonSkin, String value, String label) {
        super(label);
        this.value = value;
        if (buttonSkin != null)
            component = (ButtonSkin) SkinRender.renderComponent(ComponentSkin.BUTTON, buttonSkin);

        setContentAreaFilled(false);
    }

    /**
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * @param string
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isFocusable() {
        return false;
    }

    /*
     *  (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
        drawSkin(g);
    }

    /*
     *  (non-Javadoc)
     * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
     */
    protected void paintBorder(Graphics g) {}

    /* (non-Javadoc)
     * @see br.caixa.sispl.skin.CaixaSkinRender#setSkin(java.lang.String)
     */
    public void drawSkin(Graphics g) {
        SkinState state = null;

        if (getModel().isArmed())
            state = component.getTouchedState();
        else if (!this.isEnabled() && component.getDisabledState() != null)
            state = component.getDisabledState();
        else
            state = component.getDefaultState();

        this.setForeground(state.getFontColor());
        this.setFont(state.getFont());

        //Definindo a imagem
        if (state.getImage() == null)
            setBackground(state.getBackgroundColor());
        else
            g.drawImage(state.getImage(), 0, 0, getWidth(), getHeight(), this);

        g.setColor(getForeground());
        g.setFont(getFont());
        Rectangle2D rect = g.getFontMetrics().getStringBounds(getValue(), g);
        g.drawString(getValue(), (int) ((getWidth() / 2) - rect.getCenterX()), (int) ((getHeight() * 0.25) - rect.getCenterY()));
        rect = g.getFontMetrics().getStringBounds(getText(), g);
        g.drawString(getText(), (int) ((getWidth() / 2) - rect.getCenterX()), (int) ((getHeight() * 0.75) - rect.getCenterY()));
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        if (component != null)
            super.paint(g);
        else {
            super.paintComponent(g);
            super.paintBorder(g);
            super.paintChildren(g);
        }
    }
}
