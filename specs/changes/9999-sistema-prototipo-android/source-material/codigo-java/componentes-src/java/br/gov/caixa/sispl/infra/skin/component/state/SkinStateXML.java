/*
 * Created on 24/03/2005
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
public class SkinStateXML {

    public static final String IMAGE_TAG = "imagePath";
    public static final String COLOR_TAG = "color";
    public static final String RED_TAG = "red";
    public static final String GREEN_TAG = "green";
    public static final String BLUE_TAG = "blue";
    public static final String FONT_TAG = "font";
    public static final String FONT_TYPE_TAG = "type";
    public static final String FONT_SIZE_TAG = "size";
    public static final String FONT_BOLD_TAG = "bold";
    public static final String FONT_ITALIC_TAG = "italic";
    public static final String FONT_UNDERLINED_TAG = "underlined";

    private String imagePath;

    private FontBean font;

    private ColorBean backGroundColor;

    private String name;

    private double spacement;

    /**
     * @return
     */
    public ColorBean getBackGroundColor() {
        return backGroundColor;
    }

    /**
     * @return
     */
    public FontBean getFont() {
        return font;
    }

    /**
     * @return
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param bean
     */
    public void setBackGroundColor(ColorBean bean) {
        backGroundColor = bean;
    }

    /**
     * @param bean
     */
    public void setFont(FontBean bean) {
        font = bean;
    }

    /**
     * @param string
     */
    public void setImagePath(String string) {
        imagePath = string;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    public double getSpacement() {
        return spacement;
    }

    public void setSpacement(double spacement) {
        this.spacement = spacement;
    }

}
