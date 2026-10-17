// Source file: C:\\FABRICA_view\\Infra\\src\\java\\br\\gov\\caixa\\sispl\\infra\\ui\\UIEFL.java
// Source file: C:\\SISPL_view\\Infra\\src\\java\\br\\gov\\caixa\\sispl\\infra\\ui\\UIEFL.java

/*
 * Caixa Econômica Federal SISPL - Arquivo : UIEFL.java Criaçao : 25/02/2003 Implementador: Luciano Bohnert
 */
package br.gov.caixa.sispl.infra.ui;

import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.infra.executor.ExecutorException;

/**
 * DOCUMENT ME!
 *
 * @version 1.0 25/02/2003
 * @author Luciano Bohnert
 */
public interface UIEFL {

    /**
     * Constantes utilizadas nos paineis para descobrir se a mensagem deve ser exibida no centro do painel ou no rodape (barra de status).
     */
    // FIXME: Mover constantes para classe/interface mais apropriada futuramente
    public int MENSAGEM_INTERFACE_RODAPE = 0;

    public int MENSAGEM_INTERFACE_CENTRO = 1;

    public int MENSAGEM_INTERFACE_CENTRO_E_RODAPE = 2;

    /**
     * DOCUMENT ME!
     * 
     * @param mensagem
     * @roseuid 3D5BB3DA0067
     */
    public void apresentaMsg(String mensagem);

    /**
     * DOCUMENT ME!
     * 
     * @roseuid 3D5BB3630154
     */
    public void init() throws RegraNegocioException, ExecutorException;

}