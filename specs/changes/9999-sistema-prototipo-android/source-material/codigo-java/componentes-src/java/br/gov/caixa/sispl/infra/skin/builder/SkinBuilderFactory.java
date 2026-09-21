/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.builder;

import br.gov.caixa.sispl.infra.skin.builder.impl.SkinBuilderImpl;


/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SkinBuilderFactory {

    /**
     * 
     * @param componentSkinXml
     * @return
     */
    public static SkinBuilder getSkinBuilder() {
        return SkinBuilderImpl.getInstance();
    }

}
