/*
 * Caixa Econômica Federal
 * SISPL -
 * Arquivo: RegistroPosicaoPnlCrononometro.java
 * Criação: 26/07/2004
 * Implementador: Lellis Marçal Mesquita
 */
package br.gov.caixa.sispl.infra.ui;

/**
 * Classe que contem as posições em que deve se mostrar os relógios dos jogos se encerrando. Esta classe foi
 * criada porque os jogos estão sendo inseridos de forma dinâmica - o primeiro posicao (0,0) o segundo (0,1), ou
 * seja preenche-se a primeira coluna e depois a segunda
 *
 */

public class RegistroPosicaoPnlCrononometro {
    /* linha da tabela */
    private int linha;
    /* coluna da tabela */
    private int coluna;
    /* nome do jogo que está na posicao */
    private String nomeJogo;
    /* se a posicao está ocupada ou não */
    private boolean ocupada;

    /**
       * Construtor do RegistroPosicaoPnlCrononometro 
       * @roseuid 3EB01381009E
       */

    public  RegistroPosicaoPnlCrononometro(int linha,int coluna,String nomeJogo,boolean ocupada) {
        this.linha = linha;
        this.coluna = coluna;
        this.nomeJogo = nomeJogo;
        this.ocupada = ocupada;
    }

    /**
     * retorna o numero da coluna
     * @return
     */
    public int getColuna() {
        return coluna;
    }

    /**
     * retorna o numero da linha
     * @return
     */
    public int getLinha() {
        return linha;
    }

    /**
     * retorna o nome do jogo
     * @return
     */
    public String getNomeJogo() {
        return nomeJogo;
    }

    /**
     * retorna se a posicao está oucupada ou não
     * @return
     */
    public boolean isOcupada() {
        return ocupada;
    }

    /**
     * seta a coluna 
     * @param i
     */
    public void setColuna(int i) {
        coluna = i;
    }

    /**
     * seta a linha
     * @param i
     */
    public void setLinha(int i) {
        linha = i;
    }

    /**
     * seta o nome do jogo
     * @param string
     */
    public void setNomeJogo(String string) {
        nomeJogo = string;
    }

    /**
     * seta a posicao se ocupada ou nao
     * @param b
     */
    public void setOcupada(boolean b) {
        ocupada = b;
    }

}
