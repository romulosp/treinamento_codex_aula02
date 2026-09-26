package br.gov.caixa.sispl.infra.ui.dlg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import br.gov.caixa.sispl.dominio.TermoConfidencialidade;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUIListener;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLFrame;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.EFLTextArea;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.UIAbstractControle;

/**
 *  
 * @author Rômulo
 */
public class DlgTermoConfidencialidade extends EFLDialog implements GerenteTimeoutUIListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
    private DefaultUIManager defaultUIManager;

    //Painel que conterá os componentes e será usado como contentPane do Dialog.
    private EFLPanel termoPanel;
    
    private EFLLabel labelTitulo;

    private EFLLabel labelDeclaracao;

	private TermoConfidencialidade termoConfidencialidadeSelecionado;

    private EFLTextArea textoMensagem;
    
    private EFLLabel labelMensagem;
    
	private EFLButton teclaAceito = null;
	private EFLButton teclaCancelar = null;

	private SwingWorkerActionListener callBackConfirmacao;
	private SwingWorkerActionListener callBackCancelamento;
    
    /**
     * Construtor da caixa de dialogo, 
     * @param parent Frame ao qual o dialog (modal) ira pertencer.
     * @param callBackConfirmacao 
     * @throws Exception
     * @throws Exception
     */
    public DlgTermoConfidencialidade(EFLFrame parent, UIAbstractControle ui,  TermoConfidencialidade termoConfidencialidadeSelecionado, SwingWorkerActionListener callBackConfirmacao,SwingWorkerActionListener callBackCancelamento ) throws Exception {
       	super(null, parent);
		this.termoConfidencialidadeSelecionado = termoConfidencialidadeSelecionado;
		this.callBackConfirmacao = callBackConfirmacao;    
		this.callBackCancelamento = callBackCancelamento;
        defaultUIManager = new DefaultUIManager();
        startListener();
        
        setModal(true);
        setUndecorated(true);
        setSize(600, 500);
        setLocation((getParent().getWidth() - this.getWidth()) / 2, (getParent().getHeight() - this.getHeight()) / 2);
        getContentPane().add(getPanel());
        setVisible(true);     
        
    }
    
    /**
     * Inicia o listener Customizado
     */
    public void startListener() {
    	GerenteTimeoutUI.getInstance().stop();
    }
    

    /**
     * Cria o Painel que será usado como contentPane do Dialog.
     * @return EFLPanel Painel que será usado como contentPane do Dialog.
     */
    private EFLPanel getPanel() {
        if (termoPanel == null) {
        	termoPanel = new EFLPanel(PanelSkin.PANEL_DIALOG);
        	termoPanel.setSize(new Dimension(this.getWidth(), this.getHeight()));
        	termoPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
        	termoPanel.setLayout(null);
        	termoPanel.setLocation(0, 0);
        	termoPanel.add(getLabelTitulo());
        	termoPanel.add(getLabelDeclaracao());
        	termoPanel.add(getTextoMensagem());
        	termoPanel.add(getTeclaAceito());
        	termoPanel.add(getTeclaCancelar());
        	termoPanel.setOpaque(true);
        }
        return termoPanel;
    } 
    
    public EFLButton getTeclaCancelar() {
    	if (teclaCancelar == null) {
    		teclaCancelar = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_CANCELAR, "CANCELAR");
			teclaCancelar.setSize(80, 60);
			teclaCancelar.setLocation(teclaAceito.getLocation().x + 120, teclaAceito.getLocation().y);
			teclaCancelar.setDefaultButton();
			teclaCancelar.addActionListener(new SwingWorkerActionListener() {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformedInBackground(ActionEvent actionEvent) {
					dispose();
					teclaCancelar.setEnabled(false);
					teclaCancelar.repaint();
					callBackCancelamento.actionPerformed(actionEvent);
				}
			});
		}
		return teclaCancelar;
	}
    private EFLButton getTeclaAceito() {
		if (teclaAceito == null) {
			teclaAceito = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_CONFIRMAR, "ACEITO");
			teclaAceito.setSize(80, 60);
			teclaAceito.setLocation((450 - 40) / 2, 410);
			teclaAceito.setDefaultButton();
			teclaAceito.addActionListener(new SwingWorkerActionListener() {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformedInBackground(ActionEvent actionEvent) {
					dispose();
					teclaAceito.setEnabled(false);
					teclaAceito.repaint();
					callBackConfirmacao.actionPerformed(actionEvent);
				}
			});
		}
		return teclaAceito;
	}
    
    public EFLTextArea getTextoMensagem() {
        if (textoMensagem == null) {
            String textoFormatado = null;
            textoFormatado = getMensagemTermo();
//            textoFormatado = textoFormatado.replaceAll("\\\\t","\t");
            textoMensagem = new EFLTextArea(null, textoFormatado);
            textoMensagem.setSize(500, 280);
            textoMensagem.setLocation(40, 100);
            textoMensagem.setEditable(false);
            textoMensagem.setForeground(new Color (77,100,150));
            textoMensagem.setBackground(new Color(170,190,217));
            textoMensagem.setWrapStyleWord(true);
            textoMensagem.setAlignmentX(EFLLabel.CENTER);
            textoMensagem.setLineWrap(true);
        }
        return textoMensagem;
    }
    
    
    /**global
     * Cria o Label que contém o titulo da Dialog. (Normalmente o Nome do caso de Uso)
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelTitulo() {
        if (labelTitulo == null) {
            labelTitulo = new EFLLabel(LabelSkin.LABEL_DIALOG, termoConfidencialidadeSelecionado.getNoTermo()); //$NON-NLS-1$
            labelTitulo.setHorizontalAlignment(EFLLabel.CENTER);
            labelTitulo.setLocation(0, 0);
            labelTitulo.setSize(600, 40);
        }
        return labelTitulo;
    }

    private EFLLabel getLabelDeclaracao() {
        if (labelDeclaracao == null) {
            labelDeclaracao = new EFLLabel(LabelSkin.LABEL_CONFIRMACAO,  "Declaro estar ciente:");
            labelDeclaracao.setHorizontalAlignment(EFLLabel.LEFT);
            labelDeclaracao.setLocation(30, 50);
            labelDeclaracao.setSize(600, 40);
        }
        return labelDeclaracao;
    }
    
	private String getMensagemTermo() {
		return termoConfidencialidadeSelecionado.getDeTermo().trim();
	}

	@Override
	public boolean notifyTimeout() {
		return false;
	}

	public TermoConfidencialidade getTermoConfidencialidadeSelecionado() {
		return termoConfidencialidadeSelecionado;
	}

	public void setTermoConfidencialidadeSelecionado(TermoConfidencialidade termoConfidencialidadeSelecionado) {
		this.termoConfidencialidadeSelecionado = termoConfidencialidadeSelecionado;
	}
}
