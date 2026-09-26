/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component;


/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ComponentSkin {

    private SkinState defaultState;

    private String name;

    //Components name
    public static final String BUTTON = "button";
    public static final String PANEL = "panel";
    public static final String LABEL = "label";
    public static final String TABLE = "table";
    public static final String TABLEHEADER = "tableHeader";
    public static final String TEXT_AREA = "textArea";
    public static final String TEXT_FIELD = "textField";
    public static final String PASSWORD_FIELD = "passwordField";
    public static final String SCROLL_BAR = "scrollBar";
    public static final String TOGGLE_BUTTON = "toggleButton";
    public static final String DIALOG = "dialog";
    public static final String LIST = "list";

    /**
     * @return
     */
    public SkinState getDefaultState() {
        return defaultState;
    }

    /**
     * @param state
     */
    public void setDefaultState(SkinState state) {
        defaultState = state;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

}
