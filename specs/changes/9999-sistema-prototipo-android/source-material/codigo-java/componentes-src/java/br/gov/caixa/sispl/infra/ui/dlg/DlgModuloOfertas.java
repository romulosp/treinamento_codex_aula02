package br.gov.caixa.sispl.infra.ui.dlg;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import br.gov.caixa.sispl.dominio.OfertaCliente;
import br.gov.caixa.sispl.dominio.financeiros.ListaOfertaCliente;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutModuloOfertas;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUIListener;
import br.gov.caixa.sispl.infra.ui.DefaultUIManager;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLFrame;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.EFLReportEvent;
import br.gov.caixa.sispl.infra.ui.EFLReportEventListener;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.UIIniciaAtendimentoEFL;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;
import br.gov.caixa.sispl.util.MensagemErro;
import br.gov.caixa.sispl.util.Messages;

/**
 *  
 * @author Rommel Alexandre Vasconcelos
 */
public class DlgModuloOfertas extends EFLDialog   implements GerenteTimeoutUIListener {

	private static final Log log = LogFactory.getLog(DlgModuloOfertas.class);
	
	public static final String TITULO_MODULO_OFERTAS = "OPORTUNIDADES DE NEGÓCIOS!!!";
	private static final String TITLE_1 = "Seu cliente pode ter ofertas incríveis de produtos";
	private static final String TITLE_2 = "selecionados de forma personalizada! Oferte e";
	private static final String TITLE_3 = "alavanque seus resultados!";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UIIniciaAtendimentoEFL ui;	
	
    private DefaultUIManager defaultUIManager;

    //Painel que conterá os componentes e será usado como contentPane do Dialog.
    private EFLPanel moduloOfertasPanel;
    
    private EFLLabel labelTitulo;

    private EFLLabel labelMensagem;
    
    private EFLLabel labelOfertaDescricao;
    
    private EFLLabel labelOfertaNome;
    
    private OfertaCliente ofertaSelecionada;

    private EFLButton teclaOfertarDepois;

    private EFLButton teclaVerOutras;
    
    private EFLButton teclaVer;
    
    private EFLButton teclaSimular;
    
    private EFLButton teclaNao;
    
    private ListaOfertaCliente listaOfertaCliente;
    
    private GerenteTimeoutModuloOfertas customGerenteTimeout;
    
    private EFLLabel tituloLabel1;
    
    private EFLLabel tituloLabel2;
    
    private EFLLabel tituloLabel3;
    
   /* 
    * Components list de ofertas do modulo de ofertas
    */

	private EFLPanel moduloListaOfertasPanel;

	private EFLLabel labelTituloPrincipalModuloList;

	private EFLReportEvent ofertasLista;

    /**
     * Construtor da caixa de dialogo, 
     * @param parent Frame ao qual o dialog (modal) ira pertencer.
     * @throws Exception
     * @throws Exception
     */
    public DlgModuloOfertas(EFLFrame parent, UIIniciaAtendimentoEFL ui,  ListaOfertaCliente listaOfertaCliente, OfertaCliente ofertaSelecionada) throws Exception {
       
       	super(null, parent);
       	
        this.ui = ui;        
        defaultUIManager = new DefaultUIManager();
        this.listaOfertaCliente = listaOfertaCliente;
        this.customGerenteTimeout = new GerenteTimeoutModuloOfertas();
        startListener();
        
        setModal(true);
        setUndecorated(true);
        setSize(600, 300);
        setLocation((getParent().getWidth() - this.getWidth()) / 2, (getParent().getHeight() - this.getHeight()) / 2);
        
        carregaOferta(ofertaSelecionada);
        
        if(listaOfertaCliente.isOfertaManual()) {
        	getContentPane().add(getPanel());
        	getContentPane().add(getPanelListaOFertas());
        	getPanelListaOFertas().setVisible(false);
        } else    {        	
        	getContentPane().add(getPanelInicio());
        }
        
        setVisible(true);     
        
    }
    
	/**
     * Inicia o listener Customizadol00
     */
    public void startListener() {
    	GerenteTimeoutUI.getInstance().stop();
    	this.customGerenteTimeout.start(this);
    }
    
    /**
     * Carrega a oferta para o cliente
     */
    private void carregaOferta(OfertaCliente ofertaSelecionada) {
        
        if (ofertaSelecionada != null) {
        	
        	this.ofertaSelecionada = ofertaSelecionada;
        	
        }else {
        	
        	this.ofertaSelecionada = listaOfertaCliente.getOfertas().get(0);
		
        }
    }      
    

    /**
     * Cria o Painel que será usado como contentPane do Dialog.
     * @return EFLPanel Painel que será usado como contentPane do Dialog.
     */
    private EFLPanel getPanel() {
        if (moduloOfertasPanel == null) {
        	moduloOfertasPanel = new EFLPanel(PanelSkin.PANEL_DIALOG);
        	moduloOfertasPanel.setSize(new Dimension(this.getWidth(), this.getHeight()));
        	moduloOfertasPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
        	moduloOfertasPanel.setLayout(null);
        	moduloOfertasPanel.setLocation(0, 0);
        	moduloOfertasPanel.add(getLabelTitulo());
        	moduloOfertasPanel.add(getLabelMensagem());
        	moduloOfertasPanel.add(getLabelOfertaDescricao());
        	moduloOfertasPanel.add(getLabelOfertaNome()); 	        	
        	moduloOfertasPanel.add(getTeclaOfertarDepois());
        	moduloOfertasPanel.add(getTeclaNao());
        	moduloOfertasPanel.add(getTeclaVerOutras());
        	moduloOfertasPanel.add(getTeclaSimular());   
        	moduloOfertasPanel.setOpaque(true);
    
        }
        return moduloOfertasPanel;
    } 
    
    
    /**
     * Cria o Painel que será usado como contentPane do Dialog.
     * @return EFLPanel Painel que será usado como contentPane do Dialog.
     */
    private EFLPanel getPanelInicio() {
        if (moduloOfertasPanel == null) {
        	moduloOfertasPanel = new EFLPanel(PanelSkin.PANEL_DIALOG);
        	moduloOfertasPanel.setSize(new Dimension(this.getWidth(), this.getHeight()));
        	moduloOfertasPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
        	moduloOfertasPanel.setLayout(null);
        	moduloOfertasPanel.setLocation(0, 0);
        	moduloOfertasPanel.add(getLabelTitulo());
        	moduloOfertasPanel.add(getTituloLabel1());
        	moduloOfertasPanel.add(getTituloLabel2());
        	moduloOfertasPanel.add(getTituloLabel3());
        	
        	 	        	
        	moduloOfertasPanel.add(getTeclaOfertarDepoisVOferta());
        	
        	moduloOfertasPanel.add(getTeclaVer());
        	   
        	moduloOfertasPanel.setOpaque(true);
    
        }
        return moduloOfertasPanel;
    } 
    
    /**global
     * Cria o Label que contém o titulo da Dialog. (Normalmente o Nome do caso de Uso)
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelTitulo() {
        if (labelTitulo == null) {
            labelTitulo = new EFLLabel(LabelSkin.LABEL_DIALOG, TITULO_MODULO_OFERTAS); //$NON-NLS-1$
            labelTitulo.setHorizontalAlignment(EFLLabel.CENTER);
            labelTitulo.setLocation(0, 0);
            labelTitulo.setSize(600, 40);
        }
        return labelTitulo;
    }

    /**
     * Cria o Label que contém a pergunta a ser respondida.
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelMensagem() {
        if (labelMensagem == null) {
            labelMensagem = new EFLLabel(LabelSkin.LABEL_CONFIRMACAO, "Este cliente possui oferta de:");
            labelMensagem.setHorizontalAlignment(EFLLabel.CENTER);
            labelMensagem.setLocation(0, 50);
            labelMensagem.setSize(600, 40);
        }
        return labelMensagem;
    }
  
  
    /**
     * Método responsável por criar o label com a mensagem.
     * 
     * @return EFLLabel
     */
    private EFLLabel getTituloLabel1() {
        if (tituloLabel1 == null) {
        	tituloLabel1 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, TITLE_1);
        	tituloLabel1.setLocation(0, 90);
        	tituloLabel1.setSize(600, 40);
        	tituloLabel1.setHorizontalAlignment(EFLLabel.CENTER);
        }
        return tituloLabel1;
    }
    
    public EFLLabel getTituloLabel2() {
		if (tituloLabel2 == null) {
			tituloLabel2 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, TITLE_2);
			tituloLabel2.setLocation(0, 110);
			tituloLabel2.setSize(600, 40);
			tituloLabel2.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel2;
	}
    
    public EFLLabel getTituloLabel3() {
		if (tituloLabel3 == null) {
			tituloLabel3 = new EFLLabel(LabelSkin.LABEL_TITULO_1_ATD_CAIXA_TEM, TITLE_3);
			tituloLabel3.setLocation(0, 130);
			tituloLabel3.setSize(600, 40);
			tituloLabel3.setHorizontalAlignment(EFLLabel.CENTER);
		}
		return tituloLabel3;
	}
        
    /**
     * Cria o Label que contém a pergunta a ser respondida.
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelOfertaNome() {
        if (labelOfertaNome == null) {
        	labelOfertaNome = new EFLLabel(LabelSkin.LABEL_CONFIRMACAO, ofertaSelecionada.getNomeOferta().toUpperCase().trim());
        	labelOfertaNome.setHorizontalAlignment(EFLLabel.CENTER);
        	labelOfertaNome.setLocation(0, 90);
        	labelOfertaNome.setSize(600, 40);
        }
        return labelOfertaNome;
    }
    
    /**
     * Cria o Label que contém a pergunta a ser respondida.
     * @return EFLLabel Label que contém a pergunta a ser respondida.
     */
    private EFLLabel getLabelOfertaDescricao() {
        if (labelOfertaDescricao == null) {
        	labelOfertaDescricao = new EFLLabel(LabelSkin.LABEL_CONFIRMACAO, ofertaSelecionada.getDescricao());
        	labelOfertaDescricao.setHorizontalAlignment(EFLLabel.CENTER);
        	labelOfertaDescricao.setLocation(0, 140);
        	labelOfertaDescricao.setSize(600, 40);
        }
        return labelOfertaDescricao;

    }  

    /**
     * Método responsável por criar o Botao de Afirmação que leva a listagem de Notificações
     * @return EFLButton Botao que leva a listagem de Notificações
     */
    private EFLButton getTeclaOfertarDepois() {
        if (teclaOfertarDepois == null) {
        	teclaOfertarDepois = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA, Messages.getString("MODULO_OFERTA_LABEL_OFERTAR_DEPOIS")); //$NON-NLS-1$
        	teclaOfertarDepois.setLocation(10, 200);
        	teclaOfertarDepois.setSize(140, 80);
        	teclaOfertarDepois.setDefaultButton();
        	teclaOfertarDepois.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	teclaOfertarDepois.setEnabled(false);
                	customGerenteTimeout.stop();
                	dispose();
                	ui.tratarOfertaDepois(ofertaSelecionada);
                }
            }
           );
        }
        return teclaOfertarDepois;
    }

    /**
     * Método responsável por criar o Botao de Afirmação que leva a listagem de Notificações
     * @return EFLButton Botao que leva a listagem de Notificações
     */
    private EFLButton getTeclaOfertarDepoisVOferta() {
        if (teclaOfertarDepois == null) {
        	teclaOfertarDepois = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA, Messages.getString("MODULO_OFERTA_LABEL_OFERTAR_DEPOIS")); //$NON-NLS-1$
        	teclaOfertarDepois.setLocation(150, 200);
        	teclaOfertarDepois.setSize(140, 80);
        	teclaOfertarDepois.setDefaultButton();
        	teclaOfertarDepois.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	teclaOfertarDepois.setEnabled(false);
                	customGerenteTimeout.stop();
                	dispose();
                	ui.tratarOfertaDepois(ofertaSelecionada);
                }
            }
           );
        }
        return teclaOfertarDepois;
    }
    
    /**
     * Método responsável por criar o Botao de Negação que rejeita o produto
     * @return EFLLabel Botao que cancela o caso de uso.
     */
    private EFLButton getTeclaNao() {
        if (teclaNao == null) {
            teclaNao = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA, Messages.getString("MODULO_OFERTA_LABEL_NAO_QUER_PRODUTO")); //$NON-NLS-1$
            teclaNao.setLocation(130, 200);
            teclaNao.setSize(180, 80);
            teclaNao.addActionListener(new ActionListener() {
                                           public void actionPerformed(ActionEvent e) {
                                        	   teclaNao.setEnabled(false);
                                        	   customGerenteTimeout.stop();
                                        	   ui.tratarRejeitouOferta(ofertaSelecionada);
                                               dispose();
                                           }
                                       }
                                      );
        }
        return teclaNao;
    }
    
    
    /**
     * Método responsável por criar o Botao de Afirmação que leva a listagem de Notificações
     * @return EFLButton Botao que leva a listagem de Notificações
     */
    private EFLButton getTeclaVerOutras() {
        if (teclaVerOutras == null) {
        	teclaVerOutras = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA, Messages.getString("MODULO_OFERTA_LABEL_VER_OUTRAS_OFERTAS")); //$NON-NLS-1$
        	teclaVerOutras.setLocation(300, 200);
        	teclaVerOutras.setSize(140, 80);
        	teclaVerOutras.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	getPanel().setVisible(false);
                	getPanelListaOFertas().setVisible(true);
                }
            }
           );
        }
        return teclaVerOutras;
    }
    
    /**
     * Método responsável por criar o Botao de Afirmação que leva a listagem de Notificações
     * @return EFLButton Botao que leva a listagem de Notificações
     */
    private EFLButton getTeclaVer() {
        if (teclaVer == null) {
        	teclaVer = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA, Messages.getString("MODULO_OFERTA_LABEL_VER_OFERTAS")); //$NON-NLS-1$
        	teclaVer.setLocation(300, 200);
        	teclaVer.setSize(140, 80);
        	teclaVer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    customGerenteTimeout.stop();
                	ui.mostrarOfertaAutorizacaoMargem();
                    dispose();
                }
            }
           );
        }
        return teclaVer;
    }
    
    /**
     * Método responsável por criar o Botao de Afirmação que leva a listagem de Notificações
     * @return EFLButton Botao que leva a listagem de Notificações
     */
    private EFLButton getTeclaSimular() {
        if (teclaSimular == null) {
        	teclaSimular = new EFLButton(defaultUIManager, ButtonSkin.BOTAO_MODULO_OFERTA, Messages.getString("MODULO_OFERTA_LABEL_SIMULAR")); //$NON-NLS-1$
        	teclaSimular.setLocation(430, 200);
        	teclaSimular.setSize(140, 80);        	
        	teclaSimular.addActionListener(new SwingWorkerActionListener() {
				
				@Override
				public void actionPerformedInBackground(ActionEvent actionEvent) {
					teclaSimular.setEnabled(false);
					customGerenteTimeout.stop();
					dispose();
                	//setVisible(false);
                    //repaint();
                	ui.simularOferta(ofertaSelecionada);
				}
			});
        }
        return teclaSimular;
    }

	@Override
	public boolean notifyTimeout() {
		if (!ui.isModuloCamapanhaFinalizada()) {
			this.customGerenteTimeout.stop();
			log.debug("DLG MODULO OFERTA FINALIZADO POR TIME OUT");
			dispose();
        	ui.proseguirTransacao();	
        	ui.apresentaMsg(MensagemErro.getString("TRNI0004"));
        	return true;
        }
		return false;
	}
	
	//jpanel modulo oferta
	  
    private EFLPanel getPanelListaOFertas() {
        if (moduloListaOfertasPanel == null) {
        	moduloListaOfertasPanel = new EFLPanel(PanelSkin.PANEL_DIALOG);
        	moduloListaOfertasPanel.setSize(new Dimension(600, 300));
        	moduloListaOfertasPanel.setLayout(null);
        	moduloListaOfertasPanel.setLocation(0, 0);  
        	//add itens
        	moduloListaOfertasPanel.add(getLabelTituloPrincipalListOferta());
        	moduloListaOfertasPanel.add(getOfertasLista());
        	addOpcoesOfertasLista();
        	moduloListaOfertasPanel.setOpaque(true);
    
        }
        return moduloListaOfertasPanel;
    } 
	
	private EFLLabel getLabelTituloPrincipalListOferta() {
		if (labelTituloPrincipalModuloList == null) {
			labelTituloPrincipalModuloList = new EFLLabel(LabelSkin.LABEL_DIALOG,
					Messages.getString("MODULO_OFERTA_LABEL_LISTA_PRODUTO"), EFLLabel.CENTER);
			labelTituloPrincipalModuloList.setLocation(0, 0);
			labelTituloPrincipalModuloList.setSize(600, 40);

		}
		return labelTituloPrincipalModuloList;
	}
	
	private EFLReportEvent getOfertasLista() {
		if (ofertasLista == null) {
			ofertasLista = new EFLReportEvent(600);
			ofertasLista.setAutoscrolls(true);
			ofertasLista.setLocation(0, 0);
			ofertasLista.setSize(new Dimension(630, 360));

			ofertasLista.addSelectListener(new OfertasListaActionListener());

			EFLPanel cabecalho = ofertasLista.addPanelCabecalho();
			cabecalho.setSize(0, 0);
			addCabecalhoOfertasLista(cabecalho);
		}
		return ofertasLista;
	}

	private class OfertasListaActionListener implements EFLReportEventListener {
		@Override
		public void onSelected(int index) {
			ofertaSelecionada = ui.getListaOfertaCliente().getOfertas().get(index);
			getPanelListaOFertas().setVisible(false);
			getLabelOfertaNome().setText(ofertaSelecionada.getNomeOferta().toUpperCase().trim());
			getLabelOfertaDescricao().setText(ofertaSelecionada.getDescricao());
			getPanel().setVisible(true);
		}
	}

	private void addCabecalhoOfertasLista(EFLPanel cabecalho) {
		EFLLabel labelOfertas = new EFLLabel(LabelSkin.LABEL_REPORT, "");
		labelOfertas.setSize(600, 40);
		labelOfertas.setLocation(0, 0);
		cabecalho.add(labelOfertas);
	}
	
	private void addOpcoesOfertasLista() {
		getOfertasLista().removeAll();

		for (OfertaCliente oferta : listaOfertaCliente.getOfertas()) {
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
}
