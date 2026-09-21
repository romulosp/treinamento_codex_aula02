/*
 * Created on 16/04/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui.dlg;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import br.gov.caixa.sispl.infra.controle.ColecaoInfoMovimento;
import br.gov.caixa.sispl.infra.executor.ExecutorReenvioObserver;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLFrame;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;

/**
 * @author Achilles P. Brelaz
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DlgReenvio extends EFLDialog implements ExecutorReenvioObserver {

    private static final Log log = LogFactory.getLog(DlgReenvio.class);

    private DefaultUIManager defaultUIManager;

    //Painel que conterá os componentes e será usado como contentPane do Dialog.
    private EFLPanel notificacaoPanel;

    //Label que apresenta a mensagem.
    private EFLLabel labelTitulo;

    //Label que apresenta a mensagem.
    private EFLLabel labelMensagem;

    //Botao de Afirmação: Leva a listagem de Notificações
    private EFLButton teclaSim;

    //Botao de Negação: Cancela a operação e continua na tela atual
    private EFLButton teclaNao;

    //Qual botão foi pressionado (SIM - true; NÃO - false
    private boolean answer = false;

    //Coleção que armazena nomes dos casos de uso.
    private ColecaoInfoMovimento colecaoInfoMovimento;

    /**
     * Construtor da caixa de dialogo, 
     * @param parent Frame ao qual o dialog (modal) ira pertencer.
     * @throws Exception
     */
    public DlgReenvio(EFLFrame parent) throws Exception {
        super(null, parent);
        colecaoInfoMovimento = ColecaoInfoMovimento.getInstance();
        defaultUIManager = new DefaultUIManager();
        setModal(true);
        initialize();
    }
    /**
     * Inicializa componentes gráficos do Dialog
     *
     */
    public void initialize() {
        setUndecorated(true);
        setSize(400, 180);
        setLocation((getParent().getWidth() - this.getWidth()) / 2, (getParent().getHeight() - this.getHeight()) / 2);
        getContentPane().add(getPanel());
    }

    /**
     * Cria o Painel que será usado como contentPane do Dialog.
     * @return EFLPanel Painel que será usado como contentPane do Dialog.
     */
    private EFLPanel getPanel() {
        if (notificacaoPanel == null) {
            notificacaoPanel = new EFLPanel(PanelSkin.PANEL_DIALOG);
            notificacaoPanel.setSize(new Dimension(this.getWidth(), this.getHeight()));
            notificacaoPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
            notificacaoPanel.setLayout(null);
            notificacaoPanel.setLocation(0, 0);
            notificacaoPanel.add(getLabelTitulo());
            notificacaoPanel.add(getLabelMensagem());
            notificacaoPanel.add(getTeclaSim());
            notificacaoPanel.add(getTeclaNao());
            notificacaoPanel.setOpaque(false);
        }
        return notificacaoPanel;
    }


    /**
     * Cria o Label que contém o titulo da Dialog. (Normalmente o Nome do caso de Uso)
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelTitulo() {
        if (labelTitulo == null) {
            labelTitulo = new EFLLabel(LabelSkin.LABEL_DIALOG, "");
            labelTitulo.setHorizontalAlignment(EFLLabel.RIGHT);
            labelTitulo.setLocation(1, 20);
            labelTitulo.setSize(400, 28);
        }
        return labelTitulo;

    }

    private void setMensagemTitulo(String mensagemTitulo) {
        getLabelTitulo().setText(mensagemTitulo);
    }

    /**
     * Cria o Label que contém a pergunta a ser respondida.
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelMensagem() {
        if (labelMensagem == null) {
            labelMensagem = new EFLLabel(LabelSkin.LABEL_CONFIRMACAO, "HOUVE UM PROBLEMA NA COMUNICAÇÃO.\nDESEJA REENVIAR A SOLICITAÇÃO?");
            labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
            labelMensagem.setLocation(0, 60);
            labelMensagem.setSize(400, 60);
        }
        return labelMensagem;

    }

    /**
     * Método responsável por criar o Botao de Afirmação que leva a listagem de Notificações
     * @return EFLButton Botao que leva a listagem de Notificações
     */
    private EFLButton getTeclaSim() {
        if (teclaSim == null) {
            teclaSim = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_SIM, "SIM");
            teclaSim.setLocation(70, 130);
            teclaSim.setSize(60, 42);
            teclaSim.addActionListener(new SwingWorkerActionListener() {
                                           public void actionPerformedInBackground(ActionEvent e) {
                                               answer = true;
                                               hide();
                                           }
                                       }
                                      );
        }
        return teclaSim;
    }

    /**
     * Método responsável por criar o Botao de Negação que cancela o caso de uso. 
     * @return EFLLabel Botao que cancela o caso de uso.
     */
    private EFLButton getTeclaNao() {
        if (teclaNao == null) {
            teclaNao = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_SIM, "NÃO");
            teclaNao.setLocation(270, 130);
            teclaNao.setSize(60, 42);
            teclaNao.addActionListener(new SwingWorkerActionListener() {
                                           public void actionPerformedInBackground(ActionEvent e) {
                                               answer = false;
                                               hide();
                                           }
                                       }
                                      );
        }
        return teclaNao;
    }
    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.executor.ExecutorReenvioObserver#resendQuestion()
     */
    public boolean resendQuestion(int tipoMovimento) {
        String nomeMovimento;
        nomeMovimento = colecaoInfoMovimento.getDescricao(tipoMovimento);
        setMensagemTitulo("Reenvio - " + nomeMovimento + "     ");
        log.info("MENSAGEM: DESEJA REENVIAR A SOLICITAÇÃO?");
        this.setVisible(true);
        return answer;
    }

}
