package br.gov.caixa.sispl.infra.ui;


import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import br.gov.caixa.sispl.dominio.CodigoErro;
import br.gov.caixa.sispl.dominio.ConstantesParametros;
import br.gov.caixa.sispl.dominio.RegraNegocioException;
import br.gov.caixa.sispl.dominio.TipoMovimento;
import br.gov.caixa.sispl.dominio.Usuario;
import br.gov.caixa.sispl.infra.controle.ControleMenu;
import br.gov.caixa.sispl.infra.controle.EventoEFL;
import br.gov.caixa.sispl.infra.controle.EventosEFLListener;
import br.gov.caixa.sispl.infra.controle.FabricaControleMovimento;
import br.gov.caixa.sispl.infra.controle.InfoMovimento;
import br.gov.caixa.sispl.infra.controle.menu.ItemMenuXml;
import br.gov.caixa.sispl.infra.executor.ExecutorException;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.cesta.CestaEstadoProcessado;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.cesta.CestaFactory;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.controle.ControleMenuCesta;
import br.gov.caixa.sispl.infra.meiosdepagamento.uc.meiosdepagamento.ui.UIMeiosDePagamentoControle;
import br.gov.caixa.sispl.infra.perifericos.GerenciaPerifericos;
import br.gov.caixa.sispl.infra.propriedades.SuportePropriedades;
import br.gov.caixa.sispl.infra.timeout.GerenteTimeoutUI;
import br.gov.caixa.sispl.infra.ui.dlg.DlgNotificacao;
import br.gov.caixa.sispl.infra.ui.pnl.PnlMenu;
import br.gov.caixa.sispl.infra.util.AbstractCode;
import br.gov.caixa.sispl.infra.util.Barcode;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;
import br.gov.caixa.sispl.util.MensagemErro;
import br.gov.caixa.sispl.util.Messages;

/**
 * Controla o fluxo do funcionamento do menu, utilizando o controle de negócio para realizar
 * validações e os paineis para ativar visualização do usuário.
 * @author Tiago de A. van den Berg
 */
public class UIMenuControle extends UIAbstractControle implements EventosEFLListener {

    /**
     * Utilitário de log. usado para fins de debug
     */
    private Log log = LogFactory.getLog(UIMenuControle.class);
    
    /**
     * Utilitário para validação de QrCode de Arrecadação de Convênio.
     */
	private static final int TAMANHO_BARCODE = 44;
	private static final String GUI_ARRECADACAO = "br.gov.caixa.arrec01";
	private static final String GUI_PEC = "br.gov.caixa.pec01";	
	private static final String INICIO_COD_BARRAS = "8";
	private static final String PREFIXO_QRCODE_JOGOS = "APOSTA";
	private static final String PREFIXO_QRCODE_APP = "CAIXA";
	private String COD_BARRAS_ARREC = "";
	private int tipoMovimento;
	private String nomeMovimento;


    /**
     * Tempo utilizado para realizar pooling nos mecanismos de notificação. 
     * Nesse tempo os mecanismos serão percorridos para verificar se existe alguma notificação 
     * pendente de visualização pelo usuário.
     * @see #verificaNotificacoes() 
     */
    private static final long TEMPO_POOLING_NOTIFICACAO = 60000;
    
    
    /**
     * Referência a controladora de negócio do menu. Usado para todo tipo de validação e atribuição ao domínio do menu.
     * @see ControleMenuCesta
     */
    private ControleMenu controleMenu;

    /**
     * Armazena o mecanismo de notificação que está sendo percorrido no instante. Serve para, após o usuário confirmar
     * uma visualização de notificação. O controle saber qual foi o mecanismo confirmado através do índice do mecanismo.
     * @see #verificaNotificacoes()
     * @see #notificacaoConfirmada
     */
    private int indexNotificacaoMostradaNoInstante;

    /**
     * Flag utilizada para encerrar o percorrer dos mecanismos de notificação quando o usuário confirmar um mecanismo. 
     * @see #verificaNotificacoes()
     * @see #indexNotificacaoMostradaNoInstante
     */
    private boolean notificacaoConfirmada;

    /**
     * Referencia do último menu exibido. Usado apenas para obter o foco.
     * @see #requisitaFocoMenu()
     */
    private PnlMenu pnlMenu;
    
    /**
     * Timer usado conjuntamente com <code>verificaNotificacoesTimerTask</code> para realizar o pooling de mecanismos de notificação.
     * @see #verificaNotificacoes()
     * @see #indexNotificacaoMostradaNoInstante
     * @see #notificacaoConfirmada
     */
    private Timer timerVerificaNotificacoes;
    
    

    /**
     * TimerTask utilizado conjuntamente com <code>timerVerificaNotificacoes</code> para realizar o 
     * pooling de mecanismos de notificação.
     */
    private TimerTask verificaNotificacoesTimerTask;
    
    private Timer timeBloqueioTelaEFL;
    
    private TimerTask bloqueiaTelaEFLTimeTask;

	private boolean isBloqueioInatividade;
	
	private boolean isNotificacaoPendente;
	
	    
    /**
     * Construtor que realiza a inicialização da controladora de negócio e do timer.
     * @throws ExecutorException
     */
    public UIMenuControle() throws ExecutorException {
    	FabricaControleMovimento.getInstance().preparaControleMovimento(this);
        controleMenu = new ControleMenu();
        preparaAntigoControleMovimento(controleMenu);
        timerVerificaNotificacoes = new Timer(true);
        
        timeBloqueioTelaEFL = new Timer(true);
    }
    public UIMenuControle(ControleMenu controleMenu)  {
        this.controleMenu = controleMenu;
        timerVerificaNotificacoes = new Timer(true);
        
        timeBloqueioTelaEFL = new Timer(true);
    }

    /**
     * Mostra a tela de menu e atribui os listeners de periféricos.
     */
    public void mostraTelaMenu() {    	
        //FIXME Criar um mecanismo de ui e levar esse método para ele.
        
		if (!isBloqueioInatividade) {

			log.debug(" <--- EFL SEM BLOQIEIO POR INATIDADE ---> ");
			pnlMenu = new PnlMenu(this);

			GerenciaPerifericos.getInstance().setScannerListener(pnlMenu);
			setPanelVisualizado(pnlMenu);
		}
        
		bloqueiaTelaEFL();
    }

    /**
     * Recupera o foco do menu para que as teclas de atalho funcionem.
     *
     */
    private void requisitaFocoMenu(){
    	pnlMenu.requestFocus();
    }
    
    /**
     * Mostra a mensagem institucional no pinpad.
     */
    public void mostraMensagemPinpad() {
        //FIXME Criar um mecanismo de ui e levar esse método para ele.
        GerenciaPerifericos.getInstance().displayPinPad(3, 0, SuportePropriedades.getInstance().getMensagemCaixa());
    }

    /**
     * Mostra um dialog de notificação quando é detectada alguma em um mecanismo de notificação. 
     * @see #verificaNotificacoes()
     */
    private void mostraDialogNotificacao() {
        //FIXME Criar um mecanismo de ui e levar esse método para ele.
        try {
            GerenciaPerifericos.getInstance().desativarPerifericos();
            new DlgNotificacao(EFLFrame.getInstance(), this);
        } catch (Exception e) {
            //TODO Realizar log do erro.
        }
    }
    
    /**
     * Inicializa o fluxo de caso de uso.
     * @see UIAbstractControle#init()
     */
    public void init() {
    	GerenteTimeoutUI.getInstance().stopUC();
        preparaAntigoControleMovimento(controleMenu);
        exibeMenuRaiz();
        mostraMensagemPinpad();
        verificaNotificacoes();
    }
    
    public void initMenuManutencaoUsuario() {
    	GerenteTimeoutUI.getInstance().stopUC();
        preparaAntigoControleMovimento(controleMenu);
        exibirMenuManutencaoUsuario();
        mostraMensagemPinpad();
        verificaNotificacoes();
    }
    
	/**
     * Percorre os mecanismos de notificação, e se houver alguma notificação pendente de visualização, dispara a tela 
     * de aviso ao usuário. 
     * @see #init()
     * @see #verificaNotificacoesTimerTask
     */
    private void verificaNotificacoes() {
        //Verifica se há novas notificações em todos os mecanismos.
        int quantidadeMecanismosNotificacoes = controleMenu.getQuantidadeMecanismosNotificacoes();

//        if (EFLFrame.getInstance().getPnlFilaImpressao().isHabilitado()) {
//            return;
//        }
        //Se uma notificação for aceita, sai do loop (ver condição do for) pois será iniciado algum caso de uso e encerra os timers.
        notificacaoConfirmada = false;
        if(!existePendenciaTratamentoEspecifico()){
            for (int indexMecanismoNotificacao = 0; (indexMecanismoNotificacao < quantidadeMecanismosNotificacoes) && !notificacaoConfirmada; indexMecanismoNotificacao++) {
                if (controleMenu.hasNotificacao(indexMecanismoNotificacao) && (controleMenu.getMenuAtual().equals(controleMenu.getMenuRaiz()))) {
                    //Se há notificação...
                    indexNotificacaoMostradaNoInstante = indexMecanismoNotificacao;
                    if (isLeituraNotificacaoMostradaNoInstanteMandatoria()) {
                        confirmaNotificacao(getTipoMovimentoNotificacaoMostradaNoInstante(), getOpcaoMovimentoNotificacaoMostradaNoInstante());
                    } else {
                        //...dispara a tela de aviso ao usuário.
                        //Esse método só retorna depois do usuário responder ao dialog.
                        mostraDialogNotificacao();
                    }
                }
            }
        } else {
            indexNotificacaoMostradaNoInstante = controleMenu.getIndexPendenciaEspecifica();
            confirmaNotificacao(getTipoMovimentoNotificacaoMostradaNoInstante(), getOpcaoMovimentoNotificacaoMostradaNoInstante());
        }

        //Cancela o timer anterior (definitivamente se notificacaoConfirmada for true).
        if (verificaNotificacoesTimerTask != null) {
            verificaNotificacoesTimerTask.cancel();
            verificaNotificacoesTimerTask = null;
        }
        //Se após percorrer todos os mecanismos de notificação, o usuário não confirmar nenhuma visualização, continuar o pooling.
        if (!notificacaoConfirmada) {
            verificaNotificacoesTimerTask = new VerificaNotificacoesTimerTask();
            timerVerificaNotificacoes.schedule(verificaNotificacoesTimerTask, TEMPO_POOLING_NOTIFICACAO);

            //Recupera o foco do menu para que as teclas de atalho funcionem.
            requisitaFocoMenu();
        }
    }

    /**
     * Realiza o fluxo quando um usuário seleciona um submenu do menu atual.
     * @param item Item de menu selecionado pelo usuário.
     */
    public void selecionaMenu(ItemMenuXml item) {
        apagaMsg();
        controleMenu.setMenuAtual(item);
        mostraTelaMenu();	
    }
    
    public void processaCasoUsoLogin(int tipoMovimento, int opcaoMovimento) {
    	 apagaMsg();
         
         try {
        	 
        	controleMenu.setTipoEntrada(ControleMenu.TIPOENTRADA_MENU);
             controleMenu.setTipoMovimento(tipoMovimento);
             controleMenu.setOpcaoMovimento(opcaoMovimento);
             GerenciaPerifericos.getInstance().desativarPerifericos();
			controleMenu.conclui();
			
			isBloqueioInatividade = true;
			
	         
		} catch (RegraNegocioException e) {
			tratarErroRegraNegocioMenu(e);
		} catch (ExecutorException e) {
			log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
            mostraTelaMenu();
            apresentaMsg(e.getMessage());
            init();
		}    
    }

    /**
     * Realiza o fluxo quando um usuário seleciona um caso de uso no menu, inicializando o caso de uso.
     * @param tipoMovimento Movimento a ser inicializado. 
     * @param opcaoMovimento Opção de movimento a ser passado na inicialização do caso de uso.
     * @see br.gov.caixa.sispl.infra.controle.ControleMovimento#init(int, int) 
     */
    public void selecionaCasoUso(int tipoMovimento, int opcaoMovimento, String nomeMovimento ) {
        apagaMsg();
        try {
            controleMenu.setTipoEntrada(ControleMenu.TIPOENTRADA_MENU);
            controleMenu.setTipoMovimento(tipoMovimento);
            controleMenu.setOpcaoMovimento(opcaoMovimento);
            
            GerenciaPerifericos.getInstance().desativarPerifericos();
            
            
            if (nomeMovimento == null){
            	controleMenu.conclui();
            }else{
            	this.nomeMovimento = nomeMovimento;
				this.tipoMovimento = tipoMovimento;
            	controleMenu.conclui(controleMenu.getInfoMovimento(nomeMovimento));
            }
            
            //Se tudo der certo, finaliza o pooling...
            if (verificaNotificacoesTimerTask != null) {
                verificaNotificacoesTimerTask.cancel();
                verificaNotificacoesTimerTask = null;
            }
            
          //Se tudo der certo, finaliza o pooling para bloquear tela da efl...
            stopTimerEFL();
            
        } catch (RegraNegocioException e) {
			if (e.getCodigoErro().equals(CodigoErro.ERRO_TF_MP_CESTA_PROCESSADA)) {
				mostrarTelaCriticaAtendimentoPrecessado();
			} else if (e.getCodigoErro().getCodigo().equals(("PRRA016"))) {
				final InfoMovimento info = controleMenu.getInfoMovimento(nomeMovimento);
				mensagemConfirmarResposta(this, controleMenu, info, e.getMessage(), " ", tipoMovimento);
			} else {
				tratarErroRegraNegocioMenu(e);
			}
        } catch (ExecutorException e) {
            log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
            mostraTelaMenu();
            apresentaMsg(e.getMessage());
            init();
        }
    }
    
    public void menu() {
    	mostraTelaMenu();
		init();
    }
    
    

    public void selecionaCasoUso(int tipoMovimento, int opcaoMovimento){
    	selecionaCasoUso(tipoMovimento, opcaoMovimento, null);
    }
    
    
    public void selecionaFluxoNPC(int tipoMovimento, BigDecimal valor, AbstractCode barcode) {    
          apagaMsg();
        try {
        	        	
            controleMenu.setCodigoBarrasNPC(barcode.getBarcodeText(), valor);
            controleMenu.setTipoEntrada(ControleMenu.TIPOENTRADA_CODIGOBARRAS);
            controleMenu.setTipoMovimento(tipoMovimento);
            controleMenu.setOpcaoMovimento(0);
            
            GerenciaPerifericos.getInstance().desativarPerifericos();

           	controleMenu.conclui();
            
            //Se tudo der certo, finaliza o pooling...
            if (verificaNotificacoesTimerTask != null) {
                verificaNotificacoesTimerTask.cancel();
                verificaNotificacoesTimerTask = null;
            }
        } catch (RegraNegocioException e) {
			if (e.getCodigoErro().equals(CodigoErro.ERRO_TF_MP_CESTA_PROCESSADA)) {
				mostrarTelaCriticaAtendimentoPrecessado();
			} else if (e.getCodigoErro().getCodigo().equals(("PRRA016"))) {
				final InfoMovimento info = controleMenu.getInfoMovimento(nomeMovimento);
				mensagemConfirmarResposta(this, controleMenu, info, e.getMessage(), " ", tipoMovimento);
			} else {
				tratarErroRegraNegocioMenu(e);
			}
        } catch (ExecutorException e) {
            log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
            mostraTelaMenu();
            apresentaMsg(e.getMessage());
            init();
        }
    }

    /**
     * Realiza o fluxo quando um usuário passa um volante na leitora.
     * @param data Dados do volante que serão passados ao caso de uso. 
     * @see br.gov.caixa.sispl.infra.controle.ControleMovimento#init(int, boolean[][]) 
     */
    public void passaVolante(boolean[][] data) {
        apagaMsg();
        try {
        	if (CestaFactory.getInstanceEstadoListener().getCestaEstado() instanceof CestaEstadoProcessado) {
        		throw new RegraNegocioException(CodigoErro.ERRO_TF_MP_CESTA_PROCESSADA);
    		}
        	controleMenu.negaTrocaTipoConcurso();
            controleMenu.setTipoEntrada(ControleMenu.TIPOENTRADA_VOLANTE);
            controleMenu.setVolante(data);
            GerenciaPerifericos.getInstance().desativarPerifericos();
            controleMenu.conclui(controleMenu.getInfoMovimento(data[0]));
            
            //Se tudo der certo, finaliza o pooling...
            if (verificaNotificacoesTimerTask != null) {
                verificaNotificacoesTimerTask.cancel();
                verificaNotificacoesTimerTask = null;
            }
            
            //Se tudo der certo, finaliza o pooling para bloquear tela da efl...
            stopTimerEFL();
            
		} catch (RegraNegocioException e) {
			if (e.getCodigoErro().equals(CodigoErro.ERRO_DM_JG_TIMEMANIA_VOLANTE_INVALIDO)) {
				mostrarPaineltMensagemConfirmada(this,
						"VOLANTE INVÁLIDO. SOLICITAMOS PREENCHER O VOLANTE ATUAL DA MODALIDADE", "VOLANTE INVÁLIDO",
						new SwingWorkerActionListener() {
							@Override
							public void actionPerformedInBackground(ActionEvent actionEvent) {
								mostraTelaMenu();
								init();

							}
						});

			} else if (e.getCodigoErro().equals(CodigoErro.ERRO_TF_MP_CESTA_PROCESSADA)) {
				mostrarTelaCriticaAtendimentoPrecessado();
			} else if (e.getCodigoErro().getCodigo().equals(("PRRA016"))) {
        		InfoMovimento info;
				try {
					info = controleMenu.getInfoMovimento(data[0]);
					mensagemConfirmarResposta(this,controleMenu,info, e.getMessage(), " ", info.getCodigo());
				} catch (RegraNegocioException e1) {
					
				}

			} else {
				tratarErroRegraNegocioMenu(e);
			}
		} catch (ExecutorException e) {
			log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
			mostraTelaMenu();
			apresentaMsg(e.getMessage());
			init();
		}
    }

    /**
     * Realiza o fluxo quando um usuário passa um código de barras na leitora
     * @param data Código de barras que será passado ao caso de uso. 
     * @see br.gov.caixa.sispl.infra.controle.ControleMovimento#init(int, String)
     */
    public void passaCodigoBarras(String barcode) {
        apagaMsg();
        try {
        	barcode = verificaBarCodeQrCode(barcode);        	
            controleMenu.setTipoEntrada(ControleMenu.TIPOENTRADA_CODIGOBARRAS);
            if (!barcode.equals("PEC")) {
            controleMenu.setCodigoBarras(barcode);
        	}
            
            if (controleMenu.getTipoMovimento() != 0) {
	            GerenciaPerifericos.getInstance().desativarPerifericos();
	            controleMenu.conclui();
	            //Se tudo der certo, finaliza o pooling...
	            if (verificaNotificacoesTimerTask != null) {
	                verificaNotificacoesTimerTask.cancel();
	                verificaNotificacoesTimerTask = null;
	            }
	            
	          //Se tudo der certo, finaliza o pooling para bloquear tela da efl...
	            stopTimerEFL();
	            
            }   
        } catch (RegraNegocioException e) {
			if (e.getCodigoErro().equals(CodigoErro.ERRO_TF_MP_CESTA_PROCESSADA)) {
				mostrarTelaCriticaAtendimentoPrecessado();
			} else {
				tratarErroRegraNegocioMenu(e);
			}
        } catch (ExecutorException e) {
            log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
            mostraTelaMenu();
            apresentaMsg(e.getMessage());
            init();
            
        } catch (Exception e) {
        	log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
        	mostraTelaMenu();
            apresentaMsg(MensagemErro.getString("GRLE0015"));
            init();
        }
    }
    
    
	private void tratarErroRegraNegocioMenu(RegraNegocioException e) {
		mostrarPaineltMensagemConfirmada(this, e.getMessage(), UIMeiosDePagamentoControle.TITULO, new SwingWorkerActionListener() {
			
			@Override
			public void actionPerformedInBackground(ActionEvent actionEvent) {
				mostraTelaMenu();
//						apresentaMsg(e.getMessage());
				init();
				
			}
		});
	}
    
    /**
     * Verifica se a origem é QrCode ou Barcode.
     * @param data Dados do volante que serão passados ao caso de uso. 
     * @see br.gov.caixa.sispl.infra.controle.ControleMovimento#init(int, String) 
     */
	public String verificaBarCodeQrCode(String code) throws RegraNegocioException {
		if (code.length() > TAMANHO_BARCODE && !verificaNumerico(code)) {	
				log.info("========= QRcode " + code);							
				code = validaLayoutQrcode(code); 
		}
		return code;	
	}
	
	private boolean verificaNumerico(String code){		
		return code.replaceAll("[0-9]", "").isEmpty() ? true : false;
	}
	
    /**
     * Valida layout QrCode Arrecadação convênio.
     * @param data Dados do volante que serão passados ao caso de uso. 
     * @throws RegraNegocioException 
     * @see br.gov.caixa.sispl.infra.controle.ControleMovimento#init(int, String) 
     */
	private String validaLayoutQrcode(String code) throws RegraNegocioException {		
		if(ehJogos(code)) {
			return code;			
		}else if(ehQRCodeApp(code)) {
			return code;			
		}else {	
			COD_BARRAS_ARREC = "";	
			if(isArrecadacaoConvenio(code)) {
				code = COD_BARRAS_ARREC;
			} else if (isPEC(code)) {	            							
				code = "PEC";		
		    }  else {				
				throw new RegraNegocioException(CodigoErro.ERRO_DM_FI_QR_CODE_INSUFICIENTE);
			}
		}			
		return code;
	}

    private boolean ehJogos(String code) {
		return code.startsWith(PREFIXO_QRCODE_JOGOS) ? true : false;
	}
    
    private boolean ehQRCodeApp(String code) {
		return code.startsWith(PREFIXO_QRCODE_APP) ? true : false;
	}
	
    /**
     * Valida layout QrCode Arrecadação convênio.
     */
	private boolean isArrecadacaoConvenio(String Qrcode) {		
		int iArre = Qrcode.indexOf(GUI_ARRECADACAO);		
		if (iArre > -1) {			
			int tCodigoBarras =  Integer.parseInt(Qrcode.substring(iArre + GUI_ARRECADACAO.length(),iArre + GUI_ARRECADACAO.length() + 2));			
			if(tCodigoBarras == 44) {
				COD_BARRAS_ARREC = Qrcode.substring(iArre + GUI_ARRECADACAO.length()+ 2 ,iArre + GUI_ARRECADACAO.length() + 2 + 44);					
				return inicioCodBarras(COD_BARRAS_ARREC); 					
			} else	{					
				StringBuffer novoCodBarras = new StringBuffer(Qrcode.substring(iArre + GUI_ARRECADACAO.length()+ 2 ,iArre + GUI_ARRECADACAO.length() + 2 + 48));
				COD_BARRAS_ARREC = novoCodBarras.substring(0,11).concat(novoCodBarras.substring(12,23)).concat(novoCodBarras.substring(24,35)).concat(novoCodBarras.substring(36,47)).toString();
				return inicioCodBarras(COD_BARRAS_ARREC); 
			}				
		}
		return false;	
	}	
	
	private boolean inicioCodBarras(String codBarra)
	{return codBarra.startsWith(INICIO_COD_BARRAS) ? true : false;}
	
    /**
     * Valida layout QrCode PEC.
     */
	private boolean isPEC(String Qrcode) {
		int ixPec = Qrcode.indexOf(GUI_PEC);			
		if (ixPec > -1) {
			String tpPec =  Qrcode.substring(ixPec + GUI_PEC.length()+2,ixPec + GUI_PEC.length()+4);			
			if (tpPec.equals("10") ||tpPec.equals("20") || tpPec.equals("30")  ) {
				int tipoMovimento = tpPec.equals("10") ? TipoMovimento.ARRECADACAO_CONSULTA_CONVENIO_GENERICO_PAGAMENTO_PEC_10 : 
														 TipoMovimento.ARRECADACAO_CONVENIO_GENERICO_OUTROS_PAGAMENTOS;
				controleMenu.setTipoMovimento(tipoMovimento);				
				controleMenu.barcode = new Barcode(Qrcode);
				return true;
			}	else {
				return false;
			}
		}		
		return false;
	}

    /**
     * Realiza o fluxo quando um usuário seleciona voltar no menu.
     */
    public void selecionaVoltar() {
        apagaMsg();
        controleMenu.setMenuAtual(controleMenu.getMenuAnterior());
        mostraTelaMenu();
    }

    /**
     * Realiza o fluxo quando um usuário seleciona cancelar, voltando ao menu principal.
     */
    public void selecionaCancelar() {
        apagaMsg();
        exibeMenuRaiz();
    }

    /**
     * Exibe o menu principalO
     *
     */
	public void exibeMenuRaiz() {
		controleMenu.setMenuAtual(controleMenu.getMenuRaiz());
        mostraTelaMenu();
	}

	public void exibirMenuManutencaoUsuario() {
		controleMenu.setMenuAtual(controleMenu.getItemMenuXml());	
        mostraTelaMenu();
	}
    /**
     * Realiza o fluxo quando o usuário confirma a visualização de uma notificação. Encerrando o percorrer 
     * dos mecanismos de notificação e inicializando o cao de uso referente a notificação.
     * @param tipoMovimento Código de Movimento referente a notificação que o usuário deseja ler.
     * @see #notificacaoConfirmada
     * @see #selecionaCasoUso(int, int)
     */
    public void confirmaNotificacao(int tipoMovimento, int opcaoMovimento) {
    	if (isBloqueioInatividade && !isServidorSemComunicacao(tipoMovimento, opcaoMovimento)) {
    		isNotificacaoPendente = true;
			mostraTelaUsuarioSenhaEFL();
		} else {
			notificacaoConfirmada = true;
			isNotificacaoPendente = false;
			selecionaCasoUso(tipoMovimento, opcaoMovimento);
		}
    }
    
	private boolean isServidorSemComunicacao(int tipoMovimento, int opcaoMovimento) {
		if (TipoMovimento.LOGOFF == tipoMovimento && opcaoMovimento == 1) {
			return true;
		} else
			return false;
	}

    /**
     * Realiza o fluxo quando o usuário cancela a visualização de uma notificação.
     * Normalmente esse caso de uso simplesmente permite a continuidade da verificação dos mecanismos de notificação.
     */
    public void cancelaNotificacao() {  
    	isNotificacaoPendente = false;
		if (isBloqueioInatividade) {
			mostraTelaUsuarioSenhaEFL();
		} else {
			mostraTelaMenu();
		}
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#isItemFolha(ItemMenuXml)
     */
    public boolean isItemFolha(ItemMenuXml itemMenuXml) {
        return controleMenu.isItemFolha(itemMenuXml);
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getMenuAtual()
     */
    public ItemMenuXml getMenuAtual() {
        return controleMenu.getMenuAtual();
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getMenuRaiz()
     */
    public ItemMenuXml getMenuRaiz() {
        return controleMenu.getMenuRaiz();
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#addMecanismoNotificacao(MecanismoNotificacao)
     */
    public void addMecanismoNotificacao(MecanismoNotificacao mecanismoNotificacao) {
        controleMenu.addMecanismoNotificacao(mecanismoNotificacao);
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getTipoMovimentoNotificacao(int)
     */
    public int getTipoMovimentoNotificacaoMostradaNoInstante() {
        return controleMenu.getTipoMovimentoNotificacao(indexNotificacaoMostradaNoInstante);
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getTipoMovimentoNotificacao(int)
     */
    public int getOpcaoMovimentoNotificacaoMostradaNoInstante() {
        return controleMenu.getOpcaoMovimentoNotificacao(indexNotificacaoMostradaNoInstante);
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getTituloNotificacao(int)
     */
    public String getTituloNotificacaoMostradaNoInstante() {
        return controleMenu.getTituloNotificacao(indexNotificacaoMostradaNoInstante);
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getMensagemNotificacao(int)
     */
    public String getMensagemNotificacaoMostradaNoInstante() {
        return controleMenu.getMensagemNotificacao(indexNotificacaoMostradaNoInstante);
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getMensagemNotificacao(int)
     */
    public boolean isLeituraNotificacaoMostradaNoInstanteMandatoria() {
        return controleMenu.isLeituraNotificacaoMandatoria(indexNotificacaoMostradaNoInstante);
    }

    /**
     * Delegate do negócio. Usado para que a estrutura em camadas fique concisa.
     * @see ControleMenuCesta#getMensagemNotificacao(int)
     */
    public List getAtalhos() {
        return controleMenu.getAtalhos();
    }
    /**
     * Usado para inicializar o caso de uso ou retornar para o menu. Usado pelo GerenteEventos
     * @see EventoEFL
     * @see br.gov.caixa.sispl.infra.controle.GerenteEventosEFL
     */
    public void notificacaoEvento(EventoEFL evt) {
    	if (evt.getTipoEvento() == EventoEFL.EVT_EFL_UC_ENCERRADO 
    	    || evt.getTipoEvento() == EventoEFL.EVT_EFL_INICIADA){
    		init();
        }else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_MEGA_SENA){
            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (itemMenuXml.getNome().equals("MEGA-SENA")){
                    selecionaMenu(itemMenuXml);
                }                       
            }               
        }else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_QUINA){
            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (itemMenuXml.getNome().equals("QUINA")){
                    selecionaMenu(itemMenuXml);
                }                       
            }               
        }else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_LOTOFACIL){
            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (Normalizer.normalize(itemMenuXml.getNome(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
						.equals("LOTOFACIL")) {
					selecionaMenu(itemMenuXml);
				}                      
            }               
        }
        
//        else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_LOTOMANIA){
//            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
//            while (iteratorBotoes.hasNext()) {
//                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
//                if (itemMenuXml.getNome().equals("LOTOMANIA")){
//                    selecionaMenu(itemMenuXml);
//                }                       
//            }  
//        }
        
    	
    	
        else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_MEIOS_PAGAMENTO){
        	Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (itemMenuXml.getNome().equals("MEIOS DE PAGAMENTO")){
                    selecionaMenu(itemMenuXml);
                }                       
            }
    	}
        else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_DUPLASENA){
            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (itemMenuXml.getNome().equals("DUPLA-SENA")){
                    selecionaMenu(itemMenuXml);
                }                       
            }
            /*
        }else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_TIMEMANIA){
            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (itemMenuXml.getNome().equals("TIMEMANIA")){
                    selecionaMenu(itemMenuXml);
                }                       
            }               
        }else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_LOTECA){
            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (itemMenuXml.getNome().equals("LOTECA")){
                    selecionaMenu(itemMenuXml);
                }                       
            }               
        }else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_LOTOGOL){
            Iterator iteratorBotoes = getMenuRaiz().getItemMenuXml().iterator();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                if (itemMenuXml.getNome().equals("LOTOGOL")){
                    selecionaMenu(itemMenuXml);
                }                       
            }               
*/
        } else if(evt.getTipoEvento() == EventoEFL.EVT_MENU_EMITE_RELATORIO_CONTAS){
    		controleMenu.setMenuAtual(controleMenu.getMenuAnterior());    		
            mostraTelaMenu();
    	}
    }

    /**
     * Utilizada para a execução do pooling no mecanismo de notificações do menu. 
     * @author Tiago de A. van den Berg
     * @see ControleMenuCesta
     */
    private class VerificaNotificacoesTimerTask extends TimerTask {
        /**
         * Verifica as notificações no pooling dos mecanismo de notificação.
         */
        public void run() {
            verificaNotificacoes();
        }
    }
    
    private void checkNotificacoes() {
		if(isNotificacaoPendente) {
			log.debug("<<--- EFL POSSUI NOTIFICACAO PENDENTE --->>");
			confirmaNotificacao(getTipoMovimentoNotificacaoMostradaNoInstante(),
					getOpcaoMovimentoNotificacaoMostradaNoInstante());
		}else {
			log.debug("<<--- EFL NÃO POSSUI NOTIFICACAO PENDENTE --->>");
		}
	}
    
	public boolean desbloqueiaTelaEFL(String usuario, String senha) {

		try {
			apresentaMsg(MensagemErro.getString("GRLI0017"));
			if (getControleMenu().confirmarSenhaUsuario(usuario, senha) == Usuario.CODIGOSITUACAOLDAP_SENHANORMAL) {
							
				apagaMsg();
				stopTimerEFL();
				mostraTelaMenu();
				return true;
			}
			if (getControleMenu().confirmarSenhaUsuario(usuario, senha) == Usuario.CODIGOSITUACAOLDAP_SENHAEXPIRADA) {	
			   processaCasoUsoLogin(TipoMovimento.LOGOFF, 4);
			}
			

		} catch (ExecutorException e) {
			apresentaMsg(e);
		} catch (RegraNegocioException e) {
			apresentaMsg(e);
		}
		return false;
	}
    
	private int getParametroInatividadeEFL() {
		return SuportePropriedades.getInstance().getParametroInt(ConstantesParametros.BLOQUEIA_DESBLOQUEIA_EFL_INATIVIDADE);
	}
    
	private void bloqueiaTelaEFL() {
		
		if (bloqueiaTelaEFLTimeTask == null) {
			
			log.debug("<<--- INICIA TEMPO PARA BLOQUEIO DA EFL POR INATIVIDADE --->>");

			bloqueiaTelaEFLTimeTask = new BloqueaTelaEFLTimerTask();
			
			long tempoInatividadeEFL = TimeUnit.MINUTES.toMillis(getParametroInatividadeEFL());
			
			timeBloqueioTelaEFL.schedule(bloqueiaTelaEFLTimeTask, tempoInatividadeEFL);
			
			checkNotificacoes();
			
		} else if (isBloqueioInatividade && controleMenu.getMenuAtual().equals(controleMenu.getMenuRaiz())) {
			mostraTelaUsuarioSenhaEFL();
		}

	}
	    
	private void stopTimerEFL() {
		log.debug("<<<--- LIMPA TIME BLOQUEIO EFL--->>>");
		
		isBloqueioInatividade = false;
		
		if (bloqueiaTelaEFLTimeTask != null) {
			bloqueiaTelaEFLTimeTask.cancel();
			bloqueiaTelaEFLTimeTask = null;
		}
	}
	
	public void mostraTelaUsuarioSenhaEFL() {
		GerenciaPerifericos.getInstance().desativarPerifericos();
		apagaMsg();
		setPanelVisualizado(new PnlSenhaUsuarioEFL(this));
	}
    
    private class BloqueaTelaEFLTimerTask extends TimerTask{
		
		@Override
		public void run() {
			
			bloqueiaTelaEFL();
			
			log.debug(" <<---- TEMPO ESGOTADO - MOSTRA TELA USUÁRIO --->>");
			if (controleMenu.getMenuRaiz().equals(controleMenu.getMenuAtual())) {
				isBloqueioInatividade = true;
				mostraTelaUsuarioSenhaEFL();
			}
		}
    	
    }
    
    /**
     * Exibe a mensagem de timeout e retorna ao menu principal, 
     * desde que o menu atual não seja o principal.
     */
    public synchronized boolean notifyTimeout(){
    	if (!controleMenu.getMenuRaiz().equals(controleMenu.getMenuAtual())) {
	    	apresentaMsg(MensagemErro.getString("TRNI0004"));
	    	exibeMenuRaiz();
    	}
        return true;
    }
    
    public boolean existePendenciaTratamentoEspecifico(){
        return controleMenu.existePendenciaTratamentoEspecifico();
    }

    public boolean isParametroHabilitaFGTS( int constParams ) {
    	if (SuportePropriedades.getInstance().getParametroInt(constParams) == 1) {
    		return true;
    	}
    	return false;
    }

	public void mostraMensagemBiometriaComProblemas() {
		apresentaMsg(MensagemErro.getString("OBRE0035"));
	}
	
	public void mostraMensagemBiometriaComProblemasResumido() {
		apresentaMsg(MensagemErro.getString("OBRE0039"));
	}

	public boolean isHabilitaOpcaoDeMenu(int parametro) {
		return "1".equals(SuportePropriedades.getInstance().getParametro(parametro));
	}

	public void mostrarTelaCriticaTransacaoComPagCartao(SwingWorkerActionListener btnOK) {
		
		mostrarPaineltMensagemConfirmada(this, Messages.getString("MEIOS_PAGAMENTO_NAO_AUTORIZADO"),Messages.getString("MEIOS_PAGAMENTO_OPCAO_AZULZINHA"), btnOK);
	}

	/**
	 * RTC17711533
	 * @param item
	 */
    public void preservarItemMenu(ItemMenuXml item) {
    	controleMenu.preservarItemMenu(item);
    }
    
    public ControleMenu getControleMenu() {
		return controleMenu;
	}
    
	public void negaTrocaTipoConcurso() {
		controleMenu.negaTrocaTipoConcurso();
	}

	public void confirmaTrocaTipoConcurso(InfoMovimento info) {
		try {
			controleMenu.confirmaTrocaTipoConcurso(info);
		} catch (RegraNegocioException e) {
			if (e.getCodigoErro().equals(CodigoErro.ERRO_TF_MP_CESTA_PROCESSADA)) {
				mostrarTelaCriticaAtendimentoPrecessado();
			} else if (e.getCodigoErro().getCodigo().equals("PRRA016")) {
				final InfoMovimento inf = controleMenu.getInfoMovimento(nomeMovimento);
				mensagemConfirmarResposta(this, controleMenu, inf, e.getMessage(), " ", tipoMovimento);
			} else {
				log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
				mostraTelaMenu();
				apresentaMsg(e.getMessage());
				init();
			}
		} catch (ExecutorException e) {
			log.error("MENU: ERRO AO INICIAR CASO DE USO", e);
			mostraTelaMenu();
			apresentaMsg(e.getMessage());
			init();
		}
	}

    
	public void mostrarTelaCriticaAtendimentoPrecessado() {
		mostrarPaineltMensagemConfirmada(this, MensagemErro.getString("MP00022"),
				Messages.getString("TITULO_CONTRATA_MEIOS_DE_PAGAMENTOS"), (new SwingWorkerActionListener() {

					private static final long serialVersionUID = 347673316310157366L;

					@Override
					public void actionPerformedInBackground(final ActionEvent actionEvent) {
						liberar();
						exibirTelaAtendimento();
					}

					private void liberar() {
						try {
							if (getControleMenu() != null && getControleMenu().getGerenteTransacao() != null)
								getControleMenu().getGerenteTransacao().libera();
						} catch (final Exception e) {
							log.error("Erro na liberação do movimento ", e);
						}
					}

				}));
	}
	
	public void removerCpfPortadorAtendimento() {
		getControleMenu().removerCpfPortadorAtendimento();
	}

}
