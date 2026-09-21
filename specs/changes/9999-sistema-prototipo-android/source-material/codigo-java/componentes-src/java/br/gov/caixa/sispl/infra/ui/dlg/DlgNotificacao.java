package br.gov.caixa.sispl.infra.ui.dlg;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLFrame;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.UIMenuControle;
import br.gov.caixa.sispl.util.Messages;

/**
 * Dialog generico utilizado pelo menu ao verificar que existem notificações de um determinado mecansimo de notificações.
 * @author Tiago de A. van den Berg
 */
public class DlgNotificacao extends EFLDialog {

    private UIMenuControle uiMenuControle;

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

    /**
     * Construtor da caixa de dialogo, 
     * @param parent Frame ao qual o dialog (modal) ira pertencer.
     * @throws Exception
     * @throws Exception
     */
    public DlgNotificacao(EFLFrame parent, UIMenuControle uiMenuControle) throws Exception {
        super(null, parent);
        this.uiMenuControle = uiMenuControle;
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
        show();
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
            notificacaoPanel.setOpaque(true);
        }
        return notificacaoPanel;
    }


    /**
     * Cria o Label que contém o titulo da Dialog. (Normalmente o Nome do caso de Uso)
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelTitulo() {
        if (labelTitulo == null) {
            labelTitulo = new EFLLabel(LabelSkin.LABEL_DIALOG, uiMenuControle.getTituloNotificacaoMostradaNoInstante() + "   "); //$NON-NLS-1$
            labelTitulo.setHorizontalAlignment(EFLLabel.RIGHT);
            labelTitulo.setLocation(1, 20);
            labelTitulo.setSize(400, 28);
        }
        return labelTitulo;

    }

    /**
     * Cria o Label que contém a pergunta a ser respondida.
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelMensagem() {
        if (labelMensagem == null) {
            labelMensagem = new EFLLabel(LabelSkin.LABEL_CONFIRMACAO, uiMenuControle.getMensagemNotificacaoMostradaNoInstante());
            labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
            labelMensagem.setLocation(0, 50);
            labelMensagem.setSize(400, 80);
        }
        return labelMensagem;

    }

    /**
     * Método responsável por criar o Botao de Afirmação que leva a listagem de Notificações
     * @return EFLButton Botao que leva a listagem de Notificações
     */
    private EFLButton getTeclaSim() {
        if (teclaSim == null) {
            teclaSim = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_SIM, Messages.getString("SIM")); //$NON-NLS-1$

            teclaSim.setLocation(70, 130);
            teclaSim.setSize(60, 42);
            teclaSim.setDefaultButton();
            teclaSim.addActionListener(
                new ConfirmaNotificacaoAction(
                    uiMenuControle.getTipoMovimentoNotificacaoMostradaNoInstante(),
                    uiMenuControle.getOpcaoMovimentoNotificacaoMostradaNoInstante()
                )
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
            teclaNao = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_SIM, Messages.getString("NAO")); //$NON-NLS-1$
            teclaNao.setLocation(270, 130);
            teclaNao.setSize(60, 42);
            teclaNao.setCancelButton();
            teclaNao.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                               uiMenuControle.cancelaNotificacao();
                                               dispose();
                                           }
                                       }
                                      );
        }
        return teclaNao;
    }

    private class ConfirmaNotificacaoAction implements ActionListener {
        private int tipoMovimento;

        private int opcaoMovimento;

        public ConfirmaNotificacaoAction(int tipoMovimento, int opcaoMovimento) {
            super();
            this.tipoMovimento = tipoMovimento;
            this.opcaoMovimento = opcaoMovimento;
        }

        public void actionPerformed(ActionEvent e) {
            DlgNotificacao.this.uiMenuControle.confirmaNotificacao(tipoMovimento, opcaoMovimento);
            DlgNotificacao.this.dispose();
        }
    }
}
