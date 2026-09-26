/*
 * Created on 15/04/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.state;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ColorBean {

    private int red;
    private int blue;
    private int green;

    private int alpha;

    /**
     * 
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public ColorBean(int red, int green, int blue, int alpha) {
        this.red = red;
        this.blue = blue;
        this.green = green;
        this.alpha = alpha;
    }

    /**
     * @return
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     * @return
     */
    public int getBlue() {
        return blue;
    }

    /**
     * @return
     */
    public int getGreen() {
        return green;
    }

    /**
     * @return
     */
    public int getRed() {
        return red;
    }

    /**
     * @param i
     */
    public void setAlpha(int i) {
        alpha = i;
    }

    /**
     * @param i
     */
    public void setBlue(int i) {
        blue = i;
    }

    /**
     * @param i
     */
    public void setGreen(int i) {
        green = i;
    }

    /**
     * @param i
     */
    public void setRed(int i) {
        red = i;
    }

}
