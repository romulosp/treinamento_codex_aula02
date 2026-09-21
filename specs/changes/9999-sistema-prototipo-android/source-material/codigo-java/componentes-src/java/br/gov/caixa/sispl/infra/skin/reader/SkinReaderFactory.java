/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.reader;

import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.JaxbSkinReader;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class SkinReaderFactory {

    /**
     * 
     * @param name
     * @return
     */
    public static SkinReader getSkinReader() {
        return JaxbSkinReader.getInstance();
    }

}
