/**
 * Criado em: 30/11/2005
 * Ultima modificacao em: 26/12/2005
 */

package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import br.gov.caixa.sispl.infra.ui.plaf.EFLSelectionListener;

import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;

/**
 * Classe que implementa um Report interno para  o EFLReport3
 * @author P543424 (Jadson Jose dos Santos)
 */
public class EFLInternalReport extends EFLPanel2 {

    /* para compatibilidade com java 5*/
    private static final long serialVersionUID = 0;




    /*   Selecoes que o InternalReport suporta  */
    public static final int SEM_SELECAO = 0;
    public static final int SELECAO_SIMPLES = 1;
    public static final int SELECAO_MULTIPLA = 2;
    public static final int SELECAO_MULTIPLA_NOT_VAZIO = 3; // nao pode ficar todos os paineis sem selecao






    /* Medidas dos paineis(linhas) do InternalReport*/
    private static final int ALTURA_CABECALHO_REPORT_INTERNO = 26;
    private static final int ALTURA_RODAPE_REPORT_INTERNO = 26;

    /* Altura do painel cabecalho do Report interno  */
    private static final int ALTURA_SUB_CABECALHO_REPORT_INTERNO = 26;

    /* Altura dos demais paineis do Report interno*/
    private static final int ALTURA_PAINEL_SIMPLES = 20;


    /* Cabecalho do Report */
    private EFLPanel2 painelCabecalho = null;

    /* Rodape do Report */
    private EFLPanel2 painelRodape = null;




    /* tipo da selecao */
    private int tipoSelecao = 0;

    /*   Largura do Report  */
    private int internalSize = 0;





    /* Lista com os paineis sub cabecalhos*/
    private ArrayList paineisSubCalecalho = null;



    /* Altura total do Report interno */
    private int alturaPainelInterno = 0;


    /* Guarda o indice do painel atualmente selecionado */
    private int indexSelecionado = -1;

    /* Lista dos paineis selecionados. Onde cada painel representa uma linha do Report */
    private ArrayList paineisSelecionados = new ArrayList();

    /* Lista dos paineis com seus indices. Onde cada painel representa uma linha do Report */
    private final Hashtable paineis = new Hashtable();




    /*     Lista que guarda os objetos capazes de responder a uma ação de seleção
     * (ou deseleção) de uma linha deste report. 
     *     Usado apenas se desejar-se que alguma acao seja realiza ao se selecionar
     * algum painel.
     * */
    private ArrayList selectionListeners = new ArrayList();




    /**
     * Construtor padrao
     * @param internalSize
     */
    public EFLInternalReport(int internalSize) {
        this(internalSize, SELECAO_SIMPLES);
    }




    /**
     * Construtor Real
     * @param internalSize - tamanho do report interno
     * @param tipoSelecao- o tipo da selacao que o report aceita
     */
    public EFLInternalReport(int internalSize, int tipoSelecao) {
        super();
        setOpaque(false);
        this.setLayout(null);
        if (tipoSelecao >=0 && tipoSelecao <=3)
            this.tipoSelecao = tipoSelecao;

        this.internalSize = internalSize;
    }





    /*******************************************************************
     *                 Metodos internos da classe                      *
     *******************************************************************/



    /**
     * Calcura a altura total do report
     * @return alturaPainelInterno (altura total do Report Interno)
     */
    private int calculaAlturaReportInterno() {
        /*******************************************************************
         * Altura desse painel eh igual a:                                 *
         *    Altura painelCabeçalho + altura painelRodape +               *
         *    altura dos paineis subCabecalhos + altura dos paineis        *
         *******************************************************************/
        alturaPainelInterno = 0;

        alturaPainelInterno += painelCabecalho != null ? ALTURA_CABECALHO_REPORT_INTERNO : 0;
        alturaPainelInterno += painelRodape != null ? ALTURA_RODAPE_REPORT_INTERNO : 0;
        alturaPainelInterno +=  paineisSubCalecalho != null ? paineisSubCalecalho.size()* ALTURA_SUB_CABECALHO_REPORT_INTERNO : 0;
        alturaPainelInterno += paineis != null ? paineis.size()* ALTURA_PAINEL_SIMPLES : 0;

        return alturaPainelInterno;
    }
    
    
	private int calculaAlturaReportInternoCustomizada(int altura) {
        /*******************************************************************
         * Altura desse painel eh igual a:                                 *
         *    Altura painelCabeçalho + altura painelRodape +               *
         *    altura dos paineis subCabecalhos + altura dos paineis        *
         *******************************************************************/
        alturaPainelInterno = 0;

        alturaPainelInterno += painelCabecalho != null ? ALTURA_CABECALHO_REPORT_INTERNO : 0;
        alturaPainelInterno += painelRodape != null ? ALTURA_RODAPE_REPORT_INTERNO : 0;
        alturaPainelInterno +=  paineisSubCalecalho != null ? paineisSubCalecalho.size()* ALTURA_SUB_CABECALHO_REPORT_INTERNO : 0;
        alturaPainelInterno += paineis != null ? paineis.size()* altura : 0;

        return alturaPainelInterno;
    }




    /**
     * Calcula a posicao que o sub_cabecalho vai ficar
     * OBS.: abaixo do cabecalho e paines sub_cabecalho anteriores
     * @return posicao dos sub cabecalhos
     */
    private int calculaPosicaoSubCabecalho() {
        int posicao = 0;
        posicao += painelCabecalho != null ? ALTURA_CABECALHO_REPORT_INTERNO : 0;
        posicao += paineisSubCalecalho != null ? paineisSubCalecalho.size()* ALTURA_SUB_CABECALHO_REPORT_INTERNO: 0;

        return posicao;
    }




    /**
     * Metodo que calcula a posicao de cada painel no report
     * @return posicao do painel simples dentro do internal report
     */
    private int calculaPosicaoPaineisSimples() {
        int altura = 0;
        altura += painelCabecalho != null ? ALTURA_CABECALHO_REPORT_INTERNO : 0 ;
        altura += paineisSubCalecalho != null ? paineisSubCalecalho.size() * ALTURA_SUB_CABECALHO_REPORT_INTERNO : 0;
        altura += paineis.size() * ALTURA_PAINEL_SIMPLES;

        return altura;
    }

    private int calculaPosicaoPaineisSimplesAlturaCustomizada(int alturaCustomizada) {
        int altura = 0;
        altura += painelCabecalho != null ? ALTURA_CABECALHO_REPORT_INTERNO : 0 ;
        altura += paineisSubCalecalho != null ? paineisSubCalecalho.size() * ALTURA_SUB_CABECALHO_REPORT_INTERNO : 0;
        altura += paineis.size() * alturaCustomizada;

        return altura;
    }

    /********************   fim dos metodos internos da classes  *************************/







    /********************************************************************************
     *                       Interfaces da Classe                                   *
     ********************************************************************************/



    /**
     *     Adiciona um objeto capaz de responder as ações de seleção e deseleção das 
     * linhas deste internal report. 
     * @param listener	Objeto que responderá às ações de seleção e deseleção.
     */
    public void addSelectionListener(EFLSelectionListener listener) {
        selectionListeners.add( listener );
    }




    /**
     *     Remove um listener da lista de objetos capazes de responder as ações de 
     * seleção e deseleção das linhas deste internal report.
     * @param listener	O ouvinte a se removido.
     * @return	<code>true</code> - Caso o objeto passado como parâmetro pertença à lista. <br />
     * 			<code>true</code> - Caso o objeto passado como parâmetro não pertença à lista. <br />
     */
    public boolean removeSelectionListener(EFLSelectionListener listener) {
        return selectionListeners.remove( listener );
    }






    /**
     * Cria o painel cabecalho do Report
     *
     */
    public void addPanelCabecalho(EFLPanel2 painelCabecalho) {
        if (painelCabecalho != null) {  // todo fixo soh cria panel cabecalho uma vez
            this.painelCabecalho = painelCabecalho;
            painelCabecalho.setSkin(PanelSkin.PANEL_REPORT_TITULO);
            painelCabecalho.setPreferredSize(new Dimension(internalSize, ALTURA_CABECALHO_REPORT_INTERNO));
            painelCabecalho.setSize(new Dimension(internalSize, ALTURA_CABECALHO_REPORT_INTERNO));
            painelCabecalho.setLocation(0, 0);
            painelCabecalho.setLayout(null);

            int alturaReportInterno = calculaAlturaReportInterno();

            this.setPreferredSize(new Dimension(internalSize, alturaReportInterno));
            this.setSize(new Dimension(internalSize, alturaReportInterno));
            this.add(painelCabecalho);
            painelCabecalho.repaint();


            //////// atualiza paineis subcabecalhos, paineis simples e rodape /////////

            EFLPanel2 temp;
            int altura = 0;

            if ( paineisSubCalecalho != null) {
                altura += ALTURA_CABECALHO_REPORT_INTERNO;
                temp = (EFLPanel2) paineisSubCalecalho.get(0);
                temp.setLocation(0, altura);
                temp.repaint();

                for (int i = 1; i< paineisSubCalecalho.size() ; i++) {
                    temp = (EFLPanel2) paineisSubCalecalho.get(i);
                    altura += ALTURA_SUB_CABECALHO_REPORT_INTERNO;
                    temp.setLocation(0, altura);
                    temp.repaint();
                }

            }

            altura = 0;
            temp = null;
            if (paineis.size() != 0) {
                altura += painelCabecalho != null ? ALTURA_CABECALHO_REPORT_INTERNO : 0 ;
                altura += paineisSubCalecalho != null ? paineisSubCalecalho.size() * ALTURA_SUB_CABECALHO_REPORT_INTERNO : 0;
                temp = (EFLPanel2) paineis.get(new Integer(0));
                temp.setLocation(0, altura);
                temp.repaint();

                for (int i = 1; i< paineis.size() ; i++) {
                    temp = (EFLPanel2) paineis.get(new Integer(i));
                    altura += ALTURA_PAINEL_SIMPLES;
                    temp.setLocation(0, altura);
                    temp.repaint();
                }
            }

            if (painelRodape != null) {
                painelRodape.setLocation(0, this.getHeight() - ALTURA_RODAPE_REPORT_INTERNO);
                painelRodape.repaint();
            }
            this.repaint();


        }

    }






    /**
     * Cria o painel rodape do Report
     *
     */
    public void addPanelRodape(EFLPanel2 painelRodape) {
        if (painelRodape != null) {
            this.painelRodape = painelRodape;
            painelRodape.setSkin(PanelSkin.PANEL_REPORT_TITULO);
            painelRodape.setPreferredSize(new Dimension(internalSize, ALTURA_RODAPE_REPORT_INTERNO));
            painelRodape.setSize(new Dimension(internalSize, ALTURA_RODAPE_REPORT_INTERNO));

            painelRodape.setLayout(null);

            int alturaReportInterno = calculaAlturaReportInterno();

            this.add(painelRodape);
            this.setPreferredSize(new Dimension(internalSize, alturaReportInterno));
            this.setSize(new Dimension(internalSize, alturaReportInterno));
            painelRodape.repaint();
            this.repaint();


            painelRodape.setLocation(0, this.getHeight() - ALTURA_RODAPE_REPORT_INTERNO);
            painelRodape.repaint();

            ////////////////////////   precisa atualiza  nada  /////////////////////////
        }
    }






    /**
     * Metodo que cria os Paineis Sub Cabeçalho e o adiciona ao panelReport
     */
    public void addPanelSubCabecalho(EFLPanel2 painelSubCabecalho) {
        if (paineisSubCalecalho == null)
            paineisSubCalecalho = new ArrayList();
        if (painelSubCabecalho !=  null ) {
            painelSubCabecalho.setSkin(PanelSkin.PANEL_REPORT_TITULO);
            painelSubCabecalho.setPreferredSize(new Dimension(internalSize, ALTURA_SUB_CABECALHO_REPORT_INTERNO ));
            painelSubCabecalho.setSize(new Dimension(internalSize, ALTURA_SUB_CABECALHO_REPORT_INTERNO ));
            painelSubCabecalho.setLocation(0, calculaPosicaoSubCabecalho());
            painelSubCabecalho.setLayout(null);
            paineisSubCalecalho.add(painelSubCabecalho);

            int alturaReportInterno = calculaAlturaReportInterno();

            this.add(painelSubCabecalho);
            this.setPreferredSize(new Dimension(internalSize, alturaReportInterno));
            this.setSize(new Dimension(internalSize, alturaReportInterno));
            painelSubCabecalho.repaint();

            /////////////////// atualiza paineis simples e rodape  ///////////////////

            EFLPanel2 temp;
            int altura = 0;
            if (paineis.size() != 0) {
                altura += painelCabecalho != null ? ALTURA_CABECALHO_REPORT_INTERNO : 0 ;
                altura += paineisSubCalecalho != null ? paineisSubCalecalho.size() * ALTURA_SUB_CABECALHO_REPORT_INTERNO : 0;
                temp = (EFLPanel2) paineis.get(new Integer(0));
                temp.setLocation(0, altura);
                temp.repaint();

                for (int i = 1; i< paineis.size() ; i++) {
                    temp = (EFLPanel2) paineis.get(new Integer(i));
                    altura += ALTURA_PAINEL_SIMPLES;
                    temp.setLocation(0, altura);
                    temp.repaint();
                }
            }

            if (painelRodape != null) {
                painelRodape.setLocation(0, this.getHeight() - ALTURA_RODAPE_REPORT_INTERNO);
                painelRodape.repaint();
            }
            this.repaint();
        }
    }





    /**
     * Metodo que adicional as linhas no painel.
     * Cada linha é uma painel
     * @param novoPainel
     */
    public void addPanel(EFLPanel2 painel) {
        if (painel != null) {
            //Primeiro painel: selecionado
            if (paineis.size() == 0 ) {
                painel.setSkin(PanelSkin.PANEL_REPORT_SELECTED);
                indexSelecionado = 0;
                paineisSelecionados.add(painel);

                //Demais painéis: sem seleção
            } else {
                painel.setSkin(PanelSkin.PANEL_REPORT_SIMPLES);
            }
            painel.setPreferredSize(new Dimension(internalSize, ALTURA_PAINEL_SIMPLES));
            painel.setSize(new Dimension(internalSize, ALTURA_PAINEL_SIMPLES));
            painel.setLocation(0, calculaPosicaoPaineisSimples());
            painel.setLayout(null);
            painel.addMouseListener(new EFLReportActionListener(this, paineis.size()));

            // OBS: paineisLinhas.size() é o indice do painel
            paineis.put(new Integer(paineis.size()), painel);
            int alturaReportInterno = calculaAlturaReportInterno();

            this.setPreferredSize(new Dimension(internalSize, alturaReportInterno));
            this.setSize(new Dimension(internalSize, alturaReportInterno));
            this.add(painel);


            //////////////////// atualiza o rodape  //////////////////////////////
            if (painelRodape != null) {
                painelRodape.setLocation(0, this.getHeight() - ALTURA_RODAPE_REPORT_INTERNO);
                painelRodape.repaint();
            }

            painel.repaint();
            this.repaint();
        }
    }
    
    public void addPanel(EFLPanel2 painel, int altura) {
        if (painel != null) {
            //Primeiro painel: selecionado
            if (paineis.size() == 0 ) {
                painel.setSkin(PanelSkin.PANEL_REPORT_SELECTED);
                indexSelecionado = 0;
                paineisSelecionados.add(painel);
            } else {
                painel.setSkin(PanelSkin.PANEL_REPORT_SIMPLES);
            }
            painel.setPreferredSize(new Dimension(internalSize, altura));
            painel.setSize(new Dimension(internalSize, altura));
            painel.setLocation(0, calculaPosicaoPaineisSimplesAlturaCustomizada(altura));
            painel.addMouseListener(new EFLReportActionListener(this, paineis.size()));

            // OBS: paineisLinhas.size() é o indice do painel
            paineis.put(new Integer(paineis.size()), painel);
            int alturaReportInterno = calculaAlturaReportInternoCustomizada(altura);

            this.setPreferredSize(new Dimension(internalSize, alturaReportInterno));
            this.setSize(new Dimension(internalSize, alturaReportInterno));
            this.add(painel);


            //////////////////// atualiza o rodape  //////////////////////////////
            if (painelRodape != null) {
                painelRodape.setLocation(0, this.getHeight() - ALTURA_RODAPE_REPORT_INTERNO);
                painelRodape.repaint();
            }

            painel.repaint();
            this.repaint();
        }
    }
    





    /**
     * Metodo que retorna a altura do InternalReport
     * @return altura do InternalReport
     */
    public int getAltura() {
        return alturaPainelInterno;
    }





    /**
     * Metodo que retorna o indice do painel selecionado
     * @return Se tipoSelecao == SELECAO_SIMPLES retorna indice Senao returna -1
     */
    public int getSelected() {
        return indexSelecionado;
    }






    /**
     * Metodo que retorna o tipo de selecao do painel
     * @return tipoSelecao (tipo da selecao do painel)
     */
    public int getTipoSelecao() {
        return tipoSelecao;
    }






    /**
     * Metodo que retorna o painel selecionado no caso de SELECAO SIMPLES
     * @return EFLPanel2 (painel atualmente selecionado no caso de SELECAO SIMPLES)
     */
    public EFLPanel2 getPainelSelecionado() {
        if (tipoSelecao == SELECAO_SIMPLES)
            return (EFLPanel2) paineis.get(new Integer(indexSelecionado));
        else
            return null;
    }






    /**
     * Método que retorna os painéis que estão selecionados.
     * @return	Se SELECAO_MULTIPLA ou SELECAO_MULTIPLA_NOT_VAZIO uma lista contendo instâncias de <code>EFLPanel</code>
     * Senao null
     */
    public List getPaineisSelecionados() {
        if (tipoSelecao == SELECAO_MULTIPLA || tipoSelecao == SELECAO_MULTIPLA_NOT_VAZIO)
            return paineisSelecionados;
        else
            return null;
    }

    /**
     * Retorna uma lista com os painéis não selecionados.
     * @return lista dos painéis não selecionados
     */
    public List getPaineisNaoSelecionados() {
        ArrayList listaNaoSelecionados = new ArrayList();

        //Adiciona todos os painéis na lista
        for (int i = 0; i < paineis.size() ; i++) {
            listaNaoSelecionados.add( (EFLPanel2) paineis.get(new Integer(i)) );
        }

        //Remove da lista os painéis selecionados
        if (tipoSelecao == SELECAO_SIMPLES) {
            listaNaoSelecionados.remove( (EFLPanel2) paineis.get(new Integer(indexSelecionado)) );

        } else {
            Iterator it = getPaineisSelecionados().iterator();
            while (it.hasNext()) {
                listaNaoSelecionados.remove( (EFLPanel2) it.next() );
            }
        }

        return listaNaoSelecionados;
    }

    /**
     * Desfaz a seleção de todos os painéis selecionados.
     */
    public void deselectAll() {
        // Seção simples
        if (tipoSelecao == SELECAO_SIMPLES && indexSelecionado != -1) {
            //Pega o painel atualmente marcado e desmarca
            EFLPanel2 panelTemp2 = (EFLPanel2) paineis.get(new Integer(indexSelecionado));
            panelTemp2.setSkin(PanelSkin.PANEL_REPORT_SIMPLES);
            //Invalida o índice
            indexSelecionado = -1;
        }

        // Seleção múltipla
        else if (tipoSelecao == SELECAO_MULTIPLA) {

            Iterator panels = getPaineisSelecionados().listIterator();
            synchronized (paineisSelecionados) {
                while (panels.hasNext()) {
                    EFLPanel2 panelTemp= (EFLPanel2) panels.next();
                    panelTemp.setSkin(PanelSkin.PANEL_REPORT_SIMPLES);
                    //Remove do array dos selecionados
                    panels.remove();
                }
            }
            // Invalida o índice
            indexSelecionado = -1;

            // Seleção múltipla não-vazio: pelo menos 1 deve estar marcado
        } else if (tipoSelecao == SELECAO_MULTIPLA_NOT_VAZIO) {
            // Lista de painéis selecionados
            Iterator panels = getPaineisSelecionados().listIterator();
            // Painel marcado por último no índice
            EFLPanel2 p1 = (EFLPanel2) paineis.get(new Integer(indexSelecionado));

            synchronized (paineisSelecionados) {
                while (panels.hasNext()) {
                    EFLPanel2 p2 = (EFLPanel2) panels.next();

                    // Ignorando o painel do índice
                    if (!p1.equals(p2)) {
                        p2.setSkin(PanelSkin.PANEL_REPORT_SIMPLES);
                        //Remove do array dos selecionados
                        panels.remove();
                    }
                }//end-while
            }//end-synchonized
        }//end-else
    }


    /**
     * Seleciona todos os painéis.
     */
    public void selectAll() {
        paineisSelecionados = new ArrayList(); // Limpa todos os painéis que estavam previamente selecionados

        for (int i=0; i < paineis.size(); i++) {
            // Cria um item com referência para o armazenado na lista
            EFLPanel2 panelTemp = (EFLPanel2) paineis.get(new Integer(i));
            // Define o skin como selecionado
            panelTemp.setSkin(PanelSkin.PANEL_REPORT_SELECTED);
            // Adiciona o item à lista dos selecionados
            paineisSelecionados.add(panelTemp);
        }
    }

    /***********************  fim da interface da classe  ***************************/








    /**************************************************************************
     *                Metodos de Tratamentos de eventos                       *
     **************************************************************************/




    /**
     * Metodo que configura quais painel estao selecionados
     * É sempre chamando quando o usuario seleciona algum painel
     */
    public void setSelecionados(int index) {
        /*************************************************************
         * Se for simples selecao:                                   *
         *    Apaga o atual selecionada e tira da lista e seleciona  *
         * o novo e coloca na lista                                  *
         *                                                           *
         * Se for multipla selecao:                                  *
         *    Se NAO tiver na lista de selecionados: SELECIONA e     * 
         * coloca na lista de selecionados                           *
         *    Se JA TIVER na lista dos paineis selecionados:         *
         * TIRA A SELECAO o retira da lista dos selecionados         *
         *                                                           *
         * Se for multipla selecao not vazio:                        *
         *    Igual ao caso anterior, mas                            *
         *    Se tamanho da lista = 1 NAO REMOVE da lista, NEM       * 
         * REMOVE a SELECAO.                                          *
         *************************************************************/


        if (tipoSelecao == SELECAO_SIMPLES) {
            EFLPanel2 panelTemp2;

            // pega o painel atualmente selecionado e des seleciona (caso haja algum painel selecionado)
            if (indexSelecionado != -1) {
                panelTemp2 = (EFLPanel2) paineis.get(new Integer(indexSelecionado));
                panelTemp2.setSkin(PanelSkin.PANEL_REPORT_SIMPLES);
            }

            //pega o painel clicado e seleciona
            panelTemp2 = (EFLPanel2) paineis.get(new Integer(index));
            panelTemp2.setSkin(PanelSkin.PANEL_REPORT_SELECTED);

            indexSelecionado = index;

            //Seleção múltipla
        } else {
            //Obtém o objeto da Hashtable a patir do índice
            EFLPanel2 panelTemp = (EFLPanel2) paineis.get(new Integer(index));

            //Verifica se esse objeto está na lista dos selecionados...
            if (!paineisSelecionados.contains(panelTemp)) {
                //Marca painel como selecionado
                panelTemp.setSkin(PanelSkin.PANEL_REPORT_SELECTED);
                //Adiciona no array dos selecionados
                paineisSelecionados.add(panelTemp);

                /*
                 Se não estiver, verifica se o painel tem mais de um item selecionado
                para que não fique sem itens selecionados para o caso de seleção
                múltipla não-vazio. Para seleção múltipla normal, apenas desmarca o painel.
                */
            } else if (tipoSelecao == SELECAO_MULTIPLA ||
                       (tipoSelecao == SELECAO_MULTIPLA_NOT_VAZIO && paineisSelecionados.size() > 1)) {
                //Desmarca painel
                panelTemp.setSkin(PanelSkin.PANEL_REPORT_SIMPLES);
                //Remove do painel dos selecionados
                paineisSelecionados.remove(panelTemp);
            }
        }
        posSelecao();
    }





    /**
     * Classe privada interna que trata os eventos de selecionamento dos reports internos
     * @author P543424 (Jadson Jose dos Santos)
     *
     */
    private class EFLReportActionListener extends MouseAdapter {

        /* Indice do painel selecionado */
        private int index;

        /* Uma referencia a classe externa */
        private EFLInternalReport internalReport;


        /**
         * Construtor
         * @param internalReport
         * @param index
         */
        public EFLReportActionListener(EFLInternalReport internalReport, int index) {
            this.index = index;
            this.internalReport = internalReport;
        }

        /**
         * Metodo chamado quando se seleciona um painel
         */
        public void mouseClicked(MouseEvent e) {
            if (tipoSelecao != SEM_SELECAO)
                internalReport.setSelecionados(index);  // chama o metodo da classe externa resposavel pelo tratamento do evento

            /**********************************************************************
             *    Percorre a lista dos objetos que implementam SelectionListener  *
             * e estao adicionados a esse Internal Report e chama o metodo        *
             * selectionPerformed de cada um pra tratarem o evento.               *
             **********************************************************************/
            Iterator it = selectionListeners.iterator();
            while (it.hasNext() ) {
                ((EFLSelectionListener)it.next()).selectionPerformed();
            }
            posSelecao();
        }

    } ////////////////////           fim da classe interna        //////////////////////////


    /**
     * Método que deve ser implementado nas classes filhas. Executa uma ação após clicar em um dos panels internos ao EFLInternalReport
     */
    protected void posSelecao() {}




	public Hashtable getPaineis() {
		return paineis;
	}


    /****************************  fim dos metodos de tratamento de evento *************************/



    


}  // fim da classe InternalReport



