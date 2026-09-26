/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.passwordfield;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PasswordFieldSkin extends ComponentSkin {

    public static final String TEXT_LOGIN = "type1";
    public static final String TYPE2 = "type2";

    private SkinState disabledState;
    private SkinState focusedState;

    /**
     * @return
     */
    public SkinState getDisabledState() {
        return disabledState;
    }

    /**
     * @param state
     */
    public void setDisabledState(SkinState state) {
        disabledState = state;
    }
    /**
     * @return
     */
    public SkinState getFocusedState() {
        return focusedState;
    }

    /**
     * @param state
     */
    public void setFocusedState(SkinState state) {
        focusedState = state;
    }

}
