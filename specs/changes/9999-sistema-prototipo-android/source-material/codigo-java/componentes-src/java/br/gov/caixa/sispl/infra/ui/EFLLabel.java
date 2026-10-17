/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import br.gov.caixa.sispl.infra.ui.plaf.EFLLabelUI;

/**
 *
 * @author p504116
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLLabel extends JComponent implements SwingConstants {

    private String text = null;
    private int horizontalAlignment = 0;
    private ImageIcon imageIcon = null;

    /**
     * Construtor que recebe apenas o tipo de skin do label
     * @param labelSkin String skin do label
     */
    public EFLLabel(String labelSkin) {
        this(labelSkin, null, LEFT);
    }

    /**
     * Construtor que recebe um tipo de skin para o label, e um texto 
     * @param labelSkin String tipo de skin desse componente
     * @param text String text do label
     */
    public EFLLabel(String labelSkin, String text) {
        this(labelSkin, text, LEFT);
    }

    /**
     * Construtor que recebe um tipo de skin para o label, um texto e um alinhamento horizontal 
     * @param labelSkin String tipo de skin desse componente
     * @param text String text do label
     */
    public EFLLabel(String labelSkin, String text, int horizontalAlignment) {
        setUI(new EFLLabelUI(labelSkin));
        setText(text);
        setHorizontalAlignment(horizontalAlignment);
        repaint();
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
     * @return Returns the imageIcon.
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }

    /**
     * @param imageIcon The imageIcon to set.
     */
    public void setIcon(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
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
}
