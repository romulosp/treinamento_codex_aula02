//Source file: C:\\FABRICA_view\\Infra\\src\\java\\br\\gov\\caixa\\sispl\\infra\\ui\\UIException.java
//Source file: C:\\SISPL_view\\Loterias\\SISPL\\fontes_java\\br\\gov\\caixa\\sispl\\infra\\ui\\UIException.java

/*
 * Caixa Econômica Federal
 * SISPL -
 * Arquivo  : UIException.java
 * Criaçao  : 31/08/2002
 * Implementador: Luciano Bohnert
*/
package br.gov.caixa.sispl.infra.ui;

/**
 * Essa classe de exceção reporta todo e qualquer tipo de erro que possa ocorrer na recuperação de um  parâmetro.
 *
 * @version 1.0 31-Ago-2002
 * @author Luciano Bohnert
 */
public class UIException extends Exception {
    /**
     * DOCUMENT ME!
     * 
     * @param msg
     * 
     * @roseuid 3F1DEB9F0310
     */
    public UIException(String msg) {
        super(msg);
    }
}