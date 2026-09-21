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
public class FontBean {

    private String type;
    private int size;
    private boolean bold;
    private boolean italic;
    private boolean underlined;

    private ColorBean color;

    /**
     * 
     * @param type
     * @param size
     * @param bold
     * @param italic
     * @param underlined
     * @param color
     */
    public FontBean(String type, int size, boolean bold, boolean italic, boolean underlined, ColorBean color) {
        this.type = type;
        this.size = size;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.color = color;
    }

    /**
     * @return
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * @return
     */
    public ColorBean getColor() {
        return color;
    }

    /**
     * @return
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @return
     */
    public boolean isUnderlined() {
        return underlined;
    }

    /**
     * @param b
     */
    public void setBold(boolean b) {
        bold = b;
    }

    /**
     * @param bean
     */
    public void setColor(ColorBean bean) {
        color = bean;
    }

    /**
     * @param b
     */
    public void setItalic(boolean b) {
        italic = b;
    }

    /**
     * @param i
     */
    public void setSize(int i) {
        size = i;
    }

    /**
     * @param string
     */
    public void setType(String string) {
        type = string;
    }

    /**
     * @param b
     */
    public void setUnderlined(boolean b) {
        underlined = b;
    }

}
