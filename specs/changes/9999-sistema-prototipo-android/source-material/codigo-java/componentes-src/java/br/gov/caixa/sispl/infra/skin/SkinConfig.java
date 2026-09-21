/*
 * Created on 16/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin;


/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SkinConfig {

    private static String skinXmlPath = "/skin/skin.xml";
    //private static String skinXmlPathTreinamento = "/skin/skin_treinamento.xml";

    /**
     * Pega o SkinXml default ou de treinamento para a EFL.
     * @return: retorna o path do skin default ou de treinamento.
     */
    public static String getSkinXmlPath() {
        return skinXmlPath;

    }

    /**
     * Define o SkinXml default ou de treinamento para a EFL.
     * @param string: path default/treinamento para a EFL.
     */
    public static void setSkinXmlPath(String string) {
        skinXmlPath = string;

    }
}
