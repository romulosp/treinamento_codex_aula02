package br.gov.caixa.sispl.infra.ui.dlg;

import java.util.ArrayList;
import java.util.List;

import br.gov.caixa.sispl.dominio.OfertaCliente;
import br.gov.caixa.sispl.dominio.financeiros.ListaOfertaCliente;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutModuloOfertas;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUIListener;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLFrame;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.EFLReportEvent;
import br.gov.caixa.sispl.infra.ui.EFLReportEventListener;
import br.gov.caixa.sispl.infra.ui.UIIniciaAtendimentoEFL;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;
import br.gov.caixa.sispl.util.Messages;

public class DlgModuloOfertasLista extends EFLDialog implements GerenteTimeoutUIListener {

	private static final Log log = LogFactory.getLog(DlgModuloOfertasLista.class);
	
	private static final long serialVersionUID = 1L;
   
    private DefaultUIManager defaultUIManager;
    
    private EFLReportEvent ofertasLista;
    
    private EFLLabel labelTituloPrincipal = null;

    private List<OfertaCliente> ofertas = new ArrayList<OfertaCliente>();
    private UIIniciaAtendimentoEFL ui;
    
    private OfertaCliente ofertaSelecionada;
    
    private ListaOfertaCliente listaOfertaCliente;
    
    private GerenteTimeoutModuloOfertas customGerenteTimeout;
    
    
    public DlgModuloOfertasLista(EFLFrame parent, UIIniciaAtendimentoEFL ui,  ListaOfertaCliente listaOfertaCliente) throws Exception {
    	super(null, parent);
        this.ui = ui;
        ofertas = listaOfertaCliente.getOfertas();
        this.listaOfertaCliente = listaOfertaCliente;
        this.defaultUIManager = new DefaultUIManager();
        this.customGerenteTimeout = new GerenteTimeoutModuloOfertas();
        startListener();
        
        setModal(true);
        setUndecorated(true);
        setSize(600, 300);
        
        
        
        setLocation((getParent().getWidth() - this.getWidth()) / 2, (getParent().getHeight() - this.getHeight()) / 2);
        
        initialize();
                
        setVisible(true);     
                
        GerenteTimeoutUI.getInstance().start();    
    }
    
    /**
     * Inicia o listener Customizado
     */
    public void startListener() {
    	GerenteTimeoutUI.getInstance().stop();
    	this.customGerenteTimeout.start(this);
    }

	private void initialize() {
		add(getLabelTituloPrincipal());
		add(getOfertasLista());   	
    	addOpcoesOfertasLista();
    }

	protected void setOfertas(List<OfertaCliente> ofertas){
		this.ofertas = ofertas;
	}    
   
	private EFLLabel getLabelTituloPrincipal() {
		if (labelTituloPrincipal == null) {
			labelTituloPrincipal = new EFLLabel(LabelSkin.LABEL_DIALOG,
					Messages.getString("MODULO_OFERTA_LABEL_LISTA_PRODUTO"), EFLLabel.CENTER);
			labelTituloPrincipal.setLocation(0, 0);
			labelTituloPrincipal.setSize(600, 40);

		}
		return labelTituloPrincipal;
	}

    private EFLReportEvent getOfertasLista(){
      if (ofertasLista == null) {
    	  ofertasLista = new EFLReportEvent(600);
    	  ofertasLista.setAutoscrolls(true);
    	  ofertasLista.setLocation(0, 50);
    	  ofertasLista.setSize(630, 330);
    	  ofertasLista.addSelectListener(new OfertasListaActionListener());
      
        EFLPanel cabecalho = ofertasLista.addPanelCabecalho();
        cabecalho.setSize(0, 0);
        addCabecalhoOfertasLista(cabecalho);
      }
      return ofertasLista;
    }

    private void addCabecalhoOfertasLista(EFLPanel cabecalho) {
      EFLLabel labelOfertas = new EFLLabel(LabelSkin.LABEL_REPORT, "");
      labelOfertas.setSize(600, 40);
      labelOfertas.setLocation(20, 0);
      cabecalho.add(labelOfertas);
    }

    private void addOpcoesOfertasLista() {
    	getOfertasLista().removeAll();
    
    	for (OfertaCliente oferta : ofertas) {
    		EFLPanel panel = getOfertasLista().addPanel();
    		panel.setSize(getOfertasLista().getWidth(), 20);
    		EFLLabel label;
    		String name = oferta.getNomeOferta();
    		label = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL, name);	
    		label.setSize(panel.getWidth(), panel.getHeight());
    		label.setLocation(0, 0);
    		panel.add(label);
      }

    }

    private class OfertasListaActionListener implements EFLReportEventListener {
      @Override
      public void onSelected(int index) {
    	  customGerenteTimeout.stop();
    	  ofertaSelecionada = ofertas.get(index); 
          setVisible(false);
          repaint();
          dispose();  	  
         // ui.dlgModuloOfertas(listaOfertaCliente, ofertaSelecionada);
      }
    }
    
    public DefaultUIManager getDefaultUIManager() {
		return defaultUIManager;
	}

    @Override
	public boolean notifyTimeout() {
		if (!ui.isModuloCamapanhaFinalizada()) {
			this.customGerenteTimeout.stop();
			log.debug("DLG MODULO OFERTA FINALIZADO POR TIME OUT");
        	ui.proseguirTransacao();
        	dispose();
        	return true;
        }
		return false;
	}
}
