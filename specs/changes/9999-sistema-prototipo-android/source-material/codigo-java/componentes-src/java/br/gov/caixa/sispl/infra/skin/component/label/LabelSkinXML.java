/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.label;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkinXML;
import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LabelSkinXML extends ComponentSkinXML {

    private SkinStateXML disabled;

    /**
     * @param states
     */
    public LabelSkinXML() {

        //Define o nome do componente
        setName(ComponentSkin.LABEL);

    }


    /**
     * @return
     */
    public SkinStateXML getDisabled() {
        return disabled;
    }

    /**
     * @param stateXML
     */
    public void setDisabled(SkinStateXML stateXML) {
        disabled = stateXML;
    }

}
