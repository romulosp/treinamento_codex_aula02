/**
 * 
 */
package br.gov.caixa.sispl.infra.ui.dlg;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUIListener;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLFrame;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;

/**
 * @author f511896 - Paulo Jorge
 *
 */
public class DlgNotificacaoSituacaoCadastralUsuario extends EFLDialog implements GerenteTimeoutUIListener {
	private static final long serialVersionUID = 1L;
	
	private DefaultUIManager defaultUIManager;
	private SwingWorkerActionListener callBackConfirmacao;
	
	private EFLButton teclaAceito;
	private EFLPanel termoPanel;
    private EFLLabel labelTitulo;
	
	public DlgNotificacaoSituacaoCadastralUsuario(String type, EFLFrame parent, SwingWorkerActionListener callBackConfirmacao) throws Exception {
		super(null, parent);
        
        setModal(true);
        setUndecorated(true);
        setSize(600, 500);
        setLocation((getParent().getWidth() - this.getWidth()) / 2, (getParent().getHeight() - this.getHeight()) / 2);
        getContentPane().add(getPanel());
        setVisible(true);
        
		this.callBackConfirmacao = callBackConfirmacao;
        defaultUIManager = new DefaultUIManager();
	}
	
    private EFLPanel getPanel() {
        if (termoPanel == null) {
        	termoPanel = new EFLPanel(PanelSkin.PANEL_DIALOG);
        	termoPanel.setSize(new Dimension(this.getWidth(), this.getHeight()));
        	termoPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
        	termoPanel.setLayout(null);
        	termoPanel.setLocation(0, 0);
        	termoPanel.add(getLabelTitulo());
        	termoPanel.add(getTeclaAceito());
        	termoPanel.setOpaque(true);
        }
        return termoPanel;
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
	
	private EFLLabel getLabelTitulo() {
        if (labelTitulo == null) {
            labelTitulo = new EFLLabel(LabelSkin.LABEL_DIALOG, "testestsetase"); //$NON-NLS-1$
            labelTitulo.setHorizontalAlignment(EFLLabel.CENTER);
            labelTitulo.setLocation(0, 0);
            labelTitulo.setSize(600, 40);
        }
        return labelTitulo;
    }

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUIListener#notifyTimeout()
	 */
	@Override
	public boolean notifyTimeout() {
		// TODO Auto-generated method stub
		return false;
	}

}
