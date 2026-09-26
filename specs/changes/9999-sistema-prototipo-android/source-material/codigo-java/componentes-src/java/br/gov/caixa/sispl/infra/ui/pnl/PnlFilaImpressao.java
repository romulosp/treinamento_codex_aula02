package br.gov.caixa.sispl.infra.ui.pnl;
//package br.gov.caixa.sispl.infra.ui;
//
//import java.awt.event.ActionEvent;
//
//import javax.swing.ImageIcon;
//import javax.swing.JPanel;
//
//import br.gov.caixa.sispl.infra.controle.FabricaControleMovimento;
//import br.gov.caixa.sispl.dominio.RegraNegocioException;
//import br.gov.caixa.sispl.dominio.TipoMovimento;
//import br.gov.caixa.sispl.infra.executor.ExecutorException;
//import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
//import br.gov.caixa.sispl.infra.util.log.Log;
//import br.gov.caixa.sispl.infra.util.log.LogFactory;
//
//public class PnlFilaImpressao extends JPanel {
//
//    /**
//     * 
//     */
//    private static final long serialVersionUID = 1L;
//    private EFLLabel tamanhoFilaImpressaoLabel;
//    private EFLLabel tamanhoMaximoFilaImpressaoLabel;
//    private EFLLabel filaImpressaoLabel;
//    private EFLLabel deLabel;
//    private EFLButton botaoImpressao;
//    private boolean habilitado;
//    private DefaultUIManager defaultUIManager;
//
//    private static transient final Log log = LogFactory.getLog(PnlFilaImpressao.class);
//
//    public PnlFilaImpressao() {
//        initialize();
//        defaultUIManager = new DefaultUIManager();
//    }
//
//    private void initialize() {
//        setOpaque(false);
//        setLayout(null);
//        setLocation(0, 0);
//        setSize(250, 100);
//        setHabilitado(false);
//
//        if (tamanhoFilaImpressaoLabel == null) {
//            tamanhoFilaImpressaoLabel = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL, "0");
//            tamanhoFilaImpressaoLabel.setLocation(170, 65);
//            tamanhoFilaImpressaoLabel.setSize(50, 20);
//            tamanhoFilaImpressaoLabel.setVisible(false);
//        }
//        if (tamanhoMaximoFilaImpressaoLabel == null) {
//            tamanhoMaximoFilaImpressaoLabel = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL, "0");
//            tamanhoMaximoFilaImpressaoLabel.setLocation(213, 65);
//            tamanhoMaximoFilaImpressaoLabel.setSize(50, 20);
//            tamanhoMaximoFilaImpressaoLabel.setVisible(false);
//        }
//
//        if (deLabel == null) {
//            deLabel = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL, "de");
//            deLabel.setLocation(189, 65);
//            deLabel.setSize(50, 20);
//            deLabel.setVisible(false);
//        }
//
//        if (filaImpressaoLabel == null) {
//            filaImpressaoLabel = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL, "Fila de Impress\343o - ");
//            filaImpressaoLabel.setLocation(60, 65);
//            filaImpressaoLabel.setSize(180, 20);
//            filaImpressaoLabel.setVisible(false);
//        }
//        if (botaoImpressao == null) {
//            botaoImpressao = new EFLButton(defaultUIManager, "botao_teclado_2");
//            botaoImpressao.setIcon(new ImageIcon(getClass().getResource("/icon/Impressora.gif")));
//            botaoImpressao.setLocation(10, 55);
//            botaoImpressao.setSize(42, 42);
//            botaoImpressao.setVisible(false);
//            botaoImpressao.setEnabled(false);
//            botaoImpressao.addActionListener(new SwingWorkerActionListener() {
//
//                                                 public void actionPerformedInBackground(ActionEvent actionEvent) {
//
//                                                     try {
//                                                         FabricaControleMovimento.getInstance().getControleMovimento(TipoMovimento.VISUALIZA_FILA_APOSTA)
//                                                         .init(TipoMovimento.VISUALIZA_FILA_APOSTA, 0);
//                                                     } catch (RegraNegocioException e1) {
//                                                         log.debug(e1.getMessage(), e1);
//                                                     } catch (ExecutorException e1) {
//                                                         log.error(e1.getMessage(), e1);
//                                                     }
//                                                 }
//
//                                             }
//                                            );
//
//        }
//        add(tamanhoFilaImpressaoLabel);
//        add(tamanhoMaximoFilaImpressaoLabel);
//        add(filaImpressaoLabel);
//        add(botaoImpressao);
//        add(deLabel);
//    }
//
//    public void atulizaFilaImpressao(String tamanhoMaximoFilaImpressao, String tamanhoFila) {
//
//        try {
//            if (Integer.parseInt(tamanhoMaximoFilaImpressao) < 10) {
//                tamanhoMaximoFilaImpressao = "0".concat(tamanhoMaximoFilaImpressao);
//            }
//            if (Integer.parseInt(tamanhoFila) < 10) {
//                tamanhoFila = "0".concat(tamanhoFila);
//            }
//
//            tamanhoMaximoFilaImpressaoLabel.setText(tamanhoMaximoFilaImpressao);
//            tamanhoFilaImpressaoLabel.setText(tamanhoFila);
//            tamanhoMaximoFilaImpressaoLabel.setVisible(true);
//            tamanhoFilaImpressaoLabel.setVisible(true);
//            botaoImpressao.setVisible(true);
//            filaImpressaoLabel.setVisible(true);
//            deLabel.setVisible(true);
//            setHabilitado(true);
//
//        } catch (NumberFormatException e) {
//            log.error(e);
//        }
//    }
//
//    public void desabilitaFilaImpressao() {
//        tamanhoMaximoFilaImpressaoLabel.setVisible(false);
//        tamanhoFilaImpressaoLabel.setVisible(false);
//        botaoImpressao.setVisible(false);
//        filaImpressaoLabel.setVisible(false);
//        deLabel.setVisible(false);
//        setHabilitado(false);
//
//    }
//
//    public boolean isHabilitado() {
//        return habilitado;
//    }
//
//    public void setHabilitado(boolean habilitado) {
//        this.habilitado = habilitado;
//    }
//
//    /**
//     * Método para habilitar o botão de impressão no menu principal
//     */
//    public void habilitaBotaoImpressao() {
//
//        if (this.botaoImpressao != null)
//            this.botaoImpressao.setEnabled(true);
//    }
//
//    /**
//     * Método para desabilitar o botão de impressão no painel de fila de apostas.
//     */
//    public void desabilitaBotaoImpressao() {
//        if (this.botaoImpressao != null)
//            this.botaoImpressao.setEnabled(false);
//
//    }
//
//}
