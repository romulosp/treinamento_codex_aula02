/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.scrollPane;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkinXML;

/**
 * @author p504116
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ScrollBarSkinXML extends ComponentSkinXML {

    private String rollUpButton;
    private String rollDownButton;
    private String rollRigthButton;
    private String rollLeftButton;

    /**
     * @param states
     */
    public ScrollBarSkinXML() {
        //Define o nome do componente
        setName(ComponentSkin.SCROLL_BAR);
    }
    /**
     * @return
     */
    public String getRollDownButton() {
        return rollDownButton;
    }

    /**
     * @return
     */
    public String getRollUpButton() {
        return rollUpButton;
    }

    /**
     * @param string
     */
    public void setRollDownButton(String string) {
        rollDownButton = string;
    }

    /**
     * @param string
     */
    public void setRollUpButton(String string) {
        rollUpButton = string;
    }

    public String getRollLeftButton() {
        return rollLeftButton;
    }
    public void setRollLeftButton(String string) {
        rollLeftButton = string;
    }
    public String getRollRigthButton() {
        return rollRigthButton;
    }
    public void setRollRigthButton(String string) {
        rollRigthButton = string;
    }
}
