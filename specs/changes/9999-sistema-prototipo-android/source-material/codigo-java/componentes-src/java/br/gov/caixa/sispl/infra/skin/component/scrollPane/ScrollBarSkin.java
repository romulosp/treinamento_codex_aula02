/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.scrollPane;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;

/**
 * @author p504116
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ScrollBarSkin extends ComponentSkin {

    public static final String TEST_BAR = "testBar";

    private String rollUpButton;
    private String rollDownButton;
    private String rollRigthButton;
    private String rollLeftButton;

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
     * @return
     */
    public String getRollLeftButton() {
        return rollLeftButton;
    }

    /**
     * @return
     */
    public String getRollRigthButton() {
        return rollRigthButton;
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
    /**
     * @param string
     */

    public void setRollLeftButton(String string) {
        rollLeftButton = string;
    }
    /**
     * @param string
     */
    public void setRollRigthButton(String string) {
        rollRigthButton = string;
    }
}
