/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SkinState {

    private Image image;

    private Color backgroundColor;

    private Color gridColor;

    private Color fontColor;

    private Font font;

    private double spacement;

    /**
     * @return
     */
    public Image getImage() {
        return image;
    }

    /**
     * @param image
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * @return
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * @return
     */
    public Color getFontColor() {
        return fontColor;
    }

    /**
     * @param color
     */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    /**
     * @param color
     */
    public void setFontColor(Color color) {
        fontColor = color;
    }

    /**
     * @return
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * @param color
     */
    public void setGridColor(Color color) {
        gridColor = color;
    }

    /**
     * @return
     */
    public double getSpacement() {
        return spacement;
    }

    /**
     * @param space
     */
    public void setSpacement(double space) {
        spacement = space;
    }

}
