/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.button;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ButtonSkin extends ComponentSkin {

    public static final String BOTAO_TECLADO = "botao_teclado";
    public static final String BOTAO_TECLADO_2 = "botao_teclado_2";
    public static final String BOTAO_TECLADO_3 = "botao_teclado_3";
    public static final String BOTAO_TECLADO_4 = "botao_teclado_fonte_pequena";
    public static final String BOTAO_TECLADO_AUXILIAR_1 = "botao_teclado_auxiliar_1";
    public static final String BOTAO_TECLADO_AUXILIAR_2 = "botao_teclado_auxiliar_2";
    public static final String BOTAO_TECLADO_AUXILIAR_3 = "botao_teclado_auxiliar_3";
    public static final String BOTAO_ORDENACAO = "botao_ordenacao";
    public static final String BOTAO_SIM = "botao_sim";
    public static final String BOTAO_MODULO_OFERTA = "botao_modulo_oferta";
    public static final String BOTAO_NAO = "botao_nao";
    public static final String BOTAO_CONFIRMAR = "botao_confirmar";
    public static final String BOTAO_SECUNDARIO = "botao_secundario";
    public static final String BOTAO_VOLTAR = "botao_voltar";
    public static final String BOTAO_CANCELAR = "botao_cancelar";
    public static final String BOTAO_LABELS = "botao_two_label";
    public static final String BOTAO_MENU = "botao_menu";
    public static final String BOTAO_MENU2 = "botao_menu2";
    public static final String BOTAO_MENU_SETA = "botao_menu_seta";
    public static final String BOTAO_MENU2_SETA = "botao_menu2_seta";
    public static final String BOTAO_MENU_PEQ = "botao_menu_peq";
    public static final String BOTAO_MENU_PEQ_VERMELHO = "botao_menu_peq_vermelho";
    public static final String BOTAO_MENU_VERDE = "botao_menu_verde";
    public static final String BOTAO_MENU_LARANJA = "botao_menu_laranja";
    public static final String BOTAO_MEGASENA = "botao_megasena";
    public static final String BOTAO_MEGASENA_ESPECIAL = "botao_megasena_especial";
    public static final String BOTAO_LOTOFACIL = "botao_lotofacil";
    public static final String BOTAO_LOTOFACIL_ESPECIAL = "botao_lotofacil_especial";
    public static final String BOTAO_QUINA = "botao_quina";
    public static final String BOTAO_QUINA_ESPECIAL = "botao_quina_especial";
    public static final String BOTAO_LOTOMANIA = "botao_lotomania";
    public static final String BOTAO_LOTOMANIA_ESPECIAL = "botao_lotomania_especial";
    public static final String BOTAO_LOTECA = "botao_loteca";
    public static final String BOTAO_LOTECA_ESPECIAL = "botao_loteca_especial";
    public static final String BOTAO_LOTOGOL = "botao_lotogol";
    public static final String BOTAO_LOTOGOL_ESPECIAL = "botao_lotogol_especial";
    public static final String BOTAO_DUPLASENA = "botao_duplasena";
    public static final String BOTAO_DIA_DE_SORTE = "botao_dia_de_sorte";
    public static final String BOTAO_SUPER_SETE = "botao_super_sete";
    public static final String BOTAO_DUPLASENA_ESPECIAL = "botao_duplasena_especial";
    public static final String BOTAO_FEDERAL = "botao_federal";
    public static final String BOTAO_TIMEMANIA = "botao_timemania";
    public static final String BOTAO_TIMEMANIA_ESPECIAL = "botao_timemania_especial";
    public static final String BOTAO_JOGOS_FUTUROS = "botao_jogos_futuros";
    public static final String BOTAO_CALCULADORA = "botao_menu_calculadora";
    public static final String BOTAO_CALCULADORA_FECHAR = "botao_quadrado";
    public static final String BOTAO_SCROLL_DOWN = "botao_scroll_dn";
    public static final String BOTAO_SOMAR = "botao_somar";
    public static final String BOTAO_DIMINUIR = "botao_diminuir";
    public static final String BOTAO_PIX = "botao_pix";
    public static final String BOTAO_PIX_CONFIRMAR = "botao_pix_confirmar";
    
    public static final String BOTAO_MILIONARIA = "botao_milionaria";
    
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
