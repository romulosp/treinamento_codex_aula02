package br.gov.caixa.sispl.infra.ui.pnl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import br.gov.caixa.sispl.dominio.ConstantesParametros;
import br.gov.caixa.sispl.dominio.TipoMovimento;
import br.gov.caixa.sispl.infra.controle.AbstractControleMovimento;
import br.gov.caixa.sispl.infra.controle.Atalho;
import br.gov.caixa.sispl.infra.controle.menu.ItemMenuXml;
import br.gov.caixa.sispl.infra.perifericos.GerenciaPerifericos;
import br.gov.caixa.sispl.infra.perifericos.event.BarcodeEvent;
import br.gov.caixa.sispl.infra.perifericos.event.BarcodeListener;
import br.gov.caixa.sispl.infra.perifericos.event.ScannerEvent;
import br.gov.caixa.sispl.infra.perifericos.event.ScannerListener;
import br.gov.caixa.sispl.infra.propriedades.SuportePropriedades;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.EFLButton;
import br.gov.caixa.sispl.infra.ui.EFLLabel;
import br.gov.caixa.sispl.infra.ui.EFLPanel;
import br.gov.caixa.sispl.infra.ui.MenuBotaoDesabilitadoEnum;
import br.gov.caixa.sispl.infra.ui.SwingWorkerActionListener;
import br.gov.caixa.sispl.infra.ui.UIMenuControle;
import br.gov.caixa.sispl.infra.util.ControleAcesso;
import br.gov.caixa.sispl.util.Messages;

/**
 *
 * @author Tiago de A. van den Berg
 */
public class PnlMenu extends EFLPanel implements BarcodeListener, ScannerListener {

	private static final long serialVersionUID = 2007121201L;

	//Componentes de inteface
    private UIMenuControle uiMenuControle = null;
    private EFLPanel pnlTituloPrincipal = null;
    private EFLPanel pnlTituloSecundario = null;
    private EFLLabel labelTituloPrincipal = null;
    private Collection<EFLButton> botoesMenuDinamico = null;
    private HashMap<String, String> botoesJogos = null;
    private HashMap<String, String> botoesJogosAuxiliar = null;
    private HashMap<String, String> botoesFinanceiros = null;
    private HashMap<String, String> faixasJogos = null;
    private EFLButton buttonSair = null;
    private EFLButton buttonCancelar = null;
    private EFLButton buttonVoltar = null;
    private boolean menuActionPermission = false;
    private Timer timerPollEstadoFila;
    private int countFGTSBiometriaAux = 0;

    /**
     * Construtor da classe
     * @param panelSkin
     */
    public PnlMenu(UIMenuControle uiMenuControle) {
        super(PanelSkin.PANEL_EFL);
        this.uiMenuControle = uiMenuControle;
        botoesJogos = new HashMap<String, String>();
        botoesJogos.put("MEGA-SENA", ButtonSkin.BOTAO_MEGASENA);
        botoesJogos.put("MEGA-SENA ESPECIAL", ButtonSkin.BOTAO_MEGASENA_ESPECIAL);
        botoesJogos.put("QUINA", ButtonSkin.BOTAO_QUINA);
        botoesJogos.put("QUINA ESPECIAL", ButtonSkin.BOTAO_QUINA_ESPECIAL);
        botoesJogos.put("LOTOFÁCIL", ButtonSkin.BOTAO_LOTOFACIL);
        botoesJogos.put("LOTOFÁCIL ESPECIAL", ButtonSkin.BOTAO_LOTOFACIL_ESPECIAL);
        //botoesJogos.put("LOTOGOL", ButtonSkin.BOTAO_LOTOGOL);
        //botoesJogos.put("LOTOGOL ESPECIAL", ButtonSkin.BOTAO_LOTOGOL_ESPECIAL);
        botoesJogos.put("LOTOMANIA", ButtonSkin.BOTAO_LOTOMANIA);
        botoesJogos.put("LOTOMANIA ESPECIAL", ButtonSkin.BOTAO_LOTOMANIA_ESPECIAL);
        botoesJogos.put("DUPLA-SENA", ButtonSkin.BOTAO_DUPLASENA);
        botoesJogos.put("DUPLA-SENA ESPECIAL", ButtonSkin.BOTAO_DUPLASENA_ESPECIAL);
        botoesJogos.put("LOTECA", ButtonSkin.BOTAO_LOTECA);
        botoesJogos.put("LOTECA ESPECIAL", ButtonSkin.BOTAO_LOTECA_ESPECIAL);
        botoesJogos.put("FEDERAL", ButtonSkin.BOTAO_FEDERAL);
        botoesJogos.put("TIMEMANIA", ButtonSkin.BOTAO_TIMEMANIA);
        botoesJogos.put("TIMEMANIA ESPECIAL", ButtonSkin.BOTAO_TIMEMANIA_ESPECIAL);
        botoesJogos.put("DIA DE SORTE", ButtonSkin.BOTAO_DIA_DE_SORTE);
        botoesJogos.put("SUPER SETE", ButtonSkin.BOTAO_SUPER_SETE);
        botoesJogos.put("MILIONÁRIA", ButtonSkin.BOTAO_MILIONARIA);
        botoesJogos.put("BOLÃO MEGA-SENA", ButtonSkin.BOTAO_MEGASENA);
        botoesJogos.put("BOLÃO MEGA-SENA ESPECIAL", ButtonSkin.BOTAO_MEGASENA_ESPECIAL);
        botoesJogos.put("BOLÃO QUINA", ButtonSkin.BOTAO_QUINA);
        botoesJogos.put("BOLÃO QUINA ESPECIAL", ButtonSkin.BOTAO_QUINA_ESPECIAL);
        botoesJogos.put("BOLÃO LOTOFÁCIL", ButtonSkin.BOTAO_LOTOFACIL);
        botoesJogos.put("BOLÃO LOTOFÁCIL ESPECIAL", ButtonSkin.BOTAO_LOTOFACIL_ESPECIAL);
        botoesJogos.put("BOLÃO DUPLA-SENA", ButtonSkin.BOTAO_DUPLASENA);
        botoesJogos.put("BOLÃO DUPLA-SENA ESPECIAL", ButtonSkin.BOTAO_DUPLASENA_ESPECIAL);
        botoesJogos.put("BOLÃO LOTECA", ButtonSkin.BOTAO_LOTECA);
        botoesJogos.put("BOLÃO DIA DE SORTE", ButtonSkin.BOTAO_DIA_DE_SORTE);
        botoesJogos.put("BOLÃO TIMEMANIA", ButtonSkin.BOTAO_TIMEMANIA);
        botoesJogos.put("BOLÃO SUPER SETE", ButtonSkin.BOTAO_SUPER_SETE);
        botoesJogos.put("BOLÃO MILIONÁRIA", ButtonSkin.BOTAO_MILIONARIA);
        botoesJogosAuxiliar = new HashMap<String, String>();
        botoesJogosAuxiliar.put("BOLÃO", ButtonSkin.BOTAO_MENU);
        botoesJogosAuxiliar.put("ÚLTIMOS RESULTADOS", ButtonSkin.BOTAO_MENU);
        botoesJogosAuxiliar.put("PAGA PREMIADOS", ButtonSkin.BOTAO_MENU);
        faixasJogos = new HashMap<String, String>();
        faixasJogos.put("MEGA-SENA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_MEGASENA);
        faixasJogos.put("QUINA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_QUINA);
        faixasJogos.put("LOTOFÁCIL", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_LOTOFACIL);
        //faixasJogos.put("LOTOGOL", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_LOTOGOL);
        faixasJogos.put("LOTOMANIA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_LOTOMANIA);
        faixasJogos.put("DUPLA-SENA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_DUPLASENA);
        faixasJogos.put("BOLÃO DUPLA-SENA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_DUPLASENA);
        faixasJogos.put("LOTECA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_LOTECA);
        faixasJogos.put("FEDERAL", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_FEDERAL);
        faixasJogos.put("TIMEMANIA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_TIMEMANIA);
        faixasJogos.put("DIA DE SORTE", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_DIA_DE_SORTE);
        faixasJogos.put("SUPER SETE", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_SUPER_SETE);
        faixasJogos.put("MILIONÁRIA", LabelSkin.LABEL_FAIXA_TITULO_SECUNDARIO_MILIONARIA);

		botoesFinanceiros = new HashMap<String, String>();
        botoesFinanceiros.put("FATURA AVULSA CARTÃO", ButtonSkin.BOTAO_MENU2);
        botoesFinanceiros.put("SAQUE", ButtonSkin.BOTAO_MENU2);
        botoesFinanceiros.put("SAQUE POUP. SOCIAL DIGITAL", ButtonSkin.BOTAO_MENU2);
   		botoesFinanceiros.put("DEPÓSITO", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("CONSULTA SALDO", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("RECARGA PRÉ-PAGO", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("PROCESSA CAIXACAP", ButtonSkin.BOTAO_MENU2);
        botoesFinanceiros.put("PEC", ButtonSkin.BOTAO_MENU2);
        botoesFinanceiros.put("REC", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("ABERTURA CONTA", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("BILHETE ÚNICO SPTRANS", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("PRODUTOS NEGOCIAIS", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("CADASTRA SENHA", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("MENSAGEM SMS", ButtonSkin.BOTAO_MENU2);
//		botoesFinanceiros.put("SAQUE SEM CARTÃO", ButtonSkin.BOTAO_MENU2);
//		botoesFinanceiros.put("SAQUE COM CARTÃO", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("CONTRATA CARTÃO", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("ARRECADA GPS", ButtonSkin.BOTAO_MENU2);
//		botoesFinanceiros.put("PAGAMENTO CAIXA TEM", ButtonSkin.BOTAO_MENU2);
//		botoesFinanceiros.put("ATIVAÇÃO E SAQUE CAIXA TEM", ButtonSkin.BOTAO_MENU2);
		botoesFinanceiros.put("CAIXA TEM", ButtonSkin.BOTAO_MENU2);
		
		botoesFinanceiros.put("PRODUTOS NEGOCIAIS", ButtonSkin.BOTAO_MENU2);
//		botoesFinanceiros.put("CONTRATA CARTÃO CRÉDITO", ButtonSkin.BOTAO_MENU2);
//		botoesFinanceiros.put("CONTRATA/RENOVA CONSIGNADO", ButtonSkin.BOTAO_MENU2);
        menuActionPermission = false;
        initialize();

        //boolean isFilaInativa = !EFLFrame.getInstance().getPnlFilaImpressao().isHabilitado();
        Iterator<?> iteratorAtalhos = this.uiMenuControle.getAtalhos().iterator();
        while (iteratorAtalhos.hasNext()) {
            Atalho atalho = (Atalho) iteratorAtalhos.next();
            boolean contemAtalho = atalho.getKeysTroke() != null;
            //boolean isBotaoJogo = botoesJogos.get(atalho.getNome()) != null;
            if (contemAtalho) {
                getActionMap().put(atalho.getKeysTroke(), new UseCaseAction(atalho.getTipoMovimento(), atalho.getOpcaoMovimento(), atalho.getNomeMovimento()));
                getInputMap().put(KeyStroke.getKeyStroke(atalho.getKeysTroke()), atalho.getKeysTroke());
            }
        }
        uiMenuControle.removerCpfPortadorAtendimento();
    }

    /**
     * Inicializa os componentes do painel
     */
    private void initialize() {
        add(getPnlTituloPrincipal());
        add(getPnlTituloSecundario());
        add(getButtonSair());
        
        add(getButtonCancelar());
        add(getButtonVoltar());
        uiMenuControle.negaTrocaTipoConcurso();
        Iterator<EFLButton> iteratorBotoes = getBotoesMenuDinamico().iterator();
        while (iteratorBotoes.hasNext()) {
            this.add((EFLButton) iteratorBotoes.next());
        }
        repaint();
        timerPollEstadoFila = new Timer(500, new ActionListener() {


			public void actionPerformed(ActionEvent e) {
		        Iterator<EFLButton> iteratorBotoes = getBotoesMenuDinamico().iterator();
		        //FIXME Remover reverência para habilitar botôes
        		boolean botoesHabilitados = true;//!EFLFrame.getInstance().getPnlFilaImpressao().isHabilitado();
                ItemMenuXml itemMenuAtual = uiMenuControle.getMenuAtual();
                boolean isMenuPrincipal = itemMenuAtual == uiMenuControle.getMenuRaiz();
	        	while (iteratorBotoes.hasNext()) {
	        		EFLButton botaoMenu = (EFLButton) iteratorBotoes.next();
	        		boolean isBotaoJogo = botaoMenu.getText().equals(""); 
	                if(isMenuPrincipal || !botoesHabilitados) {
		        		botaoMenu.setEnabled(botoesHabilitados || isBotaoJogo);
	                }
	            }
                GerenciaPerifericos.getInstance().setBarcodeListener(botoesHabilitados ? PnlMenu.this : null);
                getButtonCancelar().setEnabled(botoesHabilitados);
                getButtonSair().setEnabled(botoesHabilitados);
                getButtonVoltar().setEnabled(botoesHabilitados);
                if(botoesHabilitados) {
                	timerPollEstadoFila.stop();
                }
	            repaint();
			}
        });
        timerPollEstadoFila.start();
    }
    
    private EFLPanel getPnlTituloPrincipal() {
        if (pnlTituloPrincipal == null) {
            pnlTituloPrincipal = new EFLPanel(PanelSkin.PANEL_FAIXA_TITULO_PRINCIPAL);
            pnlTituloPrincipal.setLocation(0, 100);
            pnlTituloPrincipal.setSize(800, 28);
            pnlTituloPrincipal.setVisible(uiMenuControle.getMenuAtual().getTituloPrincipal() != null);
            pnlTituloPrincipal.add(getLabelTituloPrincipal());
        }
        return pnlTituloPrincipal;
    }

    private EFLLabel getLabelTituloPrincipal() {
        if (labelTituloPrincipal == null) {
            labelTituloPrincipal = new EFLLabel(LabelSkin.LABEL_FAIXA_TITULO_PRINCIPAL, uiMenuControle.getMenuAtual().getTituloPrincipal(), SwingConstants.RIGHT);
            labelTituloPrincipal.setLocation(0, 0);
            labelTituloPrincipal.setSize(750, 28);
        }
        return labelTituloPrincipal;
    }

    private EFLPanel getPnlTituloSecundario() {
        if (pnlTituloSecundario == null) {
            pnlTituloSecundario = new EFLPanel(PanelSkin.PANEL_FAIXA_TITULO_SECUNDARIO);
            pnlTituloSecundario.setLocation(0, 150);
            pnlTituloSecundario.setSize(800, 27);
            String faixaTituloSecundario = (String) faixasJogos.get(uiMenuControle.getMenuAtual().getNome());
            pnlTituloSecundario.setVisible(faixaTituloSecundario != null);
            if (faixaTituloSecundario != null) {
                pnlTituloSecundario.add(getLabelTituloSecundario(faixaTituloSecundario));
            }
        }
        return pnlTituloSecundario;
    }

    private EFLLabel getLabelTituloSecundario(String skin) {
        EFLLabel labelTituloSecundario = new EFLLabel(skin);
        labelTituloSecundario.setLocation(0, 0);
        labelTituloSecundario.setSize(800, 27);
        return labelTituloSecundario;
    }

    private Collection<EFLButton> getBotoesMenuDinamico() {
        if (botoesMenuDinamico == null) {
            botoesMenuDinamico = new ArrayList<EFLButton>();
            ItemMenuXml itemMenuAtual = uiMenuControle.getMenuAtual();
            boolean isMenuPrincipal = itemMenuAtual == uiMenuControle.getMenuRaiz();
            int numColunasBotoes = 0;
            int numLinhasBotoes = 0;
            if (isMenuPrincipal) {
                numColunasBotoes = 2;
                numLinhasBotoes = 5;
                //EFLFrame.getInstance().getPnlFilaImpressao().habilitaBotaoImpressao();
                getButtonSair().setEnabled(true);
            } else if (uiMenuControle.getMenuAtual().getNome().equals("BENEFÍCIOS SOCIAIS")) {
            	numColunasBotoes = 3;
            	numLinhasBotoes = 3;
            } else if (uiMenuControle.getMenuAtual().getNome().equals("APOIO")) { 
            	numColunasBotoes = 4;
            	numLinhasBotoes = 3;
            }  else if (uiMenuControle.getMenuAtual().getNome().equals("OUTROS SERVIÇOS FINANCEIROS")) { 
            	numColunasBotoes = 5;
            	numLinhasBotoes  = 5;
            	
            } else if (uiMenuControle.getMenuAtual().getNome().equals("CAIXA TEM")) {
            	numColunasBotoes = 2;
            	numLinhasBotoes = 1;
            } else if (uiMenuControle.getMenuAtual().getNome().equals("PRODUTOS NEGOCIAIS")) {
            	numColunasBotoes = 4;
            	numLinhasBotoes = 1;
            }else {
                numColunasBotoes = ((itemMenuAtual.getItemMenuXml().size() + 3) >> 2) > 2 ? (itemMenuAtual.getItemMenuXml().size() + 3) >> 2 : 2;
                numLinhasBotoes = (itemMenuAtual.getItemMenuXml().size() + (numColunasBotoes - 1)) / numColunasBotoes;
            }
            Iterator<ItemMenuXml> iteratorBotoes = uiMenuControle.getMenuAtual().getItemMenuXml().iterator();
            int countLinhasBotoes = 0;
            int countColunasBotoes = 0;
            int countLinhasBotoesJogo = 0;
            int countColunasBotoesJogo = 0;
            this.countFGTSBiometriaAux = 0;
            boolean isLeitorDesconectado = GerenciaPerifericos.getInstance().isLeitorDesconectado();
            while (iteratorBotoes.hasNext()) {
                ItemMenuXml itemMenuXml = (ItemMenuXml) iteratorBotoes.next();
                int codigoMovimento = itemMenuXml.getItemMenuXml() != null && !itemMenuXml.getItemMenuXml().isEmpty() ? itemMenuXml.getItemMenuXml().get(0).getCodigoMovimento() : itemMenuXml.getCodigoMovimento();




                boolean ehItem = uiMenuControle.isItemFolha(itemMenuXml);

				if (isBotaoDesabilitado(codigoMovimento) && ehItem ) {
					continue;
				}
				
				if (itemMenuXml.getNome().equals("CONTRATA/RENOVA CONSIGNADO")) {
					System.out.println("");
				}
				
                //Se for um botão de jogo.
                EFLButton botaoMenu;
                boolean isBotaoJogo = botoesJogos.get(itemMenuXml.getNome()) != null;
                boolean isBotaoFinanceiro = botoesFinanceiros.get(itemMenuXml.getNome()) != null;
                if (isBotaoJogo) {
                    botaoMenu = new EFLButton(uiManager, (String) botoesJogos.get(itemMenuXml.getNome()), "");
                    if (isMenuPrincipal) {
						botaoMenu.setLocation(
								((1200 - (numColunasBotoes * 180)) / 2) + (countColunasBotoesJogo++ * 180),
								((700 - (numLinhasBotoes * 80)) / 2) + (countLinhasBotoesJogo * 77));
					} else {
						botaoMenu.setLocation(((800 - (numColunasBotoes * 180)) / 2) + (countColunasBotoes++ * 180),
								((700 - (numLinhasBotoes * 80)) / 2) + (countLinhasBotoes * 80));
					}
                    botaoMenu.setSize(164, 52);
                } else if(isBotaoFinanceiro) { 
                	if (uiMenuControle.isItemFolha(itemMenuXml)) {
                		botaoMenu = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU2, itemMenuXml.getNome());
                        botaoMenu.setEnabled((true) && ControleAcesso.isAcessoPermitido(codigoMovimento));
                    } else {
                        botaoMenu = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU2_SETA, itemMenuXml.getNome());
                        botaoMenu.setEnabled(true);
                    }


					botaoMenu.setLocation(((670 - (4 * 150)) / 2) + (countLinhasBotoes * 150),
							((770 - (5 * 90)) / 2) + (countColunasBotoes++ * 90));
                    getButtonCancelar().setVisible(false);
                    
                    botaoMenu.setSize(140, 80);
				} else {
					if(itemMenuXml.getNome().equals("BOLÕES DIGITAIS")) {
						botaoMenu = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU_PEQ, itemMenuXml.getNome());
					} else if (uiMenuControle.isItemFolha(itemMenuXml)) {
                    	botaoMenu = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU, itemMenuXml.getNome());
                    	botaoMenu = this.ligaBotoesFGTS(botaoMenu, itemMenuXml.getCodigoMovimento(), isLeitorDesconectado);
                    } else {
                        botaoMenu = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU_SETA, itemMenuXml.getNome());
                        botaoMenu.setEnabled(true);
                    }  
                    if (isMenuPrincipal) {

						botaoMenu.setLocation(((400 - (numColunasBotoes * 180)) / 2) + (countColunasBotoes++ * 180),
								((700 - (numLinhasBotoes * 80)) / 2) + (countLinhasBotoes * 73));
						botaoMenu.setSize(164, 68);
                    } else if(itemMenuXml.getNome().equals("BOLÕES DIGITAIS")) {
                    	botaoMenu.setLocation(500, 528);
                    	botaoMenu.setSize(180, 50);
                    	codigoMovimento = itemMenuXml.getCodigoMovimento();
                    } else if(itemMenuXml.getNome().equals("VENDER COTAS")) {
                    	botaoMenu.setLocation(50, 300);
                    	botaoMenu.setSize(190, 70);
                    	codigoMovimento = itemMenuXml.getCodigoMovimento();
                    	add(getLabelLegendaVenderCotasMKP());
                    } else if(itemMenuXml.getNome().equals("BOLÕES MARKETPLACE")) {
                    	botaoMenu.setLocation(300, 300);
                    	botaoMenu.setSize(190, 70);
                    	codigoMovimento = itemMenuXml.getCodigoMovimento();
                    	add(getLabelLegendaBoloesMarketPlace());
                    } else if(itemMenuXml.getNome().equals("BOLÕES CONCLUÍDOS")) {
                    	botaoMenu.setLocation(550, 300);
                    	botaoMenu.setSize(190, 70);
                    	codigoMovimento = itemMenuXml.getCodigoMovimento();
                    	add(getLabelLegendaBoloesConcluidosMKP());
                    } else {
							botaoMenu.setLocation(((800 - (numColunasBotoes * 180)) / 2) + (countColunasBotoes++ * 180),
									((700 - (numLinhasBotoes * 80)) / 2) + (countLinhasBotoes * 80));
							botaoMenu.setSize(164, 68);
					}
                }
                boolean isTtemFolha = uiMenuControle.isItemFolha(itemMenuXml);
				if (isTtemFolha) {
					UseCaseAction useCaseAction = new UseCaseAction(itemMenuXml.getCodigoMovimento(),
							itemMenuXml.getOpcao(), itemMenuXml.getNomeMovimento());
                    botaoMenu.addActionListener(useCaseAction);
                } else {
                    botaoMenu.addActionListener(new MenuActionListener(itemMenuXml));
                }

                if (this.countFGTSBiometriaAux > 0) {
                	if(  this.countFGTSBiometriaAux == 1 ) {
                		uiMenuControle.mostraMensagemBiometriaComProblemasResumido();
                	} else if (this.countFGTSBiometriaAux == 2) {
                		uiMenuControle.mostraMensagemBiometriaComProblemas();
                	}
        		}
            	
            	botaoMenu.setVisible(AbstractControleMovimento.isHabilitaBotao(codigoMovimento,isTtemFolha));
            	// Em atendimento à demanda: RTC17711533/RTC20627833
            	botaoMenu.setEnabled(AbstractControleMovimento.isHabilitaBotaoConformeParametroCerificacaoFebraban(codigoMovimento,isTtemFolha));
            	isHabilitaBotaoAbriContaCaixaFacil(codigoMovimento, itemMenuXml, botaoMenu);
            	
                botoesMenuDinamico.add(botaoMenu);
                if(isBotaoFinanceiro){ 
	                if (countColunasBotoes == 4) {
	                    countColunasBotoes = 0;
	                    countLinhasBotoes++;
	                }
	            }else{
	            	
                	if (countColunasBotoes == numColunasBotoes) {
	                    countColunasBotoes = 0;
	                    countLinhasBotoes++;
	                }
                	
	                if (countColunasBotoesJogo == numColunasBotoes) {
	                    countColunasBotoesJogo = 0;
	                    countLinhasBotoesJogo++;
	                }
                }
            }
        }
        return botoesMenuDinamico;
    }

    private boolean isBotaoDesabilitado(int codigoMovimento) {
		return ((TipoMovimento.CONSULTA_AVALIACAO_CARTAO == codigoMovimento
				|| TipoMovimento.CONTRATA_CARTAO_CREDITO == codigoMovimento)
				&& !uiMenuControle.isHabilitaOpcaoDeMenu(ConstantesParametros.HABILITA_CONTRATA_CARTAO_CREDITO))
				|| (MenuBotaoDesabilitadoEnum.isMenuBotaoDesabilitado(codigoMovimento));

	}
    
    public static void isHabilitaBotaoAbriContaCaixaFacil(int codigoMovimento, ItemMenuXml itemMenuXml, EFLButton botaoMenu) {
		if(codigoMovimento == TipoMovimento.ABERTURA_POUPANCA_CONSULTA_NSGD && itemMenuXml.getNome().equalsIgnoreCase("POUPANÇA CAIXA FÁCIL")){
			Integer valor = SuportePropriedades.getInstance().
					getParametroInt(ConstantesParametros.HABILITA_DESABILITA_ABERTURA_CONTA_POUPANCA_CAIXA_FACIL);
			botaoMenu.setEnabled(valor == 1);
		}
	}

	private EFLButton ligaBotoesFGTS( EFLButton botaoMenu, int codMovimento, boolean isLeitorDesconectado) {
		boolean isSemCartaoHabilitado = false;
		boolean isBiometriaHabilitado = false;
		if( codMovimento == TipoMovimento.PAGA_FGTS_SEM_CARTAO ) {
			isSemCartaoHabilitado = uiMenuControle.isParametroHabilitaFGTS(ConstantesParametros.VERIFICACAO_FGTS_SACA_SEM_CARTAO);
			isBiometriaHabilitado = uiMenuControle.isParametroHabilitaFGTS(ConstantesParametros.VERIFICACAO_FGTS_SACA_SEM_CARTAO_BIOMETRIA);
		} else if ( codMovimento == TipoMovimento.PAGA_FGTS_SEM_CARTAO_COM_SENHA ) {
			isSemCartaoHabilitado = uiMenuControle.isParametroHabilitaFGTS(ConstantesParametros.VERIFICACAO_FGTS_SACA_SEM_CARTAO_COM_SENHA);
			isBiometriaHabilitado = uiMenuControle.isParametroHabilitaFGTS(ConstantesParametros.VERIFICACAO_FGTS_SACA_SEM_CARTAO_COM_SENHA_BIOMETRIA);




		} else {
			botaoMenu.setEnabled((true) && ControleAcesso.isAcessoPermitido(codMovimento));
			return botaoMenu;
		}
		if( isSemCartaoHabilitado && isBiometriaHabilitado && isLeitorDesconectado ) {
			countFGTSBiometriaAux++;
    		botaoMenu.setEnabled(false);
    		return botaoMenu;
		}
		botaoMenu.setEnabled(isSemCartaoHabilitado);
		return botaoMenu;
	}

    private EFLButton getButtonSair() {
        if (buttonSair == null) {
            buttonSair = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU_PEQ_VERMELHO, Messages.getString("SAIR"));
            buttonSair.setLocation(675, 533);
            buttonSair.setSize(80, 46);
            buttonSair.setVisible(uiMenuControle.getMenuAtual() == uiMenuControle.getMenuRaiz());
            buttonSair.addActionListener(new SwingWorkerActionListener() {
                                            private static final long serialVersionUID = 2007121201L;
											public void actionPerformedInBackground(ActionEvent e) {
                                                 if (!menuActionPermission) {
                                                     menuActionPermission = true;
                                                     timerPollEstadoFila.stop();
                                                     uiMenuControle.selecionaCasoUso(TipoMovimento.LOGOFF, 0, null);
                                                     menuActionPermission = false;
                                                 }
                                             }
                                         }
                                        );
        }
        return buttonSair;
    }
    
    private EFLButton getButtonVoltar() {
        if (buttonVoltar == null) {
            buttonVoltar = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU_PEQ, Messages.getString("VOLTAR"));
            buttonVoltar.setLocation(700, 530);
            buttonVoltar.setSize(80, 46);
            buttonVoltar.setVisible(uiMenuControle.getMenuAtual() != uiMenuControle.getMenuRaiz());
            buttonVoltar.addActionListener(new SwingWorkerActionListener() {
                                               private static final long serialVersionUID = 2007121201L;
                                               public void actionPerformedInBackground(ActionEvent e) {
                                                   if (!menuActionPermission) {
                                                       menuActionPermission = true;
                                                       timerPollEstadoFila.stop();
                                                       uiMenuControle.selecionaVoltar();
                                                       menuActionPermission = false;
                                                   }
                                               }
                                           }
                                          );
        }
        return buttonVoltar;
    }

    private EFLButton getButtonCancelar() {
        if (buttonCancelar == null) {
            buttonCancelar = new EFLButton(uiManager, ButtonSkin.BOTAO_MENU_PEQ_VERMELHO, Messages.getString("CANCELAR"));
            buttonCancelar.setLocation(20, 530);
            buttonCancelar.setSize(80, 46);
            buttonCancelar.setVisible(uiMenuControle.getMenuAtual() != uiMenuControle.getMenuRaiz());
            buttonCancelar.addActionListener(new SwingWorkerActionListener() {
                                                 private static final long serialVersionUID = 2007121201L;
                                                 public void actionPerformedInBackground(ActionEvent e) {
                                                     if (!menuActionPermission) {
                                                         menuActionPermission = true;
                                                         timerPollEstadoFila.stop();
                                                         uiMenuControle.selecionaCancelar();
                                                         menuActionPermission = false;
                                                     }
                                                 }
                                             }
                                            );
        }
        return buttonCancelar;
    }

    private class MenuActionListener extends SwingWorkerActionListener {
        private static final long serialVersionUID = 2007121201L;


		private ItemMenuXml itemMenu;

        public MenuActionListener(ItemMenuXml itemMenu) {
            this.itemMenu = itemMenu;
        }

        public void actionPerformedInBackground(ActionEvent actionEvent) {
            if (!menuActionPermission) {
                menuActionPermission = true;
                timerPollEstadoFila.stop();
                uiMenuControle.selecionaMenu(itemMenu);
                menuActionPermission = false;
            }
        }
    }

    private class UseCaseAction extends SwingWorkerActionListener {
        private static final long serialVersionUID = 2007091301L;

        private int tipoMovimento, opcaoMovimento;
        /*
         * O nomeMovimento é utilizado como chave para buscar o movimento no movimento.xml
         * descartando a utilizacao do tipo e opcao.
         */
        private String nomeMovimento;

        public UseCaseAction(int tipoMovimento, int opcaoMovimento, String nomeMovimento) {
            this.tipoMovimento = tipoMovimento;
            this.opcaoMovimento = opcaoMovimento;
            this.nomeMovimento = nomeMovimento;
        }

        public void actionPerformedInBackground(ActionEvent e) {
            if (!menuActionPermission) {
                menuActionPermission = true;
                timerPollEstadoFila.stop();
                uiMenuControle.selecionaCasoUso(tipoMovimento, opcaoMovimento, nomeMovimento);
                menuActionPermission = false;
            }
        }

    }

    /**
     * Listener de Código de barras do menu da EFL.
     * @see br.gov.caixa.sispl.infra.perifericos.event.BarcodeListener#dataReaded(br.gov.caixa.sispl.infra.perifericos.event.BarcodeEvent)
     */
    public void dataReaded(BarcodeEvent e) {
        timerPollEstadoFila.stop();
        uiMenuControle.passaCodigoBarras(e.getData());
    }

    /**
     * Listener de volantes do menu da EFL.
     * @see br.gov.caixa.sispl.infra.perifericos.event.ScannerListener#codeReaded(br.gov.caixa.sispl.infra.perifericos.event.ScannerEvent)
     */
    public void codeReaded(ScannerEvent e) {
        timerPollEstadoFila.stop();
        uiMenuControle.passaVolante(e.getOmr());
    }
    
    private EFLLabel labelLegendaCotasDisponiveis = null;
	private EFLLabel labelLegendaCotasBaixadas = null;
	private EFLLabel labelLegendaCotasReservadas = null;
	
    protected EFLLabel getLabelLegendaVenderCotasMKP() {
        if (labelLegendaCotasDisponiveis == null) {
        	labelLegendaCotasDisponiveis = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL,"Visualizar cotas digitais para venda na lotérica.");
        	labelLegendaCotasDisponiveis.setLocation(50, 370);
        	labelLegendaCotasDisponiveis.setSize(200, 65);
        	labelLegendaCotasDisponiveis.setHorizontalAlignment(EFLLabel.CENTER);
        }
        return labelLegendaCotasDisponiveis;
    }
	

	protected EFLLabel getLabelLegendaBoloesMarketPlace() {
        if (labelLegendaCotasBaixadas == null) {
        	labelLegendaCotasBaixadas = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL,"Consulta bolões de concursos abertos para comercialização.");
        	labelLegendaCotasBaixadas.setLocation(300, 370);
        	labelLegendaCotasBaixadas.setSize(200, 65);
        	labelLegendaCotasBaixadas.setHorizontalAlignment(EFLLabel.CENTER);
        }
        return labelLegendaCotasBaixadas;
    }
	
	protected EFLLabel getLabelLegendaBoloesConcluidosMKP() {
        if (labelLegendaCotasReservadas == null) {
        	labelLegendaCotasReservadas = new EFLLabel(LabelSkin.LABEL_TEXTO_AZUL,"Consulta bolões de concursos encerrados e não prescritos.");
        	labelLegendaCotasReservadas.setLocation(550, 370);
        	labelLegendaCotasReservadas.setSize(200, 65);
        	labelLegendaCotasReservadas.setHorizontalAlignment(EFLLabel.CENTER);
        }
        return labelLegendaCotasReservadas;
    }
}