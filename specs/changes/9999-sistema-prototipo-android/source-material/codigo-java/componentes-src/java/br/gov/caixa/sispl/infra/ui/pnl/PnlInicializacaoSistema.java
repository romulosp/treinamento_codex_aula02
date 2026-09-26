package br.gov.caixa.sispl.infra.ui.pnl;

import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.util.Messages;

public class PnlInicializacaoSistema extends EFLPanel {
    //Label que apresenta a mensagem.
    private EFLLabel labelMensagem = null;

    /**
    * @param type
    * @param parent
    * @throws Exception
    */
    public PnlInicializacaoSistema() {
        super(PanelSkin.PANEL_EFL);
        initialize();
    }

    /**
    	 * Inicializa componentes gráficos do Dialog
    	 *
    	 */
    public void initialize() {
        this.setLayout(null);
        add(getLabelMensagem());
    }

    /**
    * Método responsável por criar o Label que contém a pergunta a ser respondida.
    * @return EFLLabel Label que contém a pergunta a ser respondida.
    */
    private EFLLabel getLabelMensagem() {
        if (labelMensagem == null) {
            labelMensagem = new EFLLabel(LabelSkin.LABEL_TITULO_1, Messages.getString("PnlInicializacaoSistema.msgIniciando")); //$NON-NLS-1$
            labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
            labelMensagem.setLocation(50, 220);
            labelMensagem.setSize(700, 100);
        }
        return labelMensagem;

    }
}
