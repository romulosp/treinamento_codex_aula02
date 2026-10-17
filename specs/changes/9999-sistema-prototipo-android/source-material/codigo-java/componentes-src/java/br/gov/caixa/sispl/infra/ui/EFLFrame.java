/*
 * Created on 15/03/2005 To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 * Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.propriedades.SuportePropriedades;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.ui.pnl.PnlCronometroJogo;
import br.gov.caixa.sispl.infra.ui.pnl.PnlInicializacaoSistema;
import br.gov.caixa.sispl.infra.ui.pnl.PnlMensagemConfirmada;
import br.gov.caixa.sispl.infra.ui.pnl.PnlTotalizador;
import br.gov.caixa.sispl.infra.util.Calculadora;

/**
 * @author p532406 To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static EFLFrame singleton;

    private SuportePropriedades suportePropriedades;

    private DefaultUIManager defaultUIManager;

    // Label de mensagens do sistema
    private EFLLabel labelMensagemEfl;

    // Labels do cabeçalho
    private static EFLLabel labelRegiao;

    private EFLLabel horaLabel;

    private EFLLabel dataLabel;

    private EFLLabel ajudaLabel;

    private static EFLLabel lotericaLabel;

    private static EFLLabel operadorLabel;

    private static EFLLabel terminalLabel;

    // Botões de ajuda
    private EFLButton botaoAjuda1 = null;

    private EFLButton botaoAjuda2 = null;

    private EFLButton botaoAjuda3 = null;

    private PnlCronometroJogo pnlCronometro = null;

    private PnlTotalizador pnlTotalizador = null;
    private PnlMensagemConfirmada pnlMensagemConfirmada = null;

    //Componente visual de monitoração da rede
    private static MonitorPanel monitorRede;

    /**
     * @param title
     * @throws ExecutorException
     */
    public EFLFrame(String title, SuportePropriedades suportePropriedades)  {
        super(title);
        this.suportePropriedades = suportePropriedades;
        defaultUIManager = new DefaultUIManager();

        if (!System.getProperty("os.name").startsWith("Windows"))
            this.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                               new ImageIcon("").getImage(), new Point(0, 0),
                               "CursorTransparente"));

        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(808, 632);

        labelMensagemEfl = new EFLLabel(LabelSkin.LABEL_MENSAGEM, "");
        labelMensagemEfl.setHorizontalAlignment(SwingConstants.CENTER);
        labelMensagemEfl.setLocation(26, 580);
        labelMensagemEfl.setSize(748, 18);

        horaLabel = new EFLLabel(LabelSkin.LABEL_CABECALHO_EFL, "");
        horaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        horaLabel.setLocation(60, 25);
        horaLabel.setSize(750, 20);

        dataLabel = new EFLLabel(LabelSkin.LABEL_CABECALHO_EFL, "");
        dataLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataLabel.setLocation(75, 5);
        dataLabel.setSize(750, 20);

        ajudaLabel = new EFLLabel(LabelSkin.LABEL_TOTALIZACAO, "Ajuda");
        ajudaLabel.setLocation(750, 65);
        ajudaLabel.setSize(45, 20);

        setPanel(new PnlInicializacaoSistema());
        setVisible(true);

    }

    /**
     * Retorna a intancia do singleton do EFLFrame
     * 
     * @return
     * @throws ExecutorException
     */
    public static EFLFrame getInstance() {
        if (singleton == null) {
            SuportePropriedades suportePropriedades = SuportePropriedades.getInstance();
            singleton = new EFLFrame("SISPL - Caixa Econômica Federal", suportePropriedades);
        }
        return singleton;
    }

    /**
     * Define o contentPane do frame
     * 
     * @param eflPanel
     */
    public void setPanel(EFLPanel eflPanel) {
        eflPanel.add(getMonitorRede());
        eflPanel.add(getBSB());
        eflPanel.add(horaLabel);
        eflPanel.add(dataLabel);
        eflPanel.add(ajudaLabel);
        eflPanel.add(getBotaoAjuda1());
        eflPanel.add(getBotaoAjuda2());
        eflPanel.add(getBotaoAjuda3());
        eflPanel.add(labelMensagemEfl);
        eflPanel.add(getTerminaLabel());
        eflPanel.add(getLotericaLabel());
        eflPanel.add(getProprietarioLabel());
        eflPanel.add(getPnlCronometro());
        //FIXME eflPanel.add(getPnlFilaImpressao());
        eflPanel.add(getPnlTotalizador());
        eflPanel.setPreferredSize(new Dimension(800, 600));
        //Foi necessário fazer isso para resolver um problema que estava ocorrendo na fila de apostas.
        //O LayeredPane guarda vários Components em uma lista, que não se sabe porque às vezes é desordenada,
        //perdendo assim o acesso a última tela.
        this.getLayeredPane().removeAll();
        this.setContentPane(eflPanel);
        this.pack();
        eflPanel.requestFocus();
    }

    /**
     * Este metodo retorna um EFLLabel que insere uma menssagem no rodape da
     * EFL.
     * 
     * @return notaRodape
     */
    public EFLLabel getNotaRodape() {
        return labelMensagemEfl;
    }

    /**
     * Este metodo seta uma mensagem no notaRodape.
     * 
     * @param text
     */
    public void setMensagem(String text) {
        labelMensagemEfl.setText(text);
    }
    

    /**
     * Este metodo seta a hora no horaLabel.
     * 
     * @param horaBrasilia
     */
    public void setHoraBrasilia(String horaBrasilia) {

        horaLabel.setText(horaBrasilia);
    }

    /**
     * Este metodo seta a hora no dataLabel.
     * 
     * @param dataBrasilia
     */
    public void setDataBrasilia(String dataBrasilia) {

        dataLabel.setText(dataBrasilia);
    }

    /**
     * Retorna o label com a região da hora definida
     * 
     * @return
     */
    private static final EFLLabel getBSB() {

        if (labelRegiao == null) {
            labelRegiao = new EFLLabel(LabelSkin.LABEL_CABECALHO_EFL, "BSB - ");
            labelRegiao.setHorizontalAlignment(SwingConstants.CENTER);
            labelRegiao.setLocation(35, 5);
            labelRegiao.setSize(750, 20);

        }
        return labelRegiao;
    }

    /**
     * Retorna o label com o terminal
     * 
     * @return
     */
    private static final EFLLabel getTerminaLabel() {

        if (terminalLabel == null) {
            terminalLabel = new EFLLabel(LabelSkin.LABEL_CABECALHO_EFL, "");
            terminalLabel.setHorizontalAlignment(SwingConstants.LEFT);
            terminalLabel.setLocation(505, 16);
            terminalLabel.setSize(220, 20);

        }
        return terminalLabel;
    }

    /**
     * Retorna o label com o numero da loterica
     * 
     * @return
     */
    private static final EFLLabel getLotericaLabel() {

        if (lotericaLabel == null) {
            lotericaLabel = new EFLLabel(LabelSkin.LABEL_CABECALHO_EFL, "");
            lotericaLabel.setHorizontalAlignment(SwingConstants.LEFT);
            lotericaLabel.setLocation(505, 30);
            lotericaLabel.setSize(220, 20);

        }
        return lotericaLabel;
    }

    /**
     * @return
     */
    private static final EFLLabel getProprietarioLabel() {

        if (operadorLabel == null) {
            operadorLabel = new EFLLabel(LabelSkin.LABEL_CABECALHO_EFL, "");
            operadorLabel.setHorizontalAlignment(SwingConstants.LEFT);
            operadorLabel.setLocation(505, 2);
            operadorLabel.setSize(220, 20);

        }
        return operadorLabel;
    }



    /**
     * Este metodo retorna um botao para que se possa acessar o help do sistema
     * 
     * @return botaoAjuda1 EFLButton
     */
    private EFLButton getBotaoAjuda1() {
        if (botaoAjuda1 == null) {
            botaoAjuda1 = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_TECLADO_2);
            botaoAjuda1.setIcon(new ImageIcon(this.getClass().getResource("/icon_help_1.png")));
            botaoAjuda1.setLocation(655, 55);
            botaoAjuda1.setSize(42, 42);
            botaoAjuda1.addActionListener(new ActionListener() {

                                              public void actionPerformed(ActionEvent e) {
                                                  String versaoEFLAtual = suportePropriedades.getVersaoEFLAtual();
                                                  String versaoEFLBackup = suportePropriedades.getVersaoEFLBackup();
                                                  String versaoEFLDistribuida = suportePropriedades.getVersaoEFLDistribuida();
                                                  String versaoServidor = suportePropriedades.getVersaoServidor();
                                                  int numTerminal = suportePropriedades.getNumTerminal();
                                                  StringBuffer mensagemRodape = new StringBuffer();
                                                  if (numTerminal != 0) {
                                                      mensagemRodape.append("Terminal: " + numTerminal);
                                                  }
                                                  if (versaoEFLAtual != null) {
                                                      mensagemRodape.append("  Versão Atual: " + versaoEFLAtual);
                                                  }
                                                  if (versaoEFLBackup != null) {
                                                      mensagemRodape.append("  Versão Backup: " + versaoEFLBackup);
                                                  }
                                                  if (versaoEFLDistribuida != null) {
                                                      mensagemRodape.append("  Versão Distribuição: " + versaoEFLDistribuida);
                                                  }
                                                  if ((versaoServidor != null) && (!versaoServidor.equals(""))) {
                                                      mensagemRodape.append("  Versão Servidor: " + versaoServidor);
                                                  }
                                                  setMensagem(mensagemRodape.toString());
                                              }
                                          }
                                         );
        }
        return botaoAjuda1;
    }

    /**
    * Este metodo retorna um botao para que se possa acessar o help do sistema
    * 
    * @return botaoAjuda2 EFLButton
    */
    private EFLButton getBotaoAjuda2() {

        if (botaoAjuda2 == null) {
            botaoAjuda2 = new EFLButton(defaultUIManager,
                                        ButtonSkin.BOTAO_TECLADO_2);
            botaoAjuda2.setIcon(new ImageIcon(this.getClass().getResource(
                                                  "/icon_help_2.png")));
            botaoAjuda2.setLocation(705, 55);
            botaoAjuda2.setSize(42, 42);
            /*
            * botaoAjuda1.addActionListener(new ActionListener() { public void
            * actionPerformed(ActionEvent e) {
            * uiPrognosticoNumericoControle.init(); } });
            */
        }
        return botaoAjuda2;
    }

    /**
    * Este metodo retorna um botao para que se possa acessar o help do sistema
    * 
    * @return botaoAjuda1 EFLButton
    */
    private EFLButton getBotaoAjuda3() {
        if (botaoAjuda3 == null) {
            botaoAjuda3 = new EFLButton(defaultUIManager,
                                        ButtonSkin.BOTAO_CALCULADORA);
            botaoAjuda3.setIcon(new ImageIcon(this.getClass().getResource(
                                                  "/azul/botao_calculadora_released.png")));
            botaoAjuda3.setLocation(555, 55);
            botaoAjuda3.setSize(42, 42);
            botaoAjuda3.addActionListener(new ActionListener() {
                                              public void actionPerformed(ActionEvent e) {
                                                  Calculadora.getInstance().toggleVisible();
                                                  EFLFrame.this.requestFocus();
                                                  EFLFrame.this.getContentPane().requestFocusInWindow();
                                              }
                                          }
                                         );
        }
        return botaoAjuda3;
    }

    /**
     * 
     */
    public PnlCronometroJogo getPnlCronometro() {
        if (pnlCronometro == null) {
            pnlCronometro = new PnlCronometroJogo();
        }
        return pnlCronometro;

    }
    

    /**
     * @param string
     */
    public void setLoterico(String loterica) {
        getLotericaLabel().setText(loterica);
    }

    /**
     * Formata o numero da loterica em um valor de 6 posições
     * 
     * @param loterica
     * @return
     */
    private static String formataLoterica(int loterica) {
        DecimalFormat df = new DecimalFormat("000000");
        String val = df.format(loterica);
        return val;
    }

    /**
     * Formata o numero da loterica em um valor de 6 posições
     * 
     * @param loterica
     * @return
     */
    private static String formataTerminal(int terminal) {
        DecimalFormat df = new DecimalFormat("000000");
        String val = df.format(terminal);
        return val;
    }

    /**
     * Define o número do operador
     * 
     * @param string
     */
    public void setNumeroOperador(String operador) {
        getProprietarioLabel().setText(operador);
    }

    /**
     * Define o número do terminal
     * 
     * @param string
     */
    public void setTerminal(String terminal) {
        getTerminaLabel().setText(terminal);
    }

    /**
     * Define o cabeçalho com informações sobre a lotérica logada
     * 
     * @param props
     */
    public void defineHeader(int loterico, int terminal, String nomeUsuario,
                             String usuario, String grupoUsuario) {
        // Lotérico
        setLoterico("LOTÉRICA: " + formataLoterica(loterico));

        // Terminal
        setTerminal("Terminal: " + formataTerminal(terminal));

        // Operador
        String nomeUsuarioTrim = nomeUsuario.trim();
        if (nomeUsuarioTrim == null) {
            nomeUsuarioTrim = "";
        }
        if (nomeUsuarioTrim.length() > 11) {
            nomeUsuarioTrim = nomeUsuarioTrim.substring(0, 11);
        }

        setNumeroOperador(grupoUsuario + ": " + usuario + " - "
                          + nomeUsuarioTrim);

    }

    //FIXME Remover Painel da Infra
//    /**
//     * 
//     * @return
//     */
//    public PnlFilaImpressao getPnlFilaImpressao() {
//        if (pnlFilaImpressao == null)
//            pnlFilaImpressao = new PnlFilaImpressao();
//
//        return pnlFilaImpressao;
//    }

    /**
     * Método que instancia um novo PnlTotalizador
    *	
     * @return Totalizador
     */
    public PnlTotalizador getPnlTotalizador() {
        if (pnlTotalizador == null)
            pnlTotalizador = new PnlTotalizador();

        pnlTotalizador.refresh();
        return pnlTotalizador;
    }

    /**
     * Cria e retorna o componente visual de monitoração da rede.
     * 
     * @return
     */
    private static final MonitorPanel getMonitorRede() {
        if (monitorRede == null) {
            monitorRede = new MonitorPanel();
            monitorRede.setLocation(730, 2);
        }
        return monitorRede;
    }

}
