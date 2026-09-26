/*
 * Created on 23/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component;

import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;


/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ComponentSkinXML {

    private String name;

    private SkinStateXML defaultState;

    /**
     * Construtor para suportar componentes que possuem apenas o estado default
     * @param defaultState
     */
    public ComponentSkinXML(SkinStateXML defaultState) {
        this.defaultState = defaultState;
    }

    /**
     * Construtor de corpo vazio para dar suporte aos Scroll Bars
     *
     */
    public ComponentSkinXML() {}

    /**
     * @return
     */
    public SkinStateXML getDefaultState() {
        return defaultState;
    }

    /**
     * @param type
     */
    public void setDefaultState(SkinStateXML type) {
        defaultState = type;
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
