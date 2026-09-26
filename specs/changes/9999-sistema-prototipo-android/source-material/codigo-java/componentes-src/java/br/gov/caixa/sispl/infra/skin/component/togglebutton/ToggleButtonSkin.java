/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.togglebutton;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ToggleButtonSkin extends ComponentSkin {

    public static final String BOTAO_PROGNOSTICOS = "botao_prognosticos";
    public static final String BOTAO_PROGNOSTICOS_MES = "botao_prognosticos_mes";
    public static final String BOTAO_PROGNOSTICOS_LOTOMANIA = "botao_prognosticos_lotomania";
    public static final String BOTAO_JOGO_1 = "botao_jogo_1";
    public static final String BOTAO_TECLADO_AUXILIAR_1 = "botao_teclado_auxiliar";
    public static final String BOTAO_UF = "botao_uf";
    public static final String BOTAO_PROGNOSTICO_SURPRESINHA = "botao_prognostico_surpresinha";

    public static final String BOTAO_LOTOGOL = "botao_lotogol";
    public static final String BOTAO_LOTECA = "botao_loteca";

    public static final String BOTAO_TAXAS_DETRAN_SP = "botao_taxas_detran_sp";
    
    public static final String BOTAO_AUXILIAR = "botao_auxiliar";

    private SkinState touchedState;
    private SkinState disabledState;

    /**
     * @return
     */
    public SkinState getDisabledState() {
        return disabledState;
    }

    /**
     * @return
     */
    public SkinState getTouchedState() {
        return touchedState;
    }

    /**
     * @param state
     */
    public void setDisabledState(SkinState state) {
        disabledState = state;
    }

    /**
     * @param state
     */
    public void setTouchedState(SkinState state) {
        touchedState = state;
    }

}
